from flask import jsonify, request
from . import analytics_bp
from ..services.analytics_service import AnalyticsService
from ..utils.auth import require_internal_auth
from ..utils.response import success_response, error_response
import logging

logger = logging.getLogger(__name__)
analytics_service = AnalyticsService()

@analytics_bp.route('/dashboard', methods=['GET'])
@require_internal_auth
def get_dashboard_stats():
    """대시보드 통계 조회"""
    try:
        period = request.args.get('period', 'month')
        start_date = request.args.get('start_date')
        end_date = request.args.get('end_date')
        
        stats = analytics_service.get_dashboard_stats(period, start_date, end_date)
        
        return success_response(stats)
    
    except Exception as e:
        logger.error(f"Error getting dashboard stats: {e}", exc_info=True)
        return error_response('ANALYTICS_ERROR', str(e), status=500)

@analytics_bp.route('/users/<int:user_id>/behavior', methods=['GET'])
@require_internal_auth
def get_user_behavior(user_id):
    """사용자 행동 분석"""
    try:
        behavior = analytics_service.analyze_user_behavior(user_id)
        return success_response(behavior)
    
    except Exception as e:
        logger.error(f"Error analyzing user behavior: {e}", exc_info=True)
        return error_response('ANALYTICS_ERROR', str(e), status=500)

