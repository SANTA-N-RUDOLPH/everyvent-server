package kr.santanrudolph.everyvent.auth;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.security.Keys;
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
    if (userId == null) {
      throw new IllegalArgumentException("userId cannot be null");
    }

    Date now = new Date();
    Date expirationDate = new Date(now.getTime() + expiration);

    return Jwts.builder()
        .setSubject(String.valueOf(userId))
        .setIssuedAt(now)
        .setExpiration(expirationDate)
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
    Claims claims = parseClaims(token);
    return Long.parseLong(claims.getSubject());
  }

  public boolean validateToken(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("Invalid JWT: {}: {}", e.getClass().getSimpleName(), e.getMessage());
    }
    return false;
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
