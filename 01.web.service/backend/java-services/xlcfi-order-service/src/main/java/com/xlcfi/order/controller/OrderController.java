package com.xlcfi.order.controller;

import com.xlcfi.common.dto.ApiResponse;
import com.xlcfi.order.domain.OrderStatus;
import com.xlcfi.order.dto.CreateOrderRequest;
import com.xlcfi.order.dto.OrderResponse;
import com.xlcfi.order.service.OrderService;
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

/**
 * 주문 관련 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 생성
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @RequestAttribute("userId") Long buyerId,
            @Valid @RequestBody CreateOrderRequest request) {
        
        log.info("주문 생성 요청: buyerId={}", buyerId);
        
        OrderResponse order = orderService.createOrder(buyerId, request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(order, "주문이 생성되었습니다"));
    }

    /**
     * 주문 상세 조회
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable Long orderId,
            @RequestAttribute("userId") Long userId) {
        
        log.info("주문 조회 요청: orderId={}, userId={}", orderId, userId);
        
        OrderResponse order = orderService.getOrder(orderId, userId);
        
        return ResponseEntity.ok(
                ApiResponse.success(order, "주문 조회 성공"));
    }

    /**
     * 내 주문 목록 조회 (구매자)
     * GET /api/orders/my?page=0&size=20
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @RequestAttribute("userId") Long buyerId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("내 주문 목록 조회 요청: buyerId={}", buyerId);
        
        Page<OrderResponse> orders = orderService.getOrdersByBuyer(buyerId, pageable);
        
        return ResponseEntity.ok(
                ApiResponse.success(orders, "주문 목록 조회 성공"));
    }

    /**
     * 판매 주문 목록 조회 (판매자)
     * GET /api/orders/sales?page=0&size=20
     */
    @GetMapping("/sales")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getSalesOrders(
            @RequestAttribute("userId") Long sellerId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("판매 주문 목록 조회 요청: sellerId={}", sellerId);
        
        Page<OrderResponse> orders = orderService.getOrdersBySeller(sellerId, pageable);
        
        return ResponseEntity.ok(
                ApiResponse.success(orders, "판매 주문 목록 조회 성공"));
    }

    /**
     * 주문 상태 변경 (판매자)
     * PATCH /api/orders/{orderId}/status
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestAttribute("userId") Long userId,
            @RequestParam OrderStatus status) {
        
        log.info("주문 상태 변경 요청: orderId={}, status={}", orderId, status);
        
        OrderResponse order = orderService.updateOrderStatus(orderId, userId, status);
        
        return ResponseEntity.ok(
                ApiResponse.success(order, "주문 상태가 변경되었습니다"));
    }

    /**
     * 주문 취소 (구매자)
     * POST /api/orders/{orderId}/cancel
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long orderId,
            @RequestAttribute("userId") Long userId) {
        
        log.info("주문 취소 요청: orderId={}, userId={}", orderId, userId);
        
        OrderResponse order = orderService.cancelOrder(orderId, userId);
        
        return ResponseEntity.ok(
                ApiResponse.success(order, "주문이 취소되었습니다"));
    }
}

