package com.xlcfi.payment.controller;

import com.xlcfi.common.dto.ApiResponse;
import com.xlcfi.payment.dto.CreatePaymentRequest;
import com.xlcfi.payment.dto.PaymentResponse;
import com.xlcfi.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 결제 관련 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 생성
     * POST /api/payments
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {
        
        log.info("결제 생성 요청: orderId={}", request.getOrderId());
        
        PaymentResponse payment = paymentService.createPayment(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(payment, "결제가 생성되었습니다"));
    }

    /**
     * 결제 처리 (PG사 연동)
     * POST /api/payments/{paymentId}/process
     */
    @PostMapping("/{paymentId}/process")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @PathVariable Long paymentId,
            @RequestBody Map<String, Object> pgResponse) {
        
        log.info("결제 처리 요청: paymentId={}", paymentId);
        
        String pgTransactionId = (String) pgResponse.get("pgTransactionId");
        
        PaymentResponse payment = paymentService.processPayment(
                paymentId, pgTransactionId, pgResponse);
        
        return ResponseEntity.ok(
                ApiResponse.success(payment, "결제가 처리되었습니다"));
    }

    /**
     * 결제 조회
     * GET /api/payments/{paymentId}
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @PathVariable Long paymentId) {
        
        log.info("결제 조회 요청: paymentId={}", paymentId);
        
        PaymentResponse payment = paymentService.getPayment(paymentId);
        
        return ResponseEntity.ok(
                ApiResponse.success(payment, "결제 조회 성공"));
    }

    /**
     * 주문별 결제 목록 조회
     * GET /api/payments/order/{orderId}?page=0&size=20
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getPaymentsByOrder(
            @PathVariable Long orderId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("주문별 결제 목록 조회 요청: orderId={}", orderId);
        
        Page<PaymentResponse> payments = paymentService.getPaymentsByOrder(orderId, pageable);
        
        return ResponseEntity.ok(
                ApiResponse.success(payments, "결제 목록 조회 성공"));
    }

    /**
     * 결제 환불
     * POST /api/payments/{paymentId}/refund
     */
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @PathVariable Long paymentId,
            @RequestParam(required = false) String reason) {
        
        log.info("결제 환불 요청: paymentId={}, reason={}", paymentId, reason);
        
        PaymentResponse payment = paymentService.refundPayment(paymentId, reason);
        
        return ResponseEntity.ok(
                ApiResponse.success(payment, "환불이 처리되었습니다"));
    }
}

