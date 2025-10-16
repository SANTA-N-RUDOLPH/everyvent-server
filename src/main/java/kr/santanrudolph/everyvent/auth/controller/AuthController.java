package kr.santanrudolph.everyvent.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.santanrudolph.everyvent.auth.AuthenticationUtil;
import kr.santanrudolph.everyvent.auth.dto.TokenRefreshRequest;
import kr.santanrudolph.everyvent.auth.dto.TokenResponse;
import kr.santanrudolph.everyvent.auth.entity.RefreshToken;
import kr.santanrudolph.everyvent.auth.security.JwtTokenProvider;
import kr.santanrudolph.everyvent.auth.entity.BlacklistedToken;
import kr.santanrudolph.everyvent.auth.repository.BlacklistedTokenRepository;
import kr.santanrudolph.everyvent.auth.repository.RefreshTokenRepository;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "인증", description = "인증 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final RefreshTokenRepository refreshTokenRepository;
  private final BlacklistedTokenRepository blacklistedTokenRepository;
  private final JwtTokenProvider jwtTokenProvider;

  @Value("${jwt.access-token-expiration}")
  private long accessTokenExpiration;
  @Value("${jwt.refresh-token-expiration}")
  private long refreshTokenExpiration;

  @Operation(
      summary = "토큰 재발급",
      description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
      @ApiResponse(responseCode = "400", description = "비즈니스 에러 (에러코드로 구분: INVALID_TOKEN, TOKEN_EXPIRED, INVALID_INPUT_VALUE 등)")
  })
  @PostMapping("/refresh")
  @Transactional
  public ResponseEntity<TokenResponse> refreshToken(
      @Valid @RequestBody TokenRefreshRequest request) {

    String requestRefreshToken = request.getRefreshToken();

    if (!jwtTokenProvider.validateToken(requestRefreshToken)) {
      log.warn("Invalid refresh token");

      if (jwtTokenProvider.isExpired(requestRefreshToken)) {
        refreshTokenRepository.findByToken(requestRefreshToken)
            .ifPresent(token -> {
              log.info("Deleting expired token - User ID: {}", token.getUserId());
              refreshTokenRepository.delete(token);
            });
        throw new EveryventException(ErrorCode.EXPIRED_TOKEN);
      }
      throw new EveryventException(ErrorCode.INVALID_TOKEN);
    }

    RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
        .orElseThrow(() -> new EveryventException(ErrorCode.INVALID_TOKEN));

    Long userId = jwtTokenProvider.getUserIdFromToken(requestRefreshToken);
    String newAccessToken = jwtTokenProvider.createAccessToken(userId);
    String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);

    refreshToken.updateToken(
        newRefreshToken,
        LocalDateTime.now().plusSeconds(refreshTokenExpiration)
    );

    log.info("Token refreshed - User ID: {}", userId);

    return ResponseEntity.ok(TokenResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .tokenType("Bearer")
        .expiresIn(accessTokenExpiration / 1000)
        .build());
  }

  @Operation(
      summary = "로그아웃",
      description = "RefreshToken을 DB에서 삭제하고 AccessToken을 블랙리스트에 추가합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
      @ApiResponse(responseCode = "400", description = "비즈니스 에러 (에러코드로 구분: USER_NOT_FOUND, UNAUTHORIZED 등)")
  })
  @PostMapping("/logout")
  @Transactional
  public ResponseEntity<String> logout(HttpServletRequest request) {
    Long userId = AuthenticationUtil.getCurrentUserId();

    String bearerToken = request.getHeader("Authorization");
    String accessToken = jwtTokenProvider.resolveToken(bearerToken);

    refreshTokenRepository.deleteByUserId(userId);

    if (accessToken != null) {
      BlacklistedToken blacklist = BlacklistedToken.builder()
          .token(accessToken)
          .expiresAt(LocalDateTime.now().plusHours(1))
          .build();
      blacklistedTokenRepository.save(blacklist);
    }

    log.info("User logged out - User ID: {}, RefreshToken deleted, AccessToken blacklisted",
        userId);

    return ResponseEntity.ok("Logged out successfully");
  }
}
