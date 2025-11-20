.PHONY: help install build up down logs clean test

help:
	@echo "XLCfi Platform - 개발 명령어"
	@echo ""
	@echo "사용법: make [command]"
	@echo ""
	@echo "Commands:"
	@echo "  install       - 의존성 설치"
	@echo "  build         - Docker 이미지 빌드"
	@echo "  up            - 서비스 시작 (인프라만)"
	@echo "  up-all        - 모든 서비스 시작"
	@echo "  up-java       - Java 서비스 시작"
	@echo "  up-python     - Python 서비스 시작"
	@echo "  down          - 서비스 중지"
	@echo "  logs          - 로그 확인"
	@echo "  clean         - 컨테이너 및 볼륨 삭제"
	@echo "  test          - 테스트 실행"
	@echo "  ps            - 실행 중인 서비스 확인"

install:
	@echo "Installing dependencies..."
	cd java-services && ./gradlew build -x test
	cd python-services/analytics-service && pip install -r requirements.txt
	cd python-services/recommendation-service && pip install -r requirements.txt

build:
	@echo "Building Docker images..."
	docker-compose build

up:
	@echo "Starting infrastructure services (Postgres, Redis, Kafka, Elasticsearch)..."
	docker-compose up -d postgres redis zookeeper kafka elasticsearch

up-all:
	@echo "Starting all services..."
	docker-compose --profile java-services --profile python-services up -d

up-java:
	@echo "Starting Java services..."
	docker-compose --profile java-services up -d

up-python:
	@echo "Starting Python services..."
	docker-compose --profile python-services up -d

down:
	@echo "Stopping all services..."
	docker-compose --profile java-services --profile python-services down

logs:
	docker-compose --profile java-services --profile python-services logs -f

logs-java:
	docker-compose logs -f auth-service product-service

logs-python:
	docker-compose logs -f analytics-service recommendation-service

ps:
	docker-compose --profile java-services --profile python-services ps

clean:
	@echo "Cleaning up containers and volumes..."
	docker-compose --profile java-services --profile python-services down -v
	docker system prune -f

test:
	@echo "Running tests..."
	cd java-services && ./gradlew test
	cd python-services/analytics-service && pytest

# 개발 모드 (인프라만 실행, 서비스는 IDE에서 실행)
dev:
	@echo "Starting development mode (infrastructure only)..."
	docker-compose up -d postgres redis zookeeper kafka elasticsearch
	@echo ""
	@echo "Infrastructure is ready!"
	@echo "You can now run services from your IDE:"
	@echo "  - Java services: Run SpringBoot applications"
	@echo "  - Python services: python app.py"

