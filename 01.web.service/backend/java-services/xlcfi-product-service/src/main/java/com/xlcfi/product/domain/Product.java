package com.xlcfi.product.domain;

import com.xlcfi.common.entity.BaseEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at IS NULL")
public class Product extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Column(nullable = false, length = 500)
    private String name;
    
    @Column(name = "name_en", length = 500)
    private String nameEn;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;
    
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";
    
    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> images;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProductStatus status = ProductStatus.DRAFT;
    
    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;
    
    @Column(name = "rating_average", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal ratingAverage = BigDecimal.ZERO;
    
    @Column(name = "review_count", nullable = false)
    @Builder.Default
    private Integer reviewCount = 0;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    // 비즈니스 메서드
    public void update(String name, String nameEn, String description, 
                      String descriptionEn, BigDecimal price, 
                      Integer stockQuantity, ProductStatus status) {
        this.name = name;
        this.nameEn = nameEn;
        this.description = description;
        this.descriptionEn = descriptionEn;
        if (price != null) {
            this.price = price;
        }
        if (stockQuantity != null) {
            this.stockQuantity = stockQuantity;
        }
        if (status != null) {
            this.status = status;
        }
    }
    
    public void updateCategory(Category category) {
        this.category = category;
    }
    
    public void updateImages(List<String> images) {
        this.images = images;
    }
    
    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }
    
    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new IllegalStateException("재고가 부족합니다");
        }
        this.stockQuantity -= quantity;
        if (this.stockQuantity == 0) {
            this.status = ProductStatus.SOLDOUT;
        }
    }
    
    public void publish() {
        if (this.status == ProductStatus.DRAFT) {
            this.status = ProductStatus.PUBLISHED;
        }
    }
    
    public void markAsSoldOut() {
        this.status = ProductStatus.SOLDOUT;
    }
    
    public void discontinue() {
        this.status = ProductStatus.DISCONTINUED;
    }
    
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.status = ProductStatus.DISCONTINUED;
    }
    
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
    
    public boolean isPublished() {
        return this.status == ProductStatus.PUBLISHED;
    }
    
    public boolean isInStock() {
        return this.stockQuantity > 0;
    }
    
    public void incrementViewCount() {
        this.viewCount++;
    }
    
    public void updateRating(BigDecimal averageRating, Integer reviewCount) {
        this.ratingAverage = averageRating;
        this.reviewCount = reviewCount;
    }
}

