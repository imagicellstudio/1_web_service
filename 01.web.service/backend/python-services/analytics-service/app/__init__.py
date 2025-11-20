from flask import Flask
from flask_cors import CORS
from .config import Config
import logging

def create_app(config_class=Config):
    """애플리케이션 팩토리"""
    app = Flask(__name__)
    app.config.from_object(config_class)
    
    # CORS 설정
    CORS(app)
    
    # 로깅 설정
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s [%(levelname)s] %(name)s: %(message)s'
    )
    
    # 블루프린트 등록
    from .api import analytics_bp, health_bp
    app.register_blueprint(health_bp)
    app.register_blueprint(analytics_bp, url_prefix='/v1/analytics')
    
    return app

