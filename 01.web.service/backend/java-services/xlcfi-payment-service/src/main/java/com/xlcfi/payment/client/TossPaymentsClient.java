package com.xlcfi.payment.client;

import com.xlcfi.payment.dto.tosspayments.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 토스페이먼츠 API 클라이언트
 * 
 * 공식 문서: https://docs.tosspayments.com/
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentsClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${payment.toss.secret-key}")
    private String secretKey;

    @Value("${payment.toss.api-url:https://api.tosspayments.com}")
    private String apiUrl;

    /**
     * 결제 승인
     * 
     * @param request 결제 승인 요청
     * @return 결제 승인 응답
     */
    public TossPaymentConfirmResponse confirmPayment(TossPaymentConfirmRequest request) {
        log.info("토스페이먼츠 결제 승인 요청: paymentKey={}, orderId={}, amount={}", 
                request.getPaymentKey(), request.getOrderId(), request.getAmount());

        try {
            TossPaymentConfirmResponse response = createWebClient()
                    .post()
                    .uri("/v1/payments/confirm")
                    .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(TossPaymentConfirmResponse.class)
                    .block();

            log.info("토스페이먼츠 결제 승인 성공: paymentKey={}", request.getPaymentKey());
            return response;

        } catch (Exception e) {
            log.error("토스페이먼츠 결제 승인 실패: {}", e.getMessage(), e);
            throw new RuntimeException("토스페이먼츠 결제 승인 실패", e);
        }
    }

    /**
     * 결제 조회
     * 
     * @param paymentKey 결제 키
     * @return 결제 정보
     */
    public TossPaymentResponse getPayment(String paymentKey) {
        log.info("토스페이먼츠 결제 조회: paymentKey={}", paymentKey);

        try {
            TossPaymentResponse response = createWebClient()
                    .get()
                    .uri("/v1/payments/{paymentKey}", paymentKey)
                    .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader())
                    .retrieve()
                    .bodyToMono(TossPaymentResponse.class)
                    .block();

            log.info("토스페이먼츠 결제 조회 성공: paymentKey={}", paymentKey);
            return response;

        } catch (Exception e) {
            log.error("토스페이먼츠 결제 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("토스페이먼츠 결제 조회 실패", e);
        }
    }

    /**
     * 결제 취소 (환불)
     * 
     * @param paymentKey 결제 키
     * @param request 취소 요청
     * @return 취소 응답
     */
    public TossPaymentCancelResponse cancelPayment(String paymentKey, TossPaymentCancelRequest request) {
        log.info("토스페이먼츠 결제 취소 요청: paymentKey={}, cancelReason={}", 
                paymentKey, request.getCancelReason());

        try {
            TossPaymentCancelResponse response = createWebClient()
                    .post()
                    .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                    .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(TossPaymentCancelResponse.class)
                    .block();

            log.info("토스페이먼츠 결제 취소 성공: paymentKey={}", paymentKey);
            return response;

        } catch (Exception e) {
            log.error("토스페이먼츠 결제 취소 실패: {}", e.getMessage(), e);
            throw new RuntimeException("토스페이먼츠 결제 취소 실패", e);
        }
    }

    /**
     * WebClient 생성
     */
    private WebClient createWebClient() {
        return webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Authorization 헤더 생성
     * 토스페이먼츠는 Basic Auth 사용 (secretKey를 Base64 인코딩)
     */
    private String getAuthorizationHeader() {
        String auth = secretKey + ":";
        String encodedAuth = Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encodedAuth;
    }
}




