from functools import wraps
from flask import request, jsonify
import jwt
import os

INTERNAL_SECRET = os.getenv('INTERNAL_JWT_SECRET', 'internal-secret-key-change-in-production')

def require_internal_auth(f):
    """내부 서비스 인증 데코레이터"""
    @wraps(f)
    def decorated(*args, **kwargs):
        token = request.headers.get('X-Internal-Token', '')
        
        if not token:
            return jsonify({
                'success': False,
                'error': {
                    'code': 'MISSING_TOKEN',
                    'message': 'Internal token required'
                }
            }), 401
        
        try:
            jwt.decode(token, INTERNAL_SECRET, algorithms=['HS256'])
        except jwt.InvalidTokenError as e:
            return jsonify({
                'success': False,
                'error': {
                    'code': 'INVALID_TOKEN',
                    'message': f'Invalid internal token: {str(e)}'
                }
            }), 401
        
        return f(*args, **kwargs)
    
    return decorated

def generate_internal_token():
    """내부 서비스 토큰 생성"""
    from datetime import datetime, timedelta
    
    payload = {
        'sub': 'internal-service',
        'role': 'INTERNAL_SERVICE',
        'iat': datetime.utcnow(),
        'exp': datetime.utcnow() + timedelta(hours=1)
    }
    
    return jwt.encode(payload, INTERNAL_SECRET, algorithm='HS256')

