package kr.santanrudolph.everyvent.auth.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.security.Keys;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Slf4j
@Component
public class JwtTokenProvider {

  private final SecretKey secretKey;
  private final long accessTokenExpiration;
  private final long refreshTokenExpiration;

  public JwtTokenProvider(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
      @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    if (keyBytes.length < 32) { // hmacShaKey가 검증해주지만 명확한 예외 메시지를 위해 명시
      throw new IllegalArgumentException("JWT secret must be at least 32 bytes");
    }

    this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    this.accessTokenExpiration = accessTokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;
  }

  private String generateToken(Long userId, long expiration) {
    Date now = new Date();
    Date expirationDate = new Date(now.getTime() + expiration);

    return Jwts.builder()
        .claims()
        .subject(String.valueOf(userId))
        .issuedAt(now)
        .expiration(expirationDate)
        .and()
        .signWith(secretKey, SIG.HS256)
        .compact();
  }

  public String createAccessToken(Long userId) {
    return generateToken(userId, accessTokenExpiration);
  }

  public String createRefreshToken(Long userId) {
    return generateToken(userId, refreshTokenExpiration);
  }

  public Long getUserIdFromToken(String token) {
    try {
      Claims claims = parseClaims(token);
      return Long.parseLong(claims.getSubject());
    } catch (ExpiredJwtException e) {
      throw new EveryventException(ErrorCode.ACCESS_TOKEN_EXPIRED, "토큰이 만료되어 userId를 추출할 수 없습니다");
    } catch (NumberFormatException e) {
      throw new EveryventException(ErrorCode.INVALID_ACCESS_TOKEN, "토큰의 userId 형식이 올바르지 않습니다");
    } catch (JwtException | IllegalArgumentException e) {
      throw new EveryventException(ErrorCode.INVALID_ACCESS_TOKEN, "userId 추출 중 토큰 파싱에 실패했습니다");
    }
  }

  public Date getExpirationDate(String token) {
    try {
      return parseClaims(token).getExpiration();
    } catch (ExpiredJwtException e) {
      // 만료된 토큰이라도 만료 시간은 가져올 수 있음
      return e.getClaims().getExpiration();
    } catch (JwtException | IllegalArgumentException e) {
      throw new EveryventException(ErrorCode.INVALID_ACCESS_TOKEN, "만료 시간 추출 중 토큰 파싱에 실패했습니다");
    }
  }

  public boolean validateToken(String token) {
    try {
      Claims claims = parseClaims(token);

      String subject = claims.getSubject();
      if (subject == null || subject.isEmpty()) {
        log.warn("JWT subject is null or empty");
        return false;
      }

      try {
        Long.parseLong(subject);
      } catch (NumberFormatException e) {
        log.warn("JWT subject is not a valid userId: {}", subject);
        return false;
      }

      return true;
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("Invalid JWT: {}: {}", e.getClass().getSimpleName(), e.getMessage());
    }
    return false;
  }

  public boolean isExpired(String token) {
    try {
      parseClaims(token);
      return false;
    } catch (ExpiredJwtException e) {
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  private Claims parseClaims(String token) {
    if (token == null) { // JWT가 내부적으로 처리해주지만 명시
      throw new IllegalArgumentException("Token cannot be null");
    }

    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public String resolveToken(String bearerToken) {
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
