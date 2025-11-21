from flask import jsonify
from . import health_bp
from datetime import datetime

@health_bp.route('/health', methods=['GET'])
def health_check():
    """헬스 체크 엔드포인트"""
    return jsonify({
        'status': 'healthy',
        'service': 'analytics-service',
        'timestamp': datetime.utcnow().isoformat()
    }), 200

@health_bp.route('/ready', methods=['GET'])
def readiness_check():
    """준비 상태 체크"""
    return jsonify({
        'status': 'ready',
        'service': 'analytics-service'
    }), 200

