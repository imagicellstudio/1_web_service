package com.xlcfi.order.repository;

import com.xlcfi.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * 주문 ID로 주문 항목 조회
     */
    List<OrderItem> findByOrderId(Long orderId);
    
    /**
     * 상품 ID로 주문 항목 조회
     */
    List<OrderItem> findByProductId(Long productId);
    
    /**
     * 특정 상품의 총 판매 수량 조회
     */
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.productId = :productId")
    Long getTotalSoldQuantity(@Param("productId") Long productId);
    
    /**
     * 인기 상품 조회 (판매량 기준)
     */
    @Query("SELECT oi.productId, SUM(oi.quantity) as totalQty FROM OrderItem oi " +
           "GROUP BY oi.productId ORDER BY totalQty DESC")
    List<Object[]> findTopSellingProducts(org.springframework.data.domain.Pageable pageable);
}

