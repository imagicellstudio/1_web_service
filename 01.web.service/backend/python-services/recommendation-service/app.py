#!/usr/bin/env python3
from flask import Flask, jsonify
from flask_cors import CORS
import os

app = Flask(__name__)
CORS(app)

@app.route('/health', methods=['GET'])
def health_check():
    """헬스 체크"""
    return jsonify({
        'status': 'healthy',
        'service': 'recommendation-service'
    }), 200

@app.route('/v1/recommendations/users/<int:user_id>/products', methods=['GET'])
def get_recommendations(user_id):
    """상품 추천"""
    # TODO: 실제 추천 로직 구현
    return jsonify({
        'success': True,
        'data': {
            'user_id': user_id,
            'recommendations': []
        }
    }), 200

if __name__ == '__main__':
    port = int(os.getenv('PORT', 5002))
    app.run(host='0.0.0.0', port=port, debug=True)

