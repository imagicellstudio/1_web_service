package com.xlcfi.product.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Category> children = new ArrayList<>();
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(name = "name_en", length = 200)
    private String nameEn;
    
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // 비즈니스 메서드
    public void update(String name, String nameEn, Integer sortOrder) {
        this.name = name;
        this.nameEn = nameEn;
        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }
    }
    
    public void addChild(Category child) {
        this.children.add(child);
        child.parent = this;
    }
    
    public boolean isTopLevel() {
        return this.parent == null;
    }
    
    public String getFullPath() {
        if (isTopLevel()) {
            return name;
        }
        return parent.getFullPath() + " > " + name;
    }
}

