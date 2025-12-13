package kr.santanrudolph.everyvent.domain.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.santanrudolph.everyvent.auth.AuthenticationUtil;
import kr.santanrudolph.everyvent.domain.user.dto.request.UpdateIntroductionRequest;
import kr.santanrudolph.everyvent.domain.user.dto.request.UpdateNicknameRequest;
import kr.santanrudolph.everyvent.domain.user.dto.response.UserBasicResponse;
import kr.santanrudolph.everyvent.domain.user.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "사용자", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "401", description = "UNAUTHORIZED: 인증되지 않은 요청 | INVALID_ACCESS_TOKEN: 유효하지 않은 액세스 토큰"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자를 찾을 수 없음")
  })
  @GetMapping("/me")
  public ResponseEntity<UserResponse> getMyInfo() {
    Long userId = AuthenticationUtil.getCurrentUserId();
    log.info("Get my info - User ID: {}", userId);

    UserResponse response = userService.getUserInfo(userId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "유저 정보 조회", description = "userId로 사용자의 정보를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "401", description = "UNAUTHORIZED: 인증되지 않은 요청 | INVALID_ACCESS_TOKEN: 유효하지 않은 액세스 토큰"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자를 찾을 수 없음")
  })

  @GetMapping("/{userId}")
  public ResponseEntity<UserBasicResponse> getMyInfo(@PathVariable Long userId) {
    UserBasicResponse response = userService.getUserBasicInfo(userId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "소개글 수정", description = "현재 로그인한 사용자의 소개글을 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수정 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 유효하지 않은 입력값"),
      @ApiResponse(responseCode = "401", description = "UNAUTHORIZED: 인증되지 않은 요청 | INVALID_ACCESS_TOKEN: 유효하지 않은 액세스 토큰"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자를 찾을 수 없음")
  })
  @PatchMapping("/me/introduction")
  public ResponseEntity<UserResponse> updateIntroduction(
      @Valid @RequestBody UpdateIntroductionRequest request) {
    Long userId = AuthenticationUtil.getCurrentUserId();
    log.info("Update introduction - User ID: {}", userId);

    UserResponse response = userService.updateIntroduction(userId, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "닉네임 수정", description = "현재 로그인한 사용자의 닉네임을 수정합니다. 임시 닉네임(kakao_123456789)을 원하는 닉네임으로 변경할 수 있습니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수정 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 유효하지 않은 입력값"),
      @ApiResponse(responseCode = "401", description = "UNAUTHORIZED: 인증되지 않은 요청 | INVALID_ACCESS_TOKEN: 유효하지 않은 액세스 토큰"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자를 찾을 수 없음"),
      @ApiResponse(responseCode = "409", description = "ALREADY_EXIST: 이미 사용 중인 닉네임")
  })
  @PatchMapping("/me/nickname")
  public ResponseEntity<UserResponse> updateNickname(
      @Valid @RequestBody UpdateNicknameRequest request) {
    Long userId = AuthenticationUtil.getCurrentUserId();
    log.info("Update nickname - User ID: {}", userId);

    UserResponse response = userService.updateNickname(userId, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자를 탈퇴 처리합니다. (Soft Delete)")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "탈퇴 성공"),
      @ApiResponse(responseCode = "401", description = "UNAUTHORIZED: 인증되지 않은 요청 | INVALID_ACCESS_TOKEN: 유효하지 않은 액세스 토큰"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자를 찾을 수 없음")
  })
  @DeleteMapping("/me")
  public ResponseEntity<Void> deleteUser() {
    Long userId = AuthenticationUtil.getCurrentUserId();
    log.info("User deletion requested - User ID: {}", userId);

    userService.deleteUser(userId);

    return ResponseEntity.noContent().build();
  }

}
