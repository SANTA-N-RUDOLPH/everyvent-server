package kr.santanrudolph.everyvent.auth.service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisTokenRepository {

  private final RedisTemplate<String, String> redisTemplate;

  // TODO: RedisKeyConstants 상수로 분리
  private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
  private static final String BLACKLIST_TOKEN_PREFIX = "blacklist_token:";


  public void saveRefreshToken(Long userId, String refreshToken, Instant expiresAt) {
    String key = REFRESH_TOKEN_PREFIX + userId;
    long ttl = Duration.between(Instant.now(), expiresAt).getSeconds();

    if (ttl > 0) {
      redisTemplate.opsForValue().set(key, refreshToken, ttl, TimeUnit.SECONDS);
      log.info("RefreshToken saved to Redis for userId: {}, TTL: {} seconds", userId, ttl);
    } else {
      log.warn("RefreshToken expiresAt is in the past for userId: {}", userId);
    }
  }

  public String getRefreshToken(Long userId) {
    String key = REFRESH_TOKEN_PREFIX + userId;
    Object value = redisTemplate.opsForValue().get(key);
    return value != null ? value.toString() : null;
  }

  public void deleteRefreshToken(Long userId) {
    String key = REFRESH_TOKEN_PREFIX + userId;
    redisTemplate.delete(key);
    log.info("RefreshToken deleted from Redis for userId: {}", userId);
  }

  public void addToBlacklist(String token, Instant expiresAt) {
    String key = BLACKLIST_TOKEN_PREFIX + token;
    long ttl = Duration.between(Instant.now(), expiresAt).getSeconds();

    if (ttl > 0) {
      redisTemplate.opsForValue().set(key, "true", ttl, TimeUnit.SECONDS);
      log.info("Token added to blacklist with TTL: {} seconds", ttl);
    } else {
      // TODO: 에러처리 필요
      log.warn("Token already expired, skipping blacklist - Expires at: {}", expiresAt);
    }
  }

  public boolean isBlacklisted(String token) {
    String key = BLACKLIST_TOKEN_PREFIX + token;
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
  }
}
