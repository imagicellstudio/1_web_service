package com.xlcfi.product.domain;

public enum ProductStatus {
    DRAFT("임시저장"),
    PUBLISHED("판매중"),
    SOLDOUT("품절"),
    DISCONTINUED("단종");
    
    private final String description;
    
    ProductStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

