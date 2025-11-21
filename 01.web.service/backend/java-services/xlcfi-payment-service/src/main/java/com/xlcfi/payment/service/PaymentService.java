package com.xlcfi.payment.service;

import com.xlcfi.common.exception.BusinessException;
import com.xlcfi.order.domain.Order;
import com.xlcfi.order.domain.OrderStatus;
import com.xlcfi.order.repository.OrderRepository;
import com.xlcfi.payment.client.TossPaymentsClient;
import com.xlcfi.payment.domain.Payment;
import com.xlcfi.payment.domain.PaymentStatus;
import com.xlcfi.payment.dto.CreatePaymentRequest;
import com.xlcfi.payment.dto.PaymentResponse;
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
            throw new BusinessException("PAYMENT002", "결제 금액이 주문 금액과 일치하지 않습니다");
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
     * 
     * @param paymentKey 토스페이먼츠 결제 키 (클라이언트에서 전달)
     * @param orderId 주문 ID
     * @param amount 결제 금액
     */
    @Transactional
    public PaymentResponse confirmTossPayment(String paymentKey, String orderId, String amount) {
        log.info("토스페이먼츠 결제 승인: paymentKey={}, orderId={}", paymentKey, orderId);

        // 주문 조회
        Order order = orderRepository.findById(Long.parseLong(orderId))
                .orElseThrow(() -> new BusinessException("PAYMENT001", "주문을 찾을 수 없습니다"));

        // 결제 조회
        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new BusinessException("PAYMENT004", "결제를 찾을 수 없습니다"));

        // 이미 처리된 결제인지 확인
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("PAYMENT005", "이미 처리된 결제입니다");
        }

        try {
            // 토스페이먼츠 결제 승인 API 호출
            TossPaymentConfirmRequest confirmRequest = TossPaymentConfirmRequest.builder()
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(payment.getAmount())
                    .build();

            TossPaymentConfirmResponse tossResponse = 
                    tossPaymentsClient.confirmPayment(confirmRequest);

            // 결제 정보 업데이트
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPgTransactionId(tossResponse.getPaymentKey());
            payment.setPaidAt(LocalDateTime.now());
            
            // PG 응답 저장
            Map<String, Object> pgResponse = new HashMap<>();
            pgResponse.put("paymentKey", tossResponse.getPaymentKey());
            pgResponse.put("orderId", tossResponse.getOrderId());
            pgResponse.put("orderName", tossResponse.getOrderName());
            pgResponse.put("status", tossResponse.getStatus());
            pgResponse.put("method", tossResponse.getMethod());
            pgResponse.put("totalAmount", tossResponse.getTotalAmount());
            pgResponse.put("approvedAt", tossResponse.getApprovedAt());
            payment.setPgResponse(pgResponse);

            Payment completedPayment = paymentRepository.save(payment);

            // 주문 상태 업데이트
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);

            log.info("토스페이먼츠 결제 승인 완료: paymentId={}, paymentKey={}", 
                    completedPayment.getId(), paymentKey);

            return PaymentResponse.from(completedPayment);

        } catch (Exception e) {
            log.error("토스페이먼츠 결제 승인 실패: paymentId={}", payment.getId(), e);
            
            // 결제 실패 처리
            payment.setStatus(PaymentStatus.FAILED);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("failedAt", LocalDateTime.now().toString());
            payment.setPgResponse(errorResponse);
            
            paymentRepository.save(payment);
            
            throw new BusinessException("PAYMENT006", "결제 승인에 실패했습니다: " + e.getMessage());
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
     * 결제 환불 (토스페이먼츠)
     */
    @Transactional
    public PaymentResponse refundPayment(Long paymentId, String cancelReason) {
        log.info("결제 환불: paymentId={}, reason={}", paymentId, cancelReason);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("PAYMENT004", "결제를 찾을 수 없습니다"));

        // 환불 가능 상태 확인
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessException("PAYMENT007", "환불할 수 없는 결제 상태입니다");
        }

        try {
            // PG사별 환불 처리
            if ("TOSS".equals(payment.getPgProvider())) {
                // 토스페이먼츠 환불
                TossPaymentCancelRequest cancelRequest = TossPaymentCancelRequest.builder()
                        .cancelReason(cancelReason)
                        .build();

                TossPaymentCancelResponse cancelResponse = 
                        tossPaymentsClient.cancelPayment(
                                payment.getPgTransactionId(), 
                                cancelRequest
                        );

                // 환불 정보 업데이트
                payment.setStatus(PaymentStatus.REFUNDED);
                payment.setRefundedAt(LocalDateTime.now());

                Map<String, Object> refundResponse = new HashMap<>();
                refundResponse.put("paymentKey", cancelResponse.getPaymentKey());
                refundResponse.put("orderId", cancelResponse.getOrderId());
                refundResponse.put("status", cancelResponse.getStatus());
                refundResponse.put("cancels", cancelResponse.getCancels());
                refundResponse.put("cancelReason", cancelReason);
                payment.setPgResponse(refundResponse);

                log.info("토스페이먼츠 환불 완료: paymentId={}", paymentId);

            } else {
                throw new BusinessException("PAYMENT008", "지원하지 않는 PG사입니다");
            }

            Payment refundedPayment = paymentRepository.save(payment);

            // 주문 상태 업데이트
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
     * Webhook에서 호출 - 결제 상태 업데이트
     */
    @Transactional
    public void updatePaymentStatus(String pgTransactionId, String status) {
        log.info("결제 상태 업데이트 (Webhook): pgTransactionId={}, status={}", 
                pgTransactionId, status);

        Payment payment = paymentRepository.findByPgTransactionId(pgTransactionId)
                .orElseThrow(() -> new BusinessException("PAYMENT004", "결제를 찾을 수 없습니다"));

        // 상태 업데이트
        switch (status) {
            case "DONE":
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());
                
                // 주문 상태 업데이트
                Order order = payment.getOrder();
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
                break;
                
            case "CANCELED":
            case "PARTIAL_CANCELED":
                payment.setStatus(PaymentStatus.REFUNDED);
                payment.setRefundedAt(LocalDateTime.now());
                
                // 주문 상태 업데이트
                Order canceledOrder = payment.getOrder();
                canceledOrder.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(canceledOrder);
                break;
                
            case "ABORTED":
            case "EXPIRED":
                payment.setStatus(PaymentStatus.FAILED);
                break;
        }

        paymentRepository.save(payment);
        log.info("결제 상태 업데이트 완료: paymentId={}, status={}", 
                payment.getId(), payment.getStatus());
    }
}
