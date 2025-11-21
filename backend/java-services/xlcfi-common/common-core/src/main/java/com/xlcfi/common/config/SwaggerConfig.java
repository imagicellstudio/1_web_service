package com.xlcfi.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 설정
 */
@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name:XLCfi Platform}")
    private String applicationName;

    @Bean
    public OpenAPI openAPI() {
        // Security Scheme 정의 (JWT)
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("JWT 토큰을 입력하세요 (Bearer 접두사 자동 추가)");

        // Security Requirement 정의
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .info(apiInfo())
                .servers(serverList())
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme))
                .addSecurityItem(securityRequirement);
    }

    /**
     * API 정보
     */
    private Info apiInfo() {
        return new Info()
                .title("XLCfi Platform API")
                .description("""
                        ## XLCfi K-Food 플랫폼 REST API 문서
                        
                        이 API는 K-Food 원료, 원산지, 음식, 요리방법, 레시피 등을 소개하고 거래할 수 있는 플랫폼입니다.
                        
                        ### 인증 방법
                        1. `/api/auth/login` 엔드포인트로 로그인
                        2. 응답으로 받은 `accessToken` 복사
                        3. 우측 상단 🔒 Authorize 버튼 클릭
                        4. 토큰 입력 후 Authorize
                        
                        ### 주요 기능
                        - 회원가입 및 로그인 (JWT 기반)
                        - 상품 조회, 검색, 필터링
                        - 주문 생성 및 관리
                        - 결제 처리
                        - 리뷰 작성 및 관리
                        
                        ### 역할 (Role)
                        - **BUYER**: 구매자 (상품 구매, 리뷰 작성)
                        - **SELLER**: 판매자 (상품 등록, 주문 관리)
                        - **ADMIN**: 관리자 (모든 권한)
                        """)
                .version("v1.0.0")
                .contact(new Contact()
                        .name("XLCfi Platform Team")
                        .email("support@xlcfi.com")
                        .url("https://xlcfi.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0"));
    }

    /**
     * 서버 목록
     */
    private List<Server> serverList() {
        return List.of(
                new Server()
                        .url("http://localhost:8081")
                        .description("Auth Service (개발 환경)"),
                new Server()
                        .url("http://localhost:8082")
                        .description("Product Service (개발 환경)"),
                new Server()
                        .url("http://localhost:8083")
                        .description("Order Service (개발 환경)"),
                new Server()
                        .url("http://localhost:8084")
                        .description("Payment Service (개발 환경)"),
                new Server()
                        .url("http://localhost:8085")
                        .description("Review Service (개발 환경)"),
                new Server()
                        .url("https://api.xlcfi.com")
                        .description("Production Server")
        );
    }
}

