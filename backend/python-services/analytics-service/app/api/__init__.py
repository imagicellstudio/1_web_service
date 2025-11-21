from flask import Blueprint

# 블루프린트 생성
analytics_bp = Blueprint('analytics', __name__)
health_bp = Blueprint('health', __name__)

# 라우트 임포트
from . import analytics, health

