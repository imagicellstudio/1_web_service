package com.xlcfi.auth.domain;

public enum UserRole {
    BUYER("구매자"),
    SELLER("판매자"),
    ADMIN("관리자");
    
    private final String description;
    
    UserRole(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

