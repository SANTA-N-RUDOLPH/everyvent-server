package kr.santanrudolph.everyvent.domain.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.santanrudolph.everyvent.auth.CurrentUserProvider;
import kr.santanrudolph.everyvent.domain.user.dto.request.ProfileImageUpdateRequest;
import kr.santanrudolph.everyvent.domain.user.dto.request.UpdateIntroductionRequest;
import kr.santanrudolph.everyvent.domain.user.dto.request.UpdateNicknameRequest;
import kr.santanrudolph.everyvent.domain.user.dto.response.UserBasicResponse;
import kr.santanrudolph.everyvent.domain.user.dto.response.UserResponse;
import kr.santanrudolph.everyvent.domain.user.dto.request.ProfileImageUploadRequest;
import kr.santanrudolph.everyvent.infrastructure.s3.ProfileImageUploadResponse;
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
  private final CurrentUserProvider currentUserProvider;

  @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "401", description = "UNAUTHORIZED: 인증되지 않은 요청 | INVALID_ACCESS_TOKEN: 유효하지 않은 액세스 토큰"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자를 찾을 수 없음")
  })
  @GetMapping("/me")
  public ResponseEntity<UserResponse> getMyInfo() {
    Long userId = currentUserProvider.getCurrentUserId();
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
    Long userId = currentUserProvider.getCurrentUserId();
    log.info("Update introduction - User ID: {}", userId);

    UserResponse response = userService.updateIntroduction(request);
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
    Long userId = currentUserProvider.getCurrentUserId();
    log.info("Update nickname - User ID: {}", userId);

    UserResponse response = userService.updateNickname(request);
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
    Long userId = currentUserProvider.getCurrentUserId();
    log.info("User deletion requested - User ID: {}", userId);

    userService.deleteUser();

    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "프로필 이미지 업로드 URL 발급", description = """
      프로필 이미지 업로드를 위한 Presigned URL을 발급합니다. fileSize의 단위는 byte입니다.
      """)
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "발급 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 유효하지 않은 입력값 (파일 크기, 확장자, Content-Type)"),
      @ApiResponse(responseCode = "401", description = "UNAUTHORIZED: 인증되지 않은 요청 | INVALID_ACCESS_TOKEN: 유효하지 않은 액세스 토큰")
  })
  @PostMapping("/me/profile/image/upload-url")
  public ResponseEntity<ProfileImageUploadResponse> generateProfileImageUploadUrl(
      @Valid @RequestBody ProfileImageUploadRequest request) {
    Long userId = currentUserProvider.getCurrentUserId();
    log.info("Generate profile image upload URL - User ID: {}, Filename: {}", userId, request.filename());

    ProfileImageUploadResponse response =
        userService.generateProfileImageUploadUrl(userId, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "프로필 이미지 저장", description = "S3 업로드 후 받은 objectKey를 DB에 저장합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "저장 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 유효하지 않은 objectKey (경로 탐색 공격, 권한 없음)"),
      @ApiResponse(responseCode = "401", description = "UNAUTHORIZED: 인증되지 않은 요청 | INVALID_ACCESS_TOKEN: 유효하지 않은 액세스 토큰"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자를 찾을 수 없음")
  })
  @PatchMapping("/me/profile/image")
  public ResponseEntity<UserResponse> saveProfileImage(
      @Valid @RequestBody ProfileImageUpdateRequest request) {
    Long userId = currentUserProvider.getCurrentUserId();
    log.info("Save profile image - User ID: {}, Object Key: {}", userId, request.objectKey());

    UserResponse response = userService.saveProfileImageKey(request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "프로필 이미지 삭제", description = "현재 로그인한 사용자의 프로필 이미지를 삭제합니다. (S3 + DB)")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "삭제 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 삭제할 프로필 이미지가 없음"),
      @ApiResponse(responseCode = "401", description = "UNAUTHORIZED: 인증되지 않은 요청 | INVALID_ACCESS_TOKEN: 유효하지 않은 액세스 토큰"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자를 찾을 수 없음")
  })
  @DeleteMapping("/me/profile/image")
  public ResponseEntity<UserResponse> deleteProfileImage() {
    Long userId = currentUserProvider.getCurrentUserId();
    log.info("Delete profile image - User ID: {}", userId);

    UserResponse response = userService.deleteProfileImage();
    return ResponseEntity.ok(response);
  }

}
