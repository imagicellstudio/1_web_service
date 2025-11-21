from flask import jsonify
from datetime import datetime
from typing import Any, Dict

def success_response(data: Any, message: str = "Success", status: int = 200):
    """성공 응답 생성"""
    return jsonify({
        'success': True,
        'data': data,
        'message': message,
        'timestamp': datetime.utcnow().isoformat()
    }), status

def error_response(
    code: str, 
    message: str, 
    details: Dict = None, 
    status: int = 400
):
    """에러 응답 생성"""
    error_data = {
        'success': False,
        'error': {
            'code': code,
            'message': message
        },
        'timestamp': datetime.utcnow().isoformat()
    }
    
    if details:
        error_data['error']['details'] = details
    
    return jsonify(error_data), status

