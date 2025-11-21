# 004.architecture - 시스템 아키텍처 설계 문서

이 폴더는 XLCfi Platform (SpicyJump) 웹 서비스의 **시스템 아키텍처 및 기술 설계 문서**를 포함합니다.

> **참고:** UI/UX 그래픽 디자인 문서는 `007.design/` 폴더를 참조하세요.

---

## 📋 문서 목록

### 1. 데이터베이스 설계
- **`01_database_design.md`** - 데이터베이스 설계서 (ERD, 테이블 정의, 제약조건)
- **`02_table_lists.md`** - 테이블 목록 및 관계

### 2. API 명세
- **`03_api_specs_phase1.md`** - Phase 1 API 명세
- **`11_java_api_specs_detailed.md`** - Java Spring Boot API 상세 명세
- **`11_java_based_api_specs_v2.md`** - Java API v2
- **`12_python_api_specs_detailed.md`** - Python Flask API 상세 명세

### 3. 아키텍처 설계
- **`05_architecture_defin.md`** - 아키텍처 정의
- **`09_java_spring_boot_techstack_defin.md`** - Java Spring Boot 기술 스택
- **`10_hybrid_architecture_design.md`** - 하이브리드 아키텍처 (Java + Python)
- **`10_hybrid_msa_architecture_design.md`** - 마이크로서비스 아키텍처

### 4. 기능 명세
- **`06_function_specs.md`** - 기능 명세서 (유스케이스, 시퀀스 다이어그램)

### 5. 인터페이스 설계
- **`04_interface_design.md`** - 인터페이스 설계
- **`07_user_interface_design.md`** - 사용자 인터페이스 설계
- **`08_admin_interface_design.md`** - 관리자 인터페이스 설계

### 6. 서비스 통신
- **`13_service_communication_sequences.md`** - 서비스 간 통신 시퀀스

### 7. 고급 기능 설계
- **`14_advanced_evaluation_system.md`** - 고급 평가 시스템 (라벨링, 지수, GIS)

---

## 📁 폴더 구조 구분

### `004.architecture/` (이 폴더)
**시스템 아키텍처 및 기술 설계**
- 데이터베이스 스키마
- API 명세서
- 시스템 아키텍처
- 기술 스택 정의
- 서비스 간 통신
- 비즈니스 로직 설계

### `007.design/`
**UI/UX 그래픽 디자인**
- 반응형 웹 디자인
- 모바일 UI/UX
- 컴포넌트 디자인
- 스타일 가이드
- 와이어프레임
- 프로토타입

---

## 🔗 관련 문서

### 백엔드 구현 문서
- [Backend Implementation Milestone](../backend/BACKEND_IMPLEMENTATION_MILESTONE.md)
- [Database Schema Summary](../backend/DATABASE_SCHEMA_SUMMARY.md)
- [Service Layer Summary](../backend/SERVICE_LAYER_SUMMARY.md)
- [Controller Layer Summary](../backend/CONTROLLER_LAYER_SUMMARY.md)

### 프론트엔드 디자인 문서
- [Frontend Responsive Design](../007.design/01_frontend_responsive_design.md)

---

## 📊 설계 개요

### 시스템 구성
```
┌─────────────────────────────────────────────────────────┐
│                   XLCfi Platform                         │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  Frontend (Next.js)                                      │
│  ├── Web (Responsive)                                    │
│  ├── Mobile                                              │
│  └── Tablet                                              │
│                                                           │
│  Backend (Hybrid Architecture)                           │
│  ├── Java Spring Boot (Main)                            │
│  │   ├── Auth Service                                    │
│  │   ├── Product Service                                 │
│  │   ├── Order Service                                   │
│  │   ├── Payment Service                                 │
│  │   └── Review Service                                  │
│  │                                                        │
│  └── Python Flask (Data/ML)                             │
│      ├── Analytics Service                               │
│      ├── Recommendation Service                          │
│      └── ML Service                                      │
│                                                           │
│  Blockchain (Solidity)                                   │
│  ├── XLCFI Token (ERC-20)                               │
│  ├── Origin Certificate NFT                              │
│  ├── Recipe NFT                                          │
│  └── Membership NFT                                      │
│                                                           │
│  Database & Infrastructure                               │
│  ├── PostgreSQL (+ PostGIS)                             │
│  ├── Redis                                               │
│  ├── Elasticsearch                                       │
│  └── Kafka                                               │
│                                                           │
└─────────────────────────────────────────────────────────┘
```

### 주요 기술 스택
- **Backend**: Java 17, Spring Boot 3.2, Python 3.9, Flask
- **Database**: PostgreSQL 15, Redis 7, Elasticsearch 8
- **Blockchain**: Solidity 0.8.20, Hardhat, Polygon
- **Payment**: TossPayments, NicePay, Stripe
- **Frontend**: Next.js 14, TypeScript, Tailwind CSS (설계 예정)

### 핵심 기능
1. **사용자 관리** - 회원가입, 로그인, JWT 인증
2. **상품 관리** - 상품 등록, 조회, 원산지 추적
3. **주문/결제** - 멀티 PG 결제, 주문 관리
4. **리뷰/평가** - 리뷰 작성, 시각적 반응 시스템
5. **블록체인** - P2P 거래, NFT 발행
6. **고급 평가** - 4가지 지수, GIS 기반 소비자 연결

---

## 📝 문서 작성 규칙

### 파일명 규칙
- 번호 + 설명: `01_database_design.md`
- 소문자, 언더스코어 사용
- 명확하고 구체적인 이름

### 문서 구조
1. 제목 및 개요
2. 목차
3. 상세 내용
4. 다이어그램/표
5. 예제 코드
6. 참고 문서

### 버전 관리
- 주요 변경 시 문서 상단에 변경 이력 기록
- Git 커밋 메시지로 변경 사항 추적

---

## 🔄 업데이트 이력

| 날짜 | 버전 | 변경 내용 |
|------|------|-----------|
| 2025-11-21 | 1.0 | 폴더명 변경 (004.design → 004.architecture) |
| 2025-11-21 | 1.0 | README 문서 추가 |

---

## 📧 문의

기술 문서 관련 문의: entra55@gmail.com

**프로젝트:** XLCfi Platform (SpicyJump)  
**Repository:** https://github.com/imagicellstudio/1_web_service


