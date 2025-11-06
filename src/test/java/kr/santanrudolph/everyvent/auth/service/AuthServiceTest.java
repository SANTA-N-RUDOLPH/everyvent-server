package kr.santanrudolph.everyvent.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Date;
import kr.santanrudolph.everyvent.auth.dto.TokenResponse;
import kr.santanrudolph.everyvent.auth.security.JwtTokenProvider;
import kr.santanrudolph.everyvent.auth.repository.RedisTokenRepository;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

  @Mock
  JwtTokenProvider jwtTokenProvider;
  @Mock
  RedisTokenRepository redisTokenRepository;
  @InjectMocks
  AuthService authService;

  private static final Long TEST_USER_ID = 1L;
  private static final String TEST_REFRESH_TOKEN = "valid.refresh.token";
  private static final String NEW_ACCESS_TOKEN = "new.access.token";
  private static final String NEW_REFRESH_TOKEN = "new.refresh.token";
  private static final String TEST_ACCESS_TOKEN = "valid.access.token";

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(authService, "accessTokenExpiration", 3600000L);
  }

  @Test
  @DisplayName("리프레시 토큰을 갱신한다.")
  void refreshToken_success() {
    // given
    given(jwtTokenProvider.validateToken(TEST_REFRESH_TOKEN)).willReturn(true);
    given(jwtTokenProvider.getUserIdFromToken(TEST_REFRESH_TOKEN)).willReturn(TEST_USER_ID);
    given(redisTokenRepository.getRefreshToken(TEST_USER_ID)).willReturn(TEST_REFRESH_TOKEN);
    given(jwtTokenProvider.createAccessToken(TEST_USER_ID)).willReturn(NEW_ACCESS_TOKEN);
    given(jwtTokenProvider.createRefreshToken(TEST_USER_ID)).willReturn(NEW_REFRESH_TOKEN);
    given(jwtTokenProvider.getExpirationDate(NEW_REFRESH_TOKEN))
        .willReturn(Date.from(Instant.now().plusSeconds(3600)));

    // when
    TokenResponse result = authService.refreshToken(TEST_REFRESH_TOKEN);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getAccessToken()).isEqualTo(NEW_ACCESS_TOKEN);
    assertThat(result.getRefreshToken()).isEqualTo(NEW_REFRESH_TOKEN);
    assertThat(result.getTokenType()).isEqualTo("Bearer");
    assertThat(result.getExpiresIn()).isEqualTo(3600);
  }

  @Test
  @DisplayName("만료된 리프레시 토큰으로 갱신 시 예외를 던진다.")
  void refreshToken_expired_throwsException() {
    // given
    given(jwtTokenProvider.validateToken(TEST_REFRESH_TOKEN)).willReturn(false);
    given(jwtTokenProvider.isExpired(TEST_REFRESH_TOKEN)).willReturn(true);

    // when & then
    assertThatThrownBy(() -> authService.refreshToken(TEST_REFRESH_TOKEN))
        .isInstanceOf(EveryventException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REFRESH_TOKEN_EXPIRED);
  }

  @Test
  @DisplayName("유효하지 않은 리프레시 토큰으로 갱신 시 예외를 던진다.")
  void refreshToken_invalid_throwsException() {
    // given
    given(jwtTokenProvider.validateToken(TEST_REFRESH_TOKEN)).willReturn(false);
    given(jwtTokenProvider.isExpired(TEST_REFRESH_TOKEN)).willReturn(false);

    // when & then
    assertThatThrownBy(() -> authService.refreshToken(TEST_REFRESH_TOKEN))
        .isInstanceOf(EveryventException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REFRESH_TOKEN);
  }

  // 블랙리스트 처리된 토크, redis 재시작으로 데이터 손실, 이미 로그아웃한 사용자는 redis에 토큰이 없다.
  @Test
  @DisplayName("Redis에 없지만 유효한 형식의 토큰으로 갱신을 시도한다.")
  void refreshToken_notInRedis_throwsException() {
    // given
    given(jwtTokenProvider.validateToken(TEST_REFRESH_TOKEN)).willReturn(true);
    given(jwtTokenProvider.getUserIdFromToken(TEST_REFRESH_TOKEN)).willReturn(TEST_USER_ID);
    given(redisTokenRepository.getRefreshToken(TEST_USER_ID)).willReturn(null);

    // when & then
    assertThatThrownBy(() -> authService.refreshToken(TEST_REFRESH_TOKEN))
        .isInstanceOf(EveryventException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REFRESH_TOKEN);
  }

  @Test
  @DisplayName("로그아웃 시 리프레시 토큰을 삭제하고 액세스 토큰을 블랙리스트에 추가한다.")
  void logout_success() {
    given(jwtTokenProvider.getExpirationDate(TEST_ACCESS_TOKEN))
        .willReturn(Date.from(Instant.now().plusSeconds(3600)));

    authService.logout(TEST_USER_ID, TEST_ACCESS_TOKEN);

    verify(redisTokenRepository).deleteRefreshToken(TEST_USER_ID);
    verify(jwtTokenProvider).getExpirationDate(TEST_ACCESS_TOKEN);
    verify(redisTokenRepository).addToBlacklist(anyString(), any(Instant.class));
  }

  @Test
  @DisplayName("액세스 토큰이 null이면 블랙리스트에 추가하지 않는다.")
  void logout_withNullAccessToken() {
    // when
    authService.logout(TEST_USER_ID, null);

    // then
    verify(redisTokenRepository).deleteRefreshToken(TEST_USER_ID);
    verify(redisTokenRepository, never()).addToBlacklist(anyString(), any(Instant.class));
  }


}