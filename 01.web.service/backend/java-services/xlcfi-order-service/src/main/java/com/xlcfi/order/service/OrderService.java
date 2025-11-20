package com.xlcfi.order.service;

import com.xlcfi.auth.domain.User;
import com.xlcfi.auth.repository.UserRepository;
import com.xlcfi.common.exception.BusinessException;
import com.xlcfi.order.domain.Order;
import com.xlcfi.order.domain.OrderItem;
import com.xlcfi.order.domain.OrderStatus;
import com.xlcfi.order.dto.CreateOrderRequest;
import com.xlcfi.order.dto.OrderItemRequest;
import com.xlcfi.order.dto.OrderResponse;
import com.xlcfi.order.repository.OrderRepository;
import com.xlcfi.product.domain.Product;
import com.xlcfi.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * 주문 생성
     */
    @Transactional
    public OrderResponse createOrder(Long buyerId, CreateOrderRequest request) {
        log.info("주문 생성: buyerId={}, sellerId={}", buyerId, request.getSellerId());

        // 구매자 조회
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new BusinessException("ORDER001", "구매자를 찾을 수 없습니다"));

        // 판매자 조회
        User seller = userRepository.findById(request.getSellerId())
                .orElseThrow(() -> new BusinessException("ORDER002", "판매자를 찾을 수 없습니다"));

        // 주문 생성
        Order order = Order.builder()
                .buyer(buyer)
                .seller(seller)
                .total(BigDecimal.ZERO)
                .shippingAddress(request.getShippingAddress())
                .status(OrderStatus.PENDING)
                .build();

        // 주문 항목 추가 및 총액 계산
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new BusinessException("ORDER003", "상품을 찾을 수 없습니다"));

            // 재고 확인
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new BusinessException("ORDER004", 
                        String.format("상품 재고가 부족합니다: %s (재고: %d, 요청: %d)", 
                                product.getName(), product.getStockQuantity(), itemRequest.getQuantity()));
            }

            // 주문 항목 생성
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .productNameEn(product.getNameEn())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build();

            order.addOrderItem(orderItem);
            totalAmount = totalAmount.add(subtotal);
        }

        order.setTotal(totalAmount);
        Order savedOrder = orderRepository.save(order);

        log.info("주문 생성 완료: orderId={}, orderNumber={}, total={}", 
                savedOrder.getId(), savedOrder.getOrderNumber(), savedOrder.getTotal());

        return OrderResponse.from(savedOrder);
    }

    /**
     * 주문 상세 조회
     */
    public OrderResponse getOrder(Long orderId, Long userId) {
        log.info("주문 조회: orderId={}, userId={}", orderId, userId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORDER005", "주문을 찾을 수 없습니다"));

        // 권한 확인 (구매자 또는 판매자)
        if (!order.getBuyer().getId().equals(userId) && !order.getSeller().getId().equals(userId)) {
            throw new BusinessException("ORDER006", "주문을 조회할 권한이 없습니다");
        }

        return OrderResponse.from(order);
    }

    /**
     * 주문 목록 조회 (구매자)
     */
    public Page<OrderResponse> getOrdersByBuyer(Long buyerId, Pageable pageable) {
        log.info("구매자 주문 목록 조회: buyerId={}", buyerId);

        Page<Order> orders = orderRepository.findByBuyerId(buyerId, pageable);

        return orders.map(OrderResponse::from);
    }

    /**
     * 주문 목록 조회 (판매자)
     */
    public Page<OrderResponse> getOrdersBySeller(Long sellerId, Pageable pageable) {
        log.info("판매자 주문 목록 조회: sellerId={}", sellerId);

        Page<Order> orders = orderRepository.findBySellerId(sellerId, pageable);

        return orders.map(OrderResponse::from);
    }

    /**
     * 주문 상태 변경
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, Long userId, OrderStatus status) {
        log.info("주문 상태 변경: orderId={}, status={}", orderId, status);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORDER005", "주문을 찾을 수 없습니다"));

        // 권한 확인 (판매자만 가능)
        if (!order.getSeller().getId().equals(userId)) {
            throw new BusinessException("ORDER007", "주문 상태를 변경할 권한이 없습니다");
        }

        // 상태 전환 검증
        validateStatusTransition(order.getStatus(), status);

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        log.info("주문 상태 변경 완료: orderId={}, status={}", orderId, status);

        return OrderResponse.from(updatedOrder);
    }

    /**
     * 주문 취소
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        log.info("주문 취소: orderId={}, userId={}", orderId, userId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORDER005", "주문을 찾을 수 없습니다"));

        // 권한 확인 (구매자만 가능)
        if (!order.getBuyer().getId().equals(userId)) {
            throw new BusinessException("ORDER008", "주문을 취소할 권한이 없습니다");
        }

        // 취소 가능한 상태인지 확인
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException("ORDER009", "취소할 수 없는 주문 상태입니다");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);

        log.info("주문 취소 완료: orderId={}", orderId);

        return OrderResponse.from(cancelledOrder);
    }

    /**
     * 주문 상태 전환 검증
     */
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // 상태 전환 규칙 검증
        switch (currentStatus) {
            case PENDING:
                if (newStatus != OrderStatus.PAID && newStatus != OrderStatus.CANCELLED) {
                    throw new BusinessException("ORDER010", "잘못된 주문 상태 전환입니다");
                }
                break;
            case PAID:
                if (newStatus != OrderStatus.CONFIRMED && newStatus != OrderStatus.CANCELLED) {
                    throw new BusinessException("ORDER010", "잘못된 주문 상태 전환입니다");
                }
                break;
            case CONFIRMED:
                if (newStatus != OrderStatus.SHIPPING) {
                    throw new BusinessException("ORDER010", "잘못된 주문 상태 전환입니다");
                }
                break;
            case SHIPPING:
                if (newStatus != OrderStatus.DELIVERED) {
                    throw new BusinessException("ORDER010", "잘못된 주문 상태 전환입니다");
                }
                break;
            default:
                throw new BusinessException("ORDER010", "변경할 수 없는 주문 상태입니다");
        }
    }
}

