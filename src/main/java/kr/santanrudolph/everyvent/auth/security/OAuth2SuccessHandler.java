package kr.santanrudolph.everyvent.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import kr.santanrudolph.everyvent.auth.repository.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private static final String REDIRECT_URI_SESSION_KEY = "OAUTH2_REDIRECT_URI";

  private final JwtTokenProvider jwtTokenProvider;
  private final RedisTokenRepository redisTokenRepository;
  private final RedirectUriValidator redirectUriValidator;

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
    String redirectUri = getValidatedRedirectUri(request);
    String targetUrl = UriComponentsBuilder.fromUriString(redirectUri + "/oauth/callback")
        .queryParam("code", authCode)
        .build().toUriString();

    // OAuth2 로그인 완료 후 불필요한 세션 데이터 제거
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.removeAttribute(REDIRECT_URI_SESSION_KEY);
    }

    log.info("Redirecting to: {}", targetUrl);
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

  private String getValidatedRedirectUri(HttpServletRequest request) {
    HttpSession session = request.getSession(false);

    if (session != null) {
      String storedUri = (String) session.getAttribute(REDIRECT_URI_SESSION_KEY);

      // 세션에서 꺼낸 uri 다시 검증
      if (storedUri != null && redirectUriValidator.isAllowed(storedUri)) {
        log.info("Using redirect URI from session: {}", storedUri);
        return storedUri;
      }
    }

    // Fallback(uri 파싱 실패): 기본값 사용
    String defaultUri = redirectUriValidator.getDefaultRedirectUri();
    log.warn("Session redirect URI missing or invalid, using default: {}", defaultUri);
    return defaultUri;
  }
}
