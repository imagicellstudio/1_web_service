package com.xlcfi.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 토큰 블랙리스트 관리 서비스
 * Redis를 사용하여 로그아웃된 토큰을 블랙리스트에 추가하고 검증합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    private static final String BLACKLIST_PREFIX = "blacklist:token:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh:token:";

    /**
     * 토큰을 블랙리스트에 추가 (로그아웃 시)
     * 
     * @param accessToken Access Token
     * @param refreshToken Refresh Token
     */
    public void addToBlacklist(String accessToken, String refreshToken) {
        try {
            // Access Token 블랙리스트 추가 (남은 유효 시간만큼 보관)
            long accessTokenExpiration = jwtTokenProvider.getExpirationTime(accessToken);
            if (accessTokenExpiration > 0) {
                String accessKey = BLACKLIST_PREFIX + accessToken;
                redisTemplate.opsForValue().set(accessKey, "true", accessTokenExpiration, TimeUnit.MILLISECONDS);
                log.info("Access Token이 블랙리스트에 추가되었습니다: expiration={}ms", accessTokenExpiration);
            }

            // Refresh Token 블랙리스트 추가
            long refreshTokenExpiration = jwtTokenProvider.getExpirationTime(refreshToken);
            if (refreshTokenExpiration > 0) {
                String refreshKey = BLACKLIST_PREFIX + refreshToken;
                redisTemplate.opsForValue().set(refreshKey, "true", refreshTokenExpiration, TimeUnit.MILLISECONDS);
                log.info("Refresh Token이 블랙리스트에 추가되었습니다: expiration={}ms", refreshTokenExpiration);
            }

        } catch (Exception e) {
            log.error("토큰 블랙리스트 추가 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     * 
     * @param token 확인할 토큰
     * @return 블랙리스트에 있으면 true
     */
    public boolean isBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("블랙리스트 확인 실패: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Refresh Token 저장 (선택적)
     * Refresh Token을 Redis에 저장하여 관리할 수 있습니다.
     * 
     * @param userId 사용자 ID
     * @param refreshToken Refresh Token
     * @param expirationMs 만료 시간 (밀리초)
     */
    public void saveRefreshToken(Long userId, String refreshToken, long expirationMs) {
        try {
            String key = REFRESH_TOKEN_PREFIX + userId;
            redisTemplate.opsForValue().set(key, refreshToken, expirationMs, TimeUnit.MILLISECONDS);
            log.debug("Refresh Token이 Redis에 저장되었습니다: userId={}", userId);
        } catch (Exception e) {
            log.error("Refresh Token 저장 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * Refresh Token 조회
     * 
     * @param userId 사용자 ID
     * @return Refresh Token (없으면 null)
     */
    public String getRefreshToken(Long userId) {
        try {
            String key = REFRESH_TOKEN_PREFIX + userId;
            Object token = redisTemplate.opsForValue().get(key);
            return token != null ? token.toString() : null;
        } catch (Exception e) {
            log.error("Refresh Token 조회 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Refresh Token 삭제
     * 
     * @param userId 사용자 ID
     */
    public void deleteRefreshToken(Long userId) {
        try {
            String key = REFRESH_TOKEN_PREFIX + userId;
            redisTemplate.delete(key);
            log.debug("Refresh Token이 삭제되었습니다: userId={}", userId);
        } catch (Exception e) {
            log.error("Refresh Token 삭제 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 특정 사용자의 모든 세션 무효화
     * (강제 로그아웃, 계정 정지 등)
     * 
     * @param userId 사용자 ID
     */
    public void invalidateAllSessions(Long userId) {
        try {
            deleteRefreshToken(userId);
            log.info("사용자의 모든 세션이 무효화되었습니다: userId={}", userId);
        } catch (Exception e) {
            log.error("세션 무효화 실패: {}", e.getMessage(), e);
        }
    }
}

