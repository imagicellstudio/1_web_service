package com.xlcfi.payment.client;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentRetrieveParams;
import com.stripe.param.RefundCreateParams;
import com.xlcfi.payment.dto.stripe.StripePaymentIntentRequest;
import com.xlcfi.payment.dto.stripe.StripePaymentIntentResponse;
import com.xlcfi.payment.dto.stripe.StripeRefundRequest;
import com.xlcfi.payment.dto.stripe.StripeRefundResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;

/**
 * Stripe API 클라이언트
 * 
 * 공식 문서: https://stripe.com/docs/api
 */
@Slf4j
@Component
public class StripeClient {

    @Value("${payment.stripe.secret-key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
        log.info("Stripe API 초기화 완료");
    }

    /**
     * Payment Intent 생성
     * 
     * @param request Payment Intent 생성 요청
     * @return Payment Intent 응답
     */
    public StripePaymentIntentResponse createPaymentIntent(StripePaymentIntentRequest request) {
        log.info("Stripe Payment Intent 생성: amount={}, currency={}", 
                request.getAmount(), request.getCurrency());

        try {
            // 금액을 센트 단위로 변환 (Stripe는 최소 단위 사용)
            long amountInCents = request.getAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(request.getCurrency().toLowerCase())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .putMetadata("orderId", request.getOrderId())
                    .putMetadata("customerEmail", request.getCustomerEmail())
                    .setDescription(request.getDescription())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            log.info("Stripe Payment Intent 생성 성공: id={}, clientSecret={}", 
                    paymentIntent.getId(), paymentIntent.getClientSecret());

            return StripePaymentIntentResponse.from(paymentIntent);

        } catch (StripeException e) {
            log.error("Stripe Payment Intent 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Stripe Payment Intent 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Payment Intent 조회
     * 
     * @param paymentIntentId Payment Intent ID
     * @return Payment Intent 응답
     */
    public StripePaymentIntentResponse getPaymentIntent(String paymentIntentId) {
        log.info("Stripe Payment Intent 조회: id={}", paymentIntentId);

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            log.info("Stripe Payment Intent 조회 성공: id={}, status={}", 
                    paymentIntent.getId(), paymentIntent.getStatus());

            return StripePaymentIntentResponse.from(paymentIntent);

        } catch (StripeException e) {
            log.error("Stripe Payment Intent 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Stripe Payment Intent 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Payment Intent 확인 (결제 승인)
     * 
     * @param paymentIntentId Payment Intent ID
     * @return Payment Intent 응답
     */
    public StripePaymentIntentResponse confirmPaymentIntent(String paymentIntentId) {
        log.info("Stripe Payment Intent 확인: id={}", paymentIntentId);

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            
            // 이미 확인된 경우
            if ("succeeded".equals(paymentIntent.getStatus())) {
                log.info("이미 확인된 Payment Intent: id={}", paymentIntentId);
                return StripePaymentIntentResponse.from(paymentIntent);
            }

            log.info("Stripe Payment Intent 확인 완료: id={}, status={}", 
                    paymentIntent.getId(), paymentIntent.getStatus());

            return StripePaymentIntentResponse.from(paymentIntent);

        } catch (StripeException e) {
            log.error("Stripe Payment Intent 확인 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Stripe Payment Intent 확인 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 환불 처리
     * 
     * @param request 환불 요청
     * @return 환불 응답
     */
    public StripeRefundResponse createRefund(StripeRefundRequest request) {
        log.info("Stripe 환불 생성: paymentIntentId={}, amount={}", 
                request.getPaymentIntentId(), request.getAmount());

        try {
            RefundCreateParams.Builder paramsBuilder = RefundCreateParams.builder()
                    .setPaymentIntent(request.getPaymentIntentId());

            // 부분 환불인 경우 금액 지정
            if (request.getAmount() != null) {
                long amountInCents = request.getAmount()
                        .multiply(BigDecimal.valueOf(100))
                        .longValue();
                paramsBuilder.setAmount(amountInCents);
            }

            // 환불 사유
            if (request.getReason() != null) {
                paramsBuilder.setReason(RefundCreateParams.Reason.valueOf(
                        request.getReason().toUpperCase()
                ));
            }

            Refund refund = Refund.create(paramsBuilder.build());

            log.info("Stripe 환불 생성 성공: id={}, status={}", 
                    refund.getId(), refund.getStatus());

            return StripeRefundResponse.from(refund);

        } catch (StripeException e) {
            log.error("Stripe 환불 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Stripe 환불 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 환불 조회
     * 
     * @param refundId 환불 ID
     * @return 환불 응답
     */
    public StripeRefundResponse getRefund(String refundId) {
        log.info("Stripe 환불 조회: id={}", refundId);

        try {
            Refund refund = Refund.retrieve(refundId);

            log.info("Stripe 환불 조회 성공: id={}, status={}", 
                    refund.getId(), refund.getStatus());

            return StripeRefundResponse.from(refund);

        } catch (StripeException e) {
            log.error("Stripe 환불 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Stripe 환불 조회 실패: " + e.getMessage(), e);
        }
    }
}


