package com.xlcfi.auth.domain;

import com.xlcfi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(name = "password_hash", length = 255)
    private String passwordHash;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 20)
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.BUYER;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private Language language = Language.KO;
    
    @Column(name = "marketing_consent")
    @Builder.Default
    private Boolean marketingConsent = false;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    // 비즈니스 메서드
    public void updateProfile(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }
    
    public void updateLanguage(Language language) {
        this.language = language;
    }
    
    public void updatePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }
    
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }
    
    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }
    
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }
    
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }
    
    public boolean isSeller() {
        return this.role == UserRole.SELLER;
    }
    
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }
}

