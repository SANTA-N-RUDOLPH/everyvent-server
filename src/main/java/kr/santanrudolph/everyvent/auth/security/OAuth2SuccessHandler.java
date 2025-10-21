package kr.santanrudolph.everyvent.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import kr.santanrudolph.everyvent.auth.entity.RefreshToken;
import kr.santanrudolph.everyvent.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenRepository refreshTokenRepository;

  @Override
  @Transactional
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {
    EveryventOAuth2User oAuth2User = (EveryventOAuth2User) authentication.getPrincipal();
    Long userId = oAuth2User.getUserId();

    log.info("OAuth2 Login Success - User ID: {}", userId);

    String accessToken = jwtTokenProvider.createAccessToken(userId);
    String refreshToken = jwtTokenProvider.createRefreshToken(userId);
    Instant refreshTokenExpiration = jwtTokenProvider.getExpirationDate(refreshToken).toInstant();

    // RefreshToken DB 저장 (기존 토큰이 있으면 업데이트)
    RefreshToken refreshTokenEntity = refreshTokenRepository.findById(userId)
        .orElse(RefreshToken.builder()
            .userId(userId)
            .token(refreshToken)
            .expiresAt(refreshTokenExpiration)
            .build());

    if (refreshTokenEntity.getToken() != null) {
      refreshTokenEntity.updateToken(refreshToken, refreshTokenExpiration);
    }

    refreshTokenRepository.save(refreshTokenEntity);
    log.info("RefreshToken saved for User ID: {}", userId);

    // 테스트용 콜백 엔드포인트로 리다이렉트 (토큰을 쿼리 파라미터로 전달하여, 로그인 시 프론트 없이도 토큰 확인 가능)
    // 프론트 연동 시에는 삭제 필요
    String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:8080/oauth/callback")
        .queryParam("accessToken", accessToken)
        .queryParam("refreshToken", refreshToken)
        .build().toUriString();

    log.info("Redirecting to: {}", targetUrl);
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }
}