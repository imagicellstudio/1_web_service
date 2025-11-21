package com.xlcfi.payment.service;

import com.xlcfi.common.exception.BusinessException;
import com.xlcfi.order.domain.Order;
import com.xlcfi.order.domain.OrderStatus;
import com.xlcfi.order.repository.OrderRepository;
import com.xlcfi.payment.client.NicePayClient;
import com.xlcfi.payment.client.StripeClient;
import com.xlcfi.payment.client.TossPaymentsClient;
import com.xlcfi.payment.domain.Payment;
import com.xlcfi.payment.domain.PaymentStatus;
import com.xlcfi.payment.dto.CreatePaymentRequest;
import com.xlcfi.payment.dto.PaymentResponse;
import com.xlcfi.payment.dto.nicepay.*;
import com.xlcfi.payment.dto.stripe.*;
import com.xlcfi.payment.dto.tosspayments.*;
import com.xlcfi.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final TossPaymentsClient tossPaymentsClient;
    private final NicePayClient nicePayClient;
    private final StripeClient stripeClient;

    /**
     * 결제 생성 (PG사별 분기)
     */
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        log.info("결제 생성: orderId={}, amount={}, pgProvider={}", 
                request.getOrderId(), request.getAmount(), request.getPgProvider());

        // 주문 조회
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException("PAYMENT001", "주문을 찾을 수 없습니다"));

        // 결제 금액 검증
        if (request.getAmount().compareTo(order.getTotal()) != 0) {
            throw new BusinessException("PAYMENT002", "결제 금액이 주문 금액과 일치하지 않습니다"));
        }

        // 중복 결제 확인
        boolean existsPendingPayment = paymentRepository.existsByOrderIdAndStatusIn(
                order.getId(), 
                List.of(PaymentStatus.PENDING, PaymentStatus.COMPLETED)
        );
        
        if (existsPendingPayment) {
            throw new BusinessException("PAYMENT003", "이미 결제가 진행 중이거나 완료되었습니다");
        }

        // 결제 정보 저장 (PG사 연동 전)
        Payment payment = Payment.builder()
                .order(order)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .pgProvider(request.getPgProvider())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("결제 생성 완료: paymentId={}", savedPayment.getId());

        return PaymentResponse.from(savedPayment);
    }

    /**
     * 토스페이먼츠 결제 승인
     */
    @Transactional
    public PaymentResponse confirmTossPayment(String paymentKey, String orderId, String amount) {
        log.info("토스페이먼츠 결제 승인: paymentKey={}, orderId={}", paymentKey, orderId);

        Order order = orderRepository.findById(Long.parseLong(orderId))
                .orElseThrow(() -> new BusinessException("PAYMENT001", "주문을 찾을 수 없습니다"));

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new BusinessException("PAYMENT004", "결제를 찾을 수 없습니다"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("PAYMENT005", "이미 처리된 결제입니다");
        }

        try {
            TossPaymentConfirmRequest confirmRequest = TossPaymentConfirmRequest.builder()
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(payment.getAmount())
                    .build();

            TossPaymentConfirmResponse tossResponse = 
                    tossPaymentsClient.confirmPayment(confirmRequest);

            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPgTransactionId(tossResponse.getPaymentKey());
            payment.setPaidAt(LocalDateTime.now());
            
            Map<String, Object> pgResponse = new HashMap<>();
            pgResponse.put("paymentKey", tossResponse.getPaymentKey());
            pgResponse.put("orderId", tossResponse.getOrderId());
            pgResponse.put("status", tossResponse.getStatus());
            pgResponse.put("method", tossResponse.getMethod());
            pgResponse.put("totalAmount", tossResponse.getTotalAmount());
            payment.setPgResponse(pgResponse);

            Payment completedPayment = paymentRepository.save(payment);

            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);

            log.info("토스페이먼츠 결제 승인 완료: paymentId={}", completedPayment.getId());
            return PaymentResponse.from(completedPayment);

        } catch (Exception e) {
            log.error("토스페이먼츠 결제 승인 실패: paymentId={}", payment.getId(), e);
            payment.setStatus(PaymentStatus.FAILED);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            payment.setPgResponse(errorResponse);
            paymentRepository.save(payment);
            throw new BusinessException("PAYMENT006", "결제 승인에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 나이스페이 결제 승인
     */
    @Transactional
    public PaymentResponse confirmNicePayment(String tid, String orderId) {
        log.info("나이스페이 결제 승인: tid={}, orderId={}", tid, orderId);

        Order order = orderRepository.findById(Long.parseLong(orderId))
                .orElseThrow(() -> new BusinessException("PAYMENT001", "주문을 찾을 수 없습니다"));

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new BusinessException("PAYMENT004", "결제를 찾을 수 없습니다"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("PAYMENT005", "이미 처리된 결제입니다");
        }

        try {
            NicePayApprovalRequest approvalRequest = NicePayApprovalRequest.builder()
                    .tid(tid)
                    .amt(payment.getAmount())
                    .orderId(orderId)
                    .goodsName(order.getOrderItems().get(0).getProduct().getName())
                    .build();

            NicePayApprovalResponse niceResponse = 
                    nicePayClient.approvePayment(approvalRequest);

            if (!"0000".equals(niceResponse.getResultCode())) {
                throw new RuntimeException("나이스페이 승인 실패: " + niceResponse.getResultMsg());
            }

            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPgTransactionId(niceResponse.getTid());
            payment.setPaidAt(LocalDateTime.now());
            
            Map<String, Object> pgResponse = new HashMap<>();
            pgResponse.put("tid", niceResponse.getTid());
            pgResponse.put("orderId", niceResponse.getOrderId());
            pgResponse.put("resultCode", niceResponse.getResultCode());
            pgResponse.put("payMethod", niceResponse.getPayMethod());
            payment.setPgResponse(pgResponse);

            Payment completedPayment = paymentRepository.save(payment);

            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);

            log.info("나이스페이 결제 승인 완료: paymentId={}", completedPayment.getId());
            return PaymentResponse.from(completedPayment);

        } catch (Exception e) {
            log.error("나이스페이 결제 승인 실패: paymentId={}", payment.getId(), e);
            payment.setStatus(PaymentStatus.FAILED);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            payment.setPgResponse(errorResponse);
            paymentRepository.save(payment);
            throw new BusinessException("PAYMENT006", "결제 승인에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * Stripe 결제 Intent 생성
     */
    @Transactional
    public Map<String, Object> createStripePaymentIntent(Long orderId) {
        log.info("Stripe Payment Intent 생성: orderId={}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("PAYMENT001", "주문을 찾을 수 없습니다"));

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new BusinessException("PAYMENT004", "결제를 찾을 수 없습니다"));

        try {
            StripePaymentIntentRequest intentRequest = StripePaymentIntentRequest.builder()
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency().toLowerCase())
                    .orderId(orderId.toString())
                    .description(order.getOrderItems().get(0).getProduct().getName())
                    .customerEmail("customer@example.com") // 실제로는 사용자 정보에서 가져옴
                    .build();

            StripePaymentIntentResponse intentResponse = 
                    stripeClient.createPaymentIntent(intentRequest);

            // Payment Intent ID 저장
            payment.setPgTransactionId(intentResponse.getId());
            Map<String, Object> pgResponse = new HashMap<>();
            pgResponse.put("paymentIntentId", intentResponse.getId());
            pgResponse.put("clientSecret", intentResponse.getClientSecret());
            pgResponse.put("status", intentResponse.getStatus());
            payment.setPgResponse(pgResponse);
            paymentRepository.save(payment);

            Map<String, Object> result = new HashMap<>();
            result.put("clientSecret", intentResponse.getClientSecret());
            result.put("paymentIntentId", intentResponse.getId());

            log.info("Stripe Payment Intent 생성 완료: paymentIntentId={}", intentResponse.getId());
            return result;

        } catch (Exception e) {
            log.error("Stripe Payment Intent 생성 실패: orderId={}", orderId, e);
            throw new BusinessException("PAYMENT006", "Payment Intent 생성에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * Stripe 결제 확인 (Webhook에서 호출)
     */
    @Transactional
    public void confirmStripePayment(String paymentIntentId) {
        log.info("Stripe 결제 확인: paymentIntentId={}", paymentIntentId);

        Payment payment = paymentRepository.findByPgTransactionId(paymentIntentId)
                .orElseThrow(() -> new BusinessException("PAYMENT004", "결제를 찾을 수 없습니다"));

        try {
            StripePaymentIntentResponse intentResponse = 
                    stripeClient.getPaymentIntent(paymentIntentId);

            if ("succeeded".equals(intentResponse.getStatus())) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());

                Order order = payment.getOrder();
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);

                log.info("Stripe 결제 확인 완료: paymentId={}", payment.getId());
            }

            paymentRepository.save(payment);

        } catch (Exception e) {
            log.error("Stripe 결제 확인 실패: paymentIntentId={}", paymentIntentId, e);
            throw new BusinessException("PAYMENT006", "결제 확인에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 결제 조회
     */
    public PaymentResponse getPayment(Long paymentId) {
        log.info("결제 조회: paymentId={}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("PAYMENT004", "결제를 찾을 수 없습니다"));

        return PaymentResponse.from(payment);
    }

    /**
     * 결제 키로 조회
     */
    public PaymentResponse getPaymentByPgTransactionId(String pgTransactionId) {
        log.info("결제 조회 (PG 트랜잭션 ID): pgTransactionId={}", pgTransactionId);

        Payment payment = paymentRepository.findByPgTransactionId(pgTransactionId)
                .orElseThrow(() -> new BusinessException("PAYMENT004", "결제를 찾을 수 없습니다"));

        return PaymentResponse.from(payment);
    }

    /**
     * 사용자별 결제 목록 조회
     */
    public Page<PaymentResponse> getUserPayments(Long userId, Pageable pageable) {
        log.info("사용자별 결제 목록 조회: userId={}", userId);

        Page<Payment> payments = paymentRepository.findAll(pageable);
        
        return payments.map(PaymentResponse::from);
    }

    /**
     * 결제 환불 (PG사별 분기)
     */
    @Transactional
    public PaymentResponse refundPayment(Long paymentId, String cancelReason) {
        log.info("결제 환불: paymentId={}, reason={}", paymentId, cancelReason);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("PAYMENT004", "결제를 찾을 수 없습니다"));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessException("PAYMENT007", "환불할 수 없는 결제 상태입니다");
        }

        try {
            switch (payment.getPgProvider()) {
                case "TOSS":
                    refundTossPayment(payment, cancelReason);
                    break;
                case "NICEPAY":
                    refundNicePayment(payment, cancelReason);
                    break;
                case "STRIPE":
                    refundStripePayment(payment, cancelReason);
                    break;
                default:
                    throw new BusinessException("PAYMENT008", "지원하지 않는 PG사입니다");
            }

            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setRefundedAt(LocalDateTime.now());
            Payment refundedPayment = paymentRepository.save(payment);

            Order order = payment.getOrder();
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            return PaymentResponse.from(refundedPayment);

        } catch (Exception e) {
            log.error("환불 처리 중 오류 발생: paymentId={}", paymentId, e);
            throw new BusinessException("PAYMENT009", "환불 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 토스페이먼츠 환불
     */
    private void refundTossPayment(Payment payment, String cancelReason) {
        TossPaymentCancelRequest cancelRequest = TossPaymentCancelRequest.builder()
                .cancelReason(cancelReason)
                .build();

        TossPaymentCancelResponse cancelResponse = 
                tossPaymentsClient.cancelPayment(
                        payment.getPgTransactionId(), 
                        cancelRequest
                );

        Map<String, Object> refundResponse = new HashMap<>();
        refundResponse.put("paymentKey", cancelResponse.getPaymentKey());
        refundResponse.put("status", cancelResponse.getStatus());
        refundResponse.put("cancelReason", cancelReason);
        payment.setPgResponse(refundResponse);

        log.info("토스페이먼츠 환불 완료: paymentId={}", payment.getId());
    }

    /**
     * 나이스페이 환불
     */
    private void refundNicePayment(Payment payment, String cancelReason) {
        NicePayCancelRequest cancelRequest = NicePayCancelRequest.builder()
                .cancelAmt(payment.getAmount())
                .cancelMsg(cancelReason)
                .partialCancelCode(false)
                .build();

        NicePayCancelResponse cancelResponse = 
                nicePayClient.cancelPayment(
                        payment.getPgTransactionId(), 
                        cancelRequest
                );

        Map<String, Object> refundResponse = new HashMap<>();
        refundResponse.put("tid", cancelResponse.getTid());
        refundResponse.put("resultCode", cancelResponse.getResultCode());
        refundResponse.put("cancelAmt", cancelResponse.getCancelAmt());
        payment.setPgResponse(refundResponse);

        log.info("나이스페이 환불 완료: paymentId={}", payment.getId());
    }

    /**
     * Stripe 환불
     */
    private void refundStripePayment(Payment payment, String cancelReason) {
        StripeRefundRequest refundRequest = StripeRefundRequest.builder()
                .paymentIntentId(payment.getPgTransactionId())
                .reason("requested_by_customer")
                .build();

        StripeRefundResponse refundResponse = 
                stripeClient.createRefund(refundRequest);

        Map<String, Object> refundResponseMap = new HashMap<>();
        refundResponseMap.put("refundId", refundResponse.getId());
        refundResponseMap.put("status", refundResponse.getStatus());
        refundResponseMap.put("reason", cancelReason);
        payment.setPgResponse(refundResponseMap);

        log.info("Stripe 환불 완료: paymentId={}", payment.getId());
    }

    /**
     * Webhook에서 호출 - 결제 상태 업데이트
     */
    @Transactional
    public void updatePaymentStatus(String pgTransactionId, String status) {
        log.info("결제 상태 업데이트 (Webhook): pgTransactionId={}, status={}", 
                pgTransactionId, status);

        Payment payment = paymentRepository.findByPgTransactionId(pgTransactionId)
                .orElseThrow(() -> new BusinessException("PAYMENT004", "결제를 찾을 수 없습니다"));

        switch (status) {
            case "DONE":
            case "succeeded":
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());
                
                Order order = payment.getOrder();
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
                break;
                
            case "CANCELED":
            case "PARTIAL_CANCELED":
            case "refunded":
                payment.setStatus(PaymentStatus.REFUNDED);
                payment.setRefundedAt(LocalDateTime.now());
                
                Order canceledOrder = payment.getOrder();
                canceledOrder.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(canceledOrder);
                break;
                
            case "ABORTED":
            case "EXPIRED":
            case "failed":
                payment.setStatus(PaymentStatus.FAILED);
                break;
        }

        paymentRepository.save(payment);
        log.info("결제 상태 업데이트 완료: paymentId={}, status={}", 
                payment.getId(), payment.getStatus());
    }
}
