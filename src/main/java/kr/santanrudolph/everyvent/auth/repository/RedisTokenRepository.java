package kr.santanrudolph.everyvent.auth.repository;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class RedisTokenRepository {

  private final RedisTemplate<String, String> redisTemplate;

  // TODO: RedisKeyConstants 상수로 분리
  private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
  private static final String BLACKLIST_TOKEN_PREFIX = "blacklist_token:";

  public RedisTokenRepository(
      @Qualifier("tokenRedisTemplate") RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public void saveRefreshToken(Long userId, String refreshToken, Instant expiresAt) {
    String key = REFRESH_TOKEN_PREFIX + userId;
    long ttl = Duration.between(Instant.now(), expiresAt).getSeconds();

    if (ttl <= 0) {
      throw new EveryventException(ErrorCode.REFRESH_TOKEN_EXPIRED, "RefreshToken을 저장할 수 없습니다.");
    }

    executeRedisOperation(() -> {
      redisTemplate.opsForValue().set(key, refreshToken, ttl, TimeUnit.SECONDS);
      log.info("RefreshToken saved for userId: {}, TTL: {}s", userId, ttl);
      return null;
    }, "RefreshToken을 저장할 수 없습니다.");
  }

  public String getRefreshToken(Long userId) {
    String key = REFRESH_TOKEN_PREFIX + userId;
    return executeRedisOperation(() ->
            redisTemplate.opsForValue().get(key),
        "RefreshToken을 조회할 수 없습니다."
    );
  }

  public void deleteRefreshToken(Long userId) {
    String key = REFRESH_TOKEN_PREFIX + userId;
    executeRedisOperation(() -> {
      redisTemplate.delete(key);
      log.info("RefreshToken deleted for userId: {}", userId);
      return null;
    }, "RefreshToken을 삭제할 수 없습니다.");
  }

  public void addToBlacklist(String token, Instant expiresAt) {
    String key = BLACKLIST_TOKEN_PREFIX + token;
    long ttl = Duration.between(Instant.now(), expiresAt).getSeconds();

    if (ttl <= 0) {
      log.info("Token already expired, skipping blacklist");
      return;
    }

    executeRedisOperation(() -> {
      redisTemplate.opsForValue().set(key, "true", ttl, TimeUnit.SECONDS);
      log.info("Token blacklisted - TTL: {}s", ttl);
      return null;
    }, "Blacklist에 추가할 수 없습니다.");
  }

  public boolean isBlacklisted(String token) {
    String key = BLACKLIST_TOKEN_PREFIX + token;
    return executeRedisOperation(() ->
            Boolean.TRUE.equals(redisTemplate.hasKey(key)),
        "Blacklist를 조회할 수 없습니다."
    );
  }

  private <T> T executeRedisOperation(Supplier<T> operation, String errorMessage) {
    try {
      return operation.get();
    } catch (Exception e) {
      log.error("[REDIS_ERROR] {}: {}", errorMessage, e.getMessage(), e);
      throw new EveryventException(ErrorCode.INTERNAL_SERVER_ERROR, "Redis 오류: " + errorMessage);
    }
  }
}
