package kr.santanrudolph.everyvent.auth.service;

import java.time.Instant;
import kr.santanrudolph.everyvent.auth.dto.TokenResponse;
import kr.santanrudolph.everyvent.auth.security.JwtTokenProvider;
import kr.santanrudolph.everyvent.auth.repository.RedisTokenRepository;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final JwtTokenProvider jwtTokenProvider;
  private final RedisTokenRepository redisTokenRepository;

  @Value("${jwt.access-token-expiration}")
  private long accessTokenExpiration;


  public TokenResponse refreshToken(String requestRefreshToken) {
    if (!jwtTokenProvider.validateToken(requestRefreshToken)) {
      log.warn("Invalid refresh token");

      if (jwtTokenProvider.isExpired(requestRefreshToken)) {
        throw new EveryventException(ErrorCode.REFRESH_TOKEN_EXPIRED);
      }
      throw new EveryventException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    Long userId = jwtTokenProvider.getUserIdFromToken(requestRefreshToken);
    String storedRefreshToken = redisTokenRepository.getRefreshToken(userId);

    if (storedRefreshToken == null || !storedRefreshToken.equals(requestRefreshToken)) {
      log.warn("RefreshToken not found in Redis or mismatch - User ID: {}", userId);
      throw new EveryventException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    String newAccessToken = jwtTokenProvider.createAccessToken(userId);
    String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);
    Instant newRefreshTokenExpiration = jwtTokenProvider.getExpirationDate(newRefreshToken)
        .toInstant();

    redisTokenRepository.saveRefreshToken(userId, newRefreshToken, newRefreshTokenExpiration);

    log.info("Token refreshed - User ID: {}", userId);

    return TokenResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .tokenType("Bearer")
        .expiresIn(toSeconds(accessTokenExpiration))
        .build();
  }

  public void logout(Long userId, String accessToken) {
    redisTokenRepository.deleteRefreshToken(userId);

    if (accessToken != null) {
      Instant accessTokenExpiration = jwtTokenProvider.getExpirationDate(accessToken).toInstant();
      redisTokenRepository.addToBlacklist(accessToken, accessTokenExpiration);
    }

    log.info("User logged out - User ID: {}, RefreshToken deleted, AccessToken blacklisted",
        userId);
  }

  /**
   * 일회성 인증 코드를 토큰으로 교환 (OAuth2 로그인 콜백용)
   * @param authCode 일회성 인증 코드
   * @return TokenResponse (accessToken, refreshToken)
   */
  public TokenResponse exchangeAuthCode(String authCode) {
    String[] tokens = redisTokenRepository.getAndDeleteAuthCode(authCode);

    if (tokens == null || tokens.length != 2) {
      log.warn("AuthCode not found or expired: {}", authCode);
      throw new EveryventException(ErrorCode.INVALID_INPUT, "유효하지 않거나 만료된 인증 코드입니다.");
    }

    String accessToken = tokens[0];
    String refreshToken = tokens[1];

    log.info("AuthCode exchanged successfully");

    return TokenResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(toSeconds(accessTokenExpiration))
        .build();
  }

  private long toSeconds(long milliseconds) {
    return milliseconds / 1000;
  }

}
