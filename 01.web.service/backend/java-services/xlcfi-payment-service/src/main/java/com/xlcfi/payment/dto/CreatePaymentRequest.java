package com.xlcfi.payment.dto;

import com.xlcfi.payment.domain.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequest {

    @NotNull(message = "주문 ID는 필수입니다")
    private Long orderId;

    @NotNull(message = "결제 금액은 필수입니다")
    @DecimalMin(value = "0.0", inclusive = false, message = "결제 금액은 0보다 커야 합니다")
    private BigDecimal amount;

    @NotBlank(message = "통화는 필수입니다")
    @Size(min = 3, max = 3, message = "통화 코드는 3자여야 합니다")
    private String currency;

    @NotNull(message = "결제 수단은 필수입니다")
    private PaymentMethod paymentMethod;

    private String pgProvider;
}

