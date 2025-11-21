package com.xlcfi.payment.repository;

import com.xlcfi.payment.domain.Payment;
import com.xlcfi.payment.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * 주문 ID로 결제 조회
     */
    Optional<Payment> findByOrderId(Long orderId);
    
    /**
     * PG 트랜잭션 ID로 결제 조회
     */
    Optional<Payment> findByPgTransactionId(String pgTransactionId);
    
    /**
     * 주문 ID와 상태로 결제 존재 여부 확인
     */
    boolean existsByOrderIdAndStatusIn(Long orderId, List<PaymentStatus> statuses);
}
