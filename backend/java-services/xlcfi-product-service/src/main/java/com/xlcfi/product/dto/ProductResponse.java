package com.xlcfi.product.dto;

import com.xlcfi.product.domain.Product;
import com.xlcfi.product.domain.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long id;
    private Long sellerId;
    private String sellerName;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String nameEn;
    private String description;
    private String descriptionEn;
    private BigDecimal price;
    private String currency;
    private Integer stockQuantity;
    private List<String> images;
    private ProductStatus status;
    private Integer viewCount;
    private BigDecimal ratingAverage;
    private Integer reviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sellerId(product.getSeller() != null ? product.getSeller().getId() : null)
                .sellerName(product.getSeller() != null ? product.getSeller().getName() : null)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .name(product.getName())
                .nameEn(product.getNameEn())
                .description(product.getDescription())
                .descriptionEn(product.getDescriptionEn())
                .price(product.getPrice())
                .currency(product.getCurrency())
                .stockQuantity(product.getStockQuantity())
                .images(product.getImages())
                .status(product.getStatus())
                .viewCount(product.getViewCount())
                .ratingAverage(product.getRatingAverage())
                .reviewCount(product.getReviewCount())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}

