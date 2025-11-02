package kr.santanrudolph.everyvent.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import kr.santanrudolph.everyvent.auth.AuthenticationUtil;
import kr.santanrudolph.everyvent.auth.dto.TokenRefreshRequest;
import kr.santanrudolph.everyvent.auth.dto.TokenResponse;
import kr.santanrudolph.everyvent.auth.security.JwtTokenProvider;
import kr.santanrudolph.everyvent.auth.service.TokenStoreService;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "인증", description = "인증 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final TokenStoreService tokenStoreService;
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
  public ResponseEntity<TokenResponse> refreshToken(
      @Valid @RequestBody TokenRefreshRequest request) {

    String requestRefreshToken = request.getRefreshToken();

    // 토큰 형식 검증
    if (!jwtTokenProvider.validateToken(requestRefreshToken)) {
      log.warn("Invalid refresh token");

      if (jwtTokenProvider.isExpired(requestRefreshToken)) {
        throw new EveryventException(ErrorCode.EXPIRED_TOKEN);
      }
      throw new EveryventException(ErrorCode.INVALID_TOKEN);
    }

    // Redis에서 RefreshToken 확인
    Long userId = jwtTokenProvider.getUserIdFromToken(requestRefreshToken);
    String storedRefreshToken = tokenStoreService.getRefreshToken(userId);

    if (storedRefreshToken == null || !storedRefreshToken.equals(requestRefreshToken)) {
      log.warn("RefreshToken not found in Redis or mismatch - User ID: {}", userId);
      throw new EveryventException(ErrorCode.INVALID_TOKEN);
    }

    // 새로운 토큰 발급
    String newAccessToken = jwtTokenProvider.createAccessToken(userId);
    String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);
    Instant newRefreshTokenExpiration = jwtTokenProvider.getExpirationDate(newRefreshToken)
        .toInstant();

    // Redis에 새로운 RefreshToken 저장
    tokenStoreService.saveRefreshToken(userId, newRefreshToken, newRefreshTokenExpiration);

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
      description = "RefreshToken을 Redis에서 삭제하고 AccessToken을 블랙리스트에 추가합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
      @ApiResponse(responseCode = "400", description = "비즈니스 에러 (에러코드로 구분: USER_NOT_FOUND, UNAUTHORIZED 등)")
  })
  @PostMapping("/logout")
  public ResponseEntity<String> logout(HttpServletRequest request) {
    Long userId = AuthenticationUtil.getCurrentUserId();

    String bearerToken = request.getHeader("Authorization");
    String accessToken = jwtTokenProvider.resolveToken(bearerToken);

    // Redis에서 RefreshToken 삭제
    tokenStoreService.deleteRefreshToken(userId);

    // AccessToken을 블랙리스트에 추가
    if (accessToken != null) {
      Instant accessTokenExpiration = jwtTokenProvider.getExpirationDate(accessToken).toInstant();
      tokenStoreService.addToBlacklist(accessToken, accessTokenExpiration);
    }

    log.info("User logged out - User ID: {}, RefreshToken deleted, AccessToken blacklisted",
        userId);

    return ResponseEntity.ok("Logged out successfully");
  }
}
