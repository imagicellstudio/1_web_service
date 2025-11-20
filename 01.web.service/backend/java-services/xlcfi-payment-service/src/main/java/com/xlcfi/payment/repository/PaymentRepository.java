package com.xlcfi.payment.repository;

import com.xlcfi.payment.domain.Payment;
import com.xlcfi.payment.domain.PaymentStatus;
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
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * 주문 ID로 결제 조회
     */
    List<Payment> findByOrderId(Long orderId);
    
    /**
     * PG사 거래 ID로 결제 조회
     */
    Optional<Payment> findByPgTransactionId(String pgTransactionId);
    
    /**
     * 상태별 결제 조회 (페이징)
     */
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
    
    /**
     * 주문 ID와 상태로 결제 조회
     */
    Optional<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status);
    
    /**
     * 기간별 결제 통계
     */
    @Query("SELECT COUNT(p), SUM(p.amount) FROM Payment p WHERE " +
           "p.status = :status AND " +
           "p.paidAt BETWEEN :startDate AND :endDate")
    Object[] getPaymentStats(@Param("status") PaymentStatus status,
                             @Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate);
    
    /**
     * 특정 PG사의 결제 내역 조회
     */
    List<Payment> findByPgProvider(String pgProvider);
}

