package com.xlcfi.product.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    private Long categoryId;

    @NotBlank(message = "상품명은 필수입니다")
    @Size(max = 500, message = "상품명은 500자 이하여야 합니다")
    private String name;

    @Size(max = 500, message = "영문 상품명은 500자 이하여야 합니다")
    private String nameEn;

    @NotBlank(message = "상품 설명은 필수입니다")
    private String description;

    private String descriptionEn;

    @NotNull(message = "가격은 필수입니다")
    @DecimalMin(value = "0.0", inclusive = false, message = "가격은 0보다 커야 합니다")
    private BigDecimal price;

    @NotBlank(message = "통화는 필수입니다")
    @Size(min = 3, max = 3, message = "통화 코드는 3자여야 합니다")
    private String currency;

    @NotNull(message = "재고 수량은 필수입니다")
    @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다")
    private Integer stockQuantity;

    private List<String> images;
}

