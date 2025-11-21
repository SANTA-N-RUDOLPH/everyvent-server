package kr.santanrudolph.everyvent.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import kr.santanrudolph.everyvent.auth.repository.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final RedisTokenRepository redisTokenRepository;

  @Value("${frontend.url:http://localhost:3030}")
  private String frontendUrl;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {
    EveryventOAuth2User oAuth2User = (EveryventOAuth2User) authentication.getPrincipal();
    Long userId = oAuth2User.getUserId();

    log.info("OAuth2 Login Success - User ID: {}", userId);

    // JWT 토큰 생성
    String accessToken = jwtTokenProvider.createAccessToken(userId);
    String refreshToken = jwtTokenProvider.createRefreshToken(userId);
    Instant refreshTokenExpiration = jwtTokenProvider.getExpirationDate(refreshToken).toInstant();

    redisTokenRepository.saveRefreshToken(userId, refreshToken, refreshTokenExpiration);
    log.info("RefreshToken saved to Redis for User ID: {}", userId);

    // 일회성 인증 코드 생성 (보안 강화)
    String authCode = UUID.randomUUID().toString();
    redisTokenRepository.saveAuthCode(authCode, accessToken, refreshToken);
    log.info("AuthCode generated for User ID: {}", userId);

    // 프론트엔드로 인증 코드만 전달 (토큰은 URL에 노출되지 않음)
    String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth/callback")
        .queryParam("code", authCode)
        .build().toUriString();

    log.info("Redirecting to: {}", targetUrl);
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }
}