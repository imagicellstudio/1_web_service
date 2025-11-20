import os
from dotenv import load_dotenv

load_dotenv()

class Config:
    """기본 설정"""
    
    # Flask
    SECRET_KEY = os.getenv('SECRET_KEY', 'dev-secret-key-change-in-production')
    DEBUG = os.getenv('FLASK_DEBUG', 'False').lower() == 'true'
    
    # Database
    DATABASE_URL = os.getenv(
        'DATABASE_URL',
        'postgresql://xlcfi_dev:dev_password@localhost:5432/xlcfi_dev'
    )
    
    # Redis
    REDIS_HOST = os.getenv('REDIS_HOST', 'localhost')
    REDIS_PORT = int(os.getenv('REDIS_PORT', 6379))
    REDIS_DB = int(os.getenv('REDIS_DB', 0))
    
    # Kafka
    KAFKA_BOOTSTRAP_SERVERS = os.getenv('KAFKA_BOOTSTRAP_SERVERS', 'localhost:9092')
    
    # Java Services
    JAVA_PRODUCT_SERVICE_URL = os.getenv(
        'JAVA_PRODUCT_SERVICE_URL',
        'http://localhost:8082'
    )
    
    # Internal Auth
    INTERNAL_JWT_SECRET = os.getenv(
        'INTERNAL_JWT_SECRET',
        'internal-secret-key-change-in-production'
    )
    
    # Cache TTL (seconds)
    CACHE_DEFAULT_TTL = int(os.getenv('CACHE_DEFAULT_TTL', 300))

class DevelopmentConfig(Config):
    """개발 환경 설정"""
    DEBUG = True

class ProductionConfig(Config):
    """운영 환경 설정"""
    DEBUG = False

class TestConfig(Config):
    """테스트 환경 설정"""
    TESTING = True
    DATABASE_URL = 'sqlite:///:memory:'

config = {
    'development': DevelopmentConfig,
    'production': ProductionConfig,
    'test': TestConfig,
    'default': DevelopmentConfig
}

