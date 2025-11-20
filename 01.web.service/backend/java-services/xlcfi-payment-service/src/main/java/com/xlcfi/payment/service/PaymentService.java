package com.xlcfi.payment.service;

import com.xlcfi.common.exception.BusinessException;
import com.xlcfi.order.domain.Order;
import com.xlcfi.order.repository.OrderRepository;
import com.xlcfi.payment.domain.Payment;
import com.xlcfi.payment.domain.PaymentStatus;
import com.xlcfi.payment.dto.CreatePaymentRequest;
import com.xlcfi.payment.dto.PaymentResponse;
import com.xlcfi.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    /**
     * 결제 생성
     */
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        log.info("결제 생성: orderId={}, amount={}", request.getOrderId(), request.getAmount());

        // 주문 조회
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException("PAYMENT001", "주문을 찾을 수 없습니다"));

        // 결제 금액 검증
        if (request.getAmount().compareTo(order.getTotal()) != 0) {
            throw new BusinessException("PAYMENT002", "결제 금액이 주문 금액과 일치하지 않습니다");
        }

        // 결제 생성
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
     * 결제 처리 (PG사 연동)
     */
    @Transactional
    public PaymentResponse processPayment(Long paymentId, String pgTransactionId, Map<String, Object> pgResponse) {
        log.info("결제 처리: paymentId={}, pgTransactionId={}", paymentId, pgTransactionId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("PAYMENT003", "결제를 찾을 수 없습니다"));

        // 이미 처리된 결제인지 확인
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("PAYMENT004", "이미 처리된 결제입니다");
        }

        try {
            // PG사 API 호출 (실제로는 외부 API 호출)
            // 여기서는 시뮬레이션
            boolean paymentSuccess = simulatePaymentProcessing(payment);

            if (paymentSuccess) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setPgTransactionId(pgTransactionId);
                payment.setPgResponse(pgResponse);
                payment.setPaidAt(LocalDateTime.now());

                log.info("결제 완료: paymentId={}, pgTransactionId={}", paymentId, pgTransactionId);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setPgResponse(pgResponse);

                log.warn("결제 실패: paymentId={}", paymentId);
            }

            Payment processedPayment = paymentRepository.save(payment);
            return PaymentResponse.from(processedPayment);

        } catch (Exception e) {
            log.error("결제 처리 중 오류 발생: paymentId={}", paymentId, e);
            payment.setStatus(PaymentStatus.FAILED);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            payment.setPgResponse(errorResponse);
            
            paymentRepository.save(payment);
            
            throw new BusinessException("PAYMENT005", "결제 처리 중 오류가 발생했습니다");
        }
    }

    /**
     * 결제 조회
     */
    public PaymentResponse getPayment(Long paymentId) {
        log.info("결제 조회: paymentId={}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("PAYMENT003", "결제를 찾을 수 없습니다"));

        return PaymentResponse.from(payment);
    }

    /**
     * 주문별 결제 목록 조회
     */
    public Page<PaymentResponse> getPaymentsByOrder(Long orderId, Pageable pageable) {
        log.info("주문별 결제 목록 조회: orderId={}", orderId);

        Page<Payment> payments = paymentRepository.findAll(pageable);
        
        return payments.map(PaymentResponse::from);
    }

    /**
     * 결제 환불
     */
    @Transactional
    public PaymentResponse refundPayment(Long paymentId, String reason) {
        log.info("결제 환불: paymentId={}, reason={}", paymentId, reason);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("PAYMENT003", "결제를 찾을 수 없습니다"));

        // 환불 가능 상태 확인
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessException("PAYMENT006", "환불할 수 없는 결제 상태입니다");
        }

        try {
            // PG사 환불 API 호출 (실제로는 외부 API 호출)
            boolean refundSuccess = simulateRefundProcessing(payment);

            if (refundSuccess) {
                payment.setStatus(PaymentStatus.REFUNDED);
                payment.setRefundedAt(LocalDateTime.now());

                Map<String, Object> refundResponse = new HashMap<>();
                refundResponse.put("refundReason", reason);
                refundResponse.put("refundedAt", LocalDateTime.now().toString());
                payment.setPgResponse(refundResponse);

                log.info("환불 완료: paymentId={}", paymentId);
            } else {
                throw new BusinessException("PAYMENT007", "환불 처리에 실패했습니다");
            }

            Payment refundedPayment = paymentRepository.save(payment);
            return PaymentResponse.from(refundedPayment);

        } catch (Exception e) {
            log.error("환불 처리 중 오류 발생: paymentId={}", paymentId, e);
            throw new BusinessException("PAYMENT008", "환불 처리 중 오류가 발생했습니다");
        }
    }

    /**
     * 결제 처리 시뮬레이션
     */
    private boolean simulatePaymentProcessing(Payment payment) {
        // 실제로는 PG사 API를 호출하여 결제 처리
        // 여기서는 항상 성공으로 시뮬레이션
        log.info("PG사 결제 처리 시뮬레이션: paymentId={}", payment.getId());
        return true;
    }

    /**
     * 환불 처리 시뮬레이션
     */
    private boolean simulateRefundProcessing(Payment payment) {
        // 실제로는 PG사 API를 호출하여 환불 처리
        // 여기서는 항상 성공으로 시뮬레이션
        log.info("PG사 환불 처리 시뮬레이션: paymentId={}", payment.getId());
        return true;
    }
}

