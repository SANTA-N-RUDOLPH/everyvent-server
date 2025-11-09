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
import kr.santanrudolph.everyvent.auth.security.JwtTokenProvider;
import kr.santanrudolph.everyvent.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  private final AuthService authService;
  private final JwtTokenProvider jwtTokenProvider;

  @Operation(
      summary = "토큰 재발급",
      description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 요청 형식 오류"),
      @ApiResponse(responseCode = "401", description = "INVALID_REFRESH_TOKEN: 유효하지 않은 리프레시 토큰 | REFRESH_TOKEN_EXPIRED: 만료된 리프레시 토큰")
  })
  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refreshToken(
      @Valid @RequestBody TokenRefreshRequest request) {
    TokenResponse response = authService.refreshToken(request.getRefreshToken());
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "로그아웃",
      description = "RefreshToken을 Redis에서 삭제하고 AccessToken을 블랙리스트에 추가합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
      @ApiResponse(responseCode = "401", description = "UNAUTHORIZED: 인증되지 않은 사용자 | INVALID_ACCESS_TOKEN: 유효하지 않은 액세스 토큰"),
      @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: 서버 내부 오류")
  })
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    Long userId = AuthenticationUtil.getCurrentUserId();
    String accessToken = jwtTokenProvider.resolveToken(request.getHeader("Authorization"));

    authService.logout(userId, accessToken);

    return ResponseEntity.ok().build();
  }
}
