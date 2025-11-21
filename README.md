# SpicyJump Web Service

웹기반 K-Food 거래 플랫폼 서비스 프로젝트

## 프로젝트 구조

- `/backend` - 백엔드 코드 (Java Spring Boot, Python Flask, 블록체인)
- `/004.design` - 설계 문서

## 기술 스택

- Backend: Java Spring Boot + Python Flask
- Database: PostgreSQL + Redis
- Blockchain: Solidity (Polygon)
- Payment: TossPayments, NicePay, Stripe

## 주요 기능

- NFT 시스템 (원산지 인증, 레시피, 멤버십)
- 멀티 PG 결제 통합
- 고급 평가 및 게시판 시스템
- GIS 기반 소비자 연결

## 문서

### 설계 문서 (004.design/)
1. 데이터베이스 설계
2. 하이브리드 아키텍처
3. Java API 명세
4. Python API 명세
5. 고급 평가 시스템

### 백엔드 문서 (backend/)
1. 백엔드 구현 마일스톤
2. NFT 구현 완료
3. 멀티 PG 통합
4. 블록체인 토큰 아키텍처

## 개발 환경

### 필수 요구사항
- Java 17+
- Python 3.9+
- PostgreSQL 14+
- Redis 6+
- Node.js 18+ (Hardhat)

### 실행 방법
```bash
# Docker Compose로 실행
cd backend
docker-compose up -d

# Java 서비스 실행
cd backend/java-services
./gradlew bootRun

# Python 서비스 실행
cd backend/python-services/analytics-service
python app.py
```

## 라이선스

Proprietary - All Rights Reserved

