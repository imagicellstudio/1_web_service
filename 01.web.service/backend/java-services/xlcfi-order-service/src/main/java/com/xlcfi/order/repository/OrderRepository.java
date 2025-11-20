package com.xlcfi.order.repository;

import com.xlcfi.order.domain.Order;
import com.xlcfi.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * 주문번호로 조회
     */
    Optional<Order> findByOrderNumber(String orderNumber);
    
    /**
     * 구매자별 주문 조회 (페이징)
     */
    Page<Order> findByBuyerId(Long buyerId, Pageable pageable);
    
    /**
     * 판매자별 주문 조회 (페이징)
     */
    Page<Order> findBySellerId(Long sellerId, Pageable pageable);
    
    /**
     * 구매자별 + 상태별 주문 조회 (페이징)
     */
    Page<Order> findByBuyerIdAndStatus(Long buyerId, OrderStatus status, Pageable pageable);
    
    /**
     * 판매자별 + 상태별 주문 조회 (페이징)
     */
    Page<Order> findBySellerIdAndStatus(Long sellerId, OrderStatus status, Pageable pageable);
    
    /**
     * 기간별 주문 조회
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate);
    
    /**
     * 판매자별 + 기간별 주문 통계
     */
    @Query("SELECT COUNT(o), SUM(o.total) FROM Order o WHERE " +
           "o.sellerId = :sellerId AND " +
           "o.status = :status AND " +
           "o.createdAt BETWEEN :startDate AND :endDate")
    Object[] getSellerOrderStats(@Param("sellerId") Long sellerId,
                                  @Param("status") OrderStatus status,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);
}

