package kr.santanrudolph.everyvent.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.santanrudolph.everyvent.auth.dto.TokenResponse;
import kr.santanrudolph.everyvent.auth.security.JwtTokenProvider;
import kr.santanrudolph.everyvent.domain.user.User;
import kr.santanrudolph.everyvent.domain.user.UserRepository;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[개발용] 인증", description = "개발 환경에서만 사용 가능한 테스트용 인증 API")
@Slf4j
@Profile("dev") // dev 프로파일에서만 활성화
@RestController
@RequestMapping("/api/dev/auth")
@RequiredArgsConstructor
public class DevAuthController {

  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;

  @Operation(
      summary = "[개발용] userId로 즉시 로그인",
      description = "userId를 받아서 JWT 토큰을 즉시 발급합니다. 개발 환경에서만 사용 가능합니다."
  )
  @GetMapping("/login/{userId}")
  public ResponseEntity<TokenResponse> loginByUserId(
      @Parameter(description = "사용자 ID", example = "1", required = true)
      @PathVariable("userId") Long userId) {
    log.info("[DEV] 개발용 로그인 요청: userId={}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EveryventException(ErrorCode.NOT_FOUND, "존재하지 않는 유저입니다."));

    if (user.isDeleted()) {
      throw new EveryventException(ErrorCode.NOT_FOUND, "탈퇴한 유저입니다.");
    }

    String accessToken = jwtTokenProvider.createAccessToken(userId);
    String refreshToken = jwtTokenProvider.createRefreshToken(userId);

    log.info("[DEV] 토큰 발급 완료: userId={}, nickname={}", userId, user.getNickname());

    TokenResponse response = TokenResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(3600L)
        .build();

    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "[개발용] 이메일로 즉시 로그인",
      description = "이메일을 받아서 해당 유저의 JWT 토큰을 즉시 발급합니다. 개발 환경에서만 사용 가능합니다."
  )
  @GetMapping("/login/email/{email}")
  public ResponseEntity<TokenResponse> loginByEmail(@PathVariable("email") String email) {
    log.info("[DEV] 개발용 로그인 요청: email={}", email);

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new EveryventException(ErrorCode.NOT_FOUND));

    if (user.isDeleted()) {
      throw new EveryventException(ErrorCode.NOT_FOUND);
    }

    String accessToken = jwtTokenProvider.createAccessToken(user.getId());
    String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

    log.info("[DEV] 토큰 발급 완료: email={}, userId={}, nickname={}", email, user.getId(),
        user.getNickname());

    TokenResponse response = TokenResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(3600L)
        .build();

    return ResponseEntity.ok(response);
  }
}