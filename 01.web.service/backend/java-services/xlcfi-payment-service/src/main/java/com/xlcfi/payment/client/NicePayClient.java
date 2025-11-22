package com.xlcfi.payment.client;

import com.xlcfi.payment.dto.nicepay.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * 나이스페이 API 클라이언트
 * 
 * 공식 문서: https://developers.nicepay.co.kr/
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NicePayClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${payment.nicepay.merchant-key}")
    private String merchantKey;

    @Value("${payment.nicepay.merchant-id}")
    private String merchantId;

    @Value("${payment.nicepay.api-url:https://api.nicepay.co.kr}")
    private String apiUrl;

    /**
     * 결제 승인
     * 
     * @param request 결제 승인 요청
     * @return 결제 승인 응답
     */
    public NicePayApprovalResponse approvePayment(NicePayApprovalRequest request) {
        log.info("나이스페이 결제 승인 요청: tid={}, amount={}", 
                request.getTid(), request.getAmt());

        try {
            // 서명 생성
            String signature = generateSignature(request);
            request.setSignature(signature);

            NicePayApprovalResponse response = createWebClient()
                    .post()
                    .uri("/v1/payments/{tid}", request.getTid())
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + getAuthorizationHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(NicePayApprovalResponse.class)
                    .block();

            log.info("나이스페이 결제 승인 성공: tid={}, resultCode={}", 
                    request.getTid(), response.getResultCode());
            return response;

        } catch (Exception e) {
            log.error("나이스페이 결제 승인 실패: {}", e.getMessage(), e);
            throw new RuntimeException("나이스페이 결제 승인 실패", e);
        }
    }

    /**
     * 결제 조회
     * 
     * @param tid 거래 ID
     * @return 결제 정보
     */
    public NicePayResponse getPayment(String tid) {
        log.info("나이스페이 결제 조회: tid={}", tid);

        try {
            NicePayResponse response = createWebClient()
                    .get()
                    .uri("/v1/payments/{tid}", tid)
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + getAuthorizationHeader())
                    .retrieve()
                    .bodyToMono(NicePayResponse.class)
                    .block();

            log.info("나이스페이 결제 조회 성공: tid={}", tid);
            return response;

        } catch (Exception e) {
            log.error("나이스페이 결제 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("나이스페이 결제 조회 실패", e);
        }
    }

    /**
     * 결제 취소 (환불)
     * 
     * @param tid 거래 ID
     * @param request 취소 요청
     * @return 취소 응답
     */
    public NicePayCancelResponse cancelPayment(String tid, NicePayCancelRequest request) {
        log.info("나이스페이 결제 취소 요청: tid={}, cancelAmt={}", 
                tid, request.getCancelAmt());

        try {
            // 서명 생성
            String signature = generateCancelSignature(tid, request);
            request.setSignature(signature);

            NicePayCancelResponse response = createWebClient()
                    .post()
                    .uri("/v1/payments/{tid}/cancel", tid)
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + getAuthorizationHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(NicePayCancelResponse.class)
                    .block();

            log.info("나이스페이 결제 취소 성공: tid={}, resultCode={}", 
                    tid, response.getResultCode());
            return response;

        } catch (Exception e) {
            log.error("나이스페이 결제 취소 실패: {}", e.getMessage(), e);
            throw new RuntimeException("나이스페이 결제 취소 실패", e);
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
     * 나이스페이는 Basic Auth 사용 (merchantId:merchantKey를 Base64 인코딩)
     */
    private String getAuthorizationHeader() {
        String auth = merchantId + ":" + merchantKey;
        return Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 결제 승인 서명 생성
     * SHA-256 해시 사용
     */
    private String generateSignature(NicePayApprovalRequest request) {
        try {
            String data = request.getTid() + request.getAmt() + merchantKey;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            log.error("서명 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("서명 생성 실패", e);
        }
    }

    /**
     * 결제 취소 서명 생성
     */
    private String generateCancelSignature(String tid, NicePayCancelRequest request) {
        try {
            String data = tid + request.getCancelAmt() + merchantKey;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            log.error("취소 서명 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("취소 서명 생성 실패", e);
        }
    }

    /**
     * 바이트 배열을 16진수 문자열로 변환
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}







