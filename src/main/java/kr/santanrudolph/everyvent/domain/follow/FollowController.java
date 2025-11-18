package kr.santanrudolph.everyvent.domain.follow;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.santanrudolph.everyvent.auth.AuthenticationUtil;
import kr.santanrudolph.everyvent.domain.follow.command.FollowCreateCommand;
import kr.santanrudolph.everyvent.domain.follow.dto.FollowCountDto;
import kr.santanrudolph.everyvent.domain.follow.dto.FollowCreateRequest;
import kr.santanrudolph.everyvent.domain.follow.dto.FollowCreateResponse;
import kr.santanrudolph.everyvent.domain.follow.dto.FollowResponse;
import kr.santanrudolph.everyvent.domain.user.UserService;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Tag(name = "팔로우", description = "팔로우 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

  private final FollowService followService;

  @Operation(
      summary = "팔로우 생성",
      description = "다른 사용자를 팔로우합니다. 자기 자신은 팔로우할 수 없습니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "팔로우 생성 성공"),
      @ApiResponse(responseCode = "400", description = "INVALID_INPUT: 자기 자신을 팔로우할 수 없음"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 팔로우 대상 사용자를 찾을 수 없음"),
      @ApiResponse(responseCode = "409", description = "ALREADY_EXIST: 이미 팔로우 중")
  })
  @PostMapping("/me/followings/{targetId}")
  public ResponseEntity<FollowCreateResponse> createFollow(
      @PathVariable Long targetId
  ) {
    Long currentUserId = AuthenticationUtil.getCurrentUserId();

    // 자기 자신 팔로우 방지
    if (currentUserId.equals(targetId)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "자기 자신을 팔로우할 수 없습니다.");
    }

    FollowCreateCommand command = new FollowCreateCommand(currentUserId, targetId);
    FollowCreateResponse response = followService.createFollow(command);

    log.info("Follow created - follower: {}, target: {}", currentUserId, targetId);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }


  @Operation(
      summary = "특정 사용자의 팔로워 목록 조회",
      description = "특정 사용자를 팔로우하는 사용자 목록을 조회합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "팔로워 목록 조회 성공"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자를 찾을 수 없음")
  })
  @GetMapping("/{userId}/followers")
  public ResponseEntity<List<FollowResponse>> getFollowers(
      @PathVariable Long userId
  ) {
    List<FollowResponse> followers = followService.getFollowers(userId);

    log.debug("Followers retrieved for user: {}, count: {}", userId, followers.size());

    return ResponseEntity.ok(followers);
  }

  @Operation(
      summary = "특정 사용자의 팔로잉 목록 조회",
      description = "특정 사용자가 팔로우하는 사용자 목록을 조회합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "팔로잉 목록 조회 성공"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자를 찾을 수 없음")
  })
  @GetMapping("/{userId}/followings")
  public ResponseEntity<List<FollowResponse>> getFollowings(
      @PathVariable Long userId
  ) {
    List<FollowResponse> followings = followService.getFollowings(userId);

    log.debug("Followings retrieved for user: {}, count: {}", userId, followings.size());

    return ResponseEntity.ok(followings);
  }

  @Operation(
      summary = "언팔로우 (팔로잉 제거)",
      description = "내가 팔로우한 사용자를 언팔로우합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "언팔로우 성공"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 팔로워 또는 팔로우 대상 사용자를 찾을 수 없음 | 팔로우 관계가 존재하지 않음")
  })
  @DeleteMapping("/me/followings/{targetId}")
  public ResponseEntity<Void> unfollowUser(
      @PathVariable Long targetId
  ) {
    Long currentUserId = AuthenticationUtil.getCurrentUserId();

    followService.deleteFollow(currentUserId, targetId);

    log.info("Unfollow - follower: {}, target: {}", currentUserId, targetId);

    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "팔로워 제거",
      description = "나를 팔로우하는 사용자를 제거합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "팔로워 제거 성공"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 팔로워 또는 팔로우 대상 사용자를 찾을 수 없음 | 팔로우 관계가 존재하지 않음")
  })
  @DeleteMapping("/me/followers/{followerId}")
  public ResponseEntity<Void> removeFollower(
      @PathVariable Long followerId
  ) {
    Long currentUserId = AuthenticationUtil.getCurrentUserId();

    followService.deleteFollow(followerId, currentUserId);

    log.info("Follower removed - follower: {}, target: {}", followerId, currentUserId);

    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "팔로워/팔로잉 수 조회",
      description = "특정 사용자의 팔로워 수와 팔로잉 수를 조회합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "팔로워/팔로잉 수 조회 성공"),
      @ApiResponse(responseCode = "404", description = "NOT_FOUND: 사용자를 찾을 수 없음")
  })
  @GetMapping("/{userId}/count")
  public ResponseEntity<FollowCountDto> getFollowCount(
      @PathVariable Long userId
  ) {
    FollowCountDto count = followService.getFollowCount(userId);

    log.debug("Follow count retrieved for user: {}, followers: {}, followings: {}",
        userId, count.followerCount(), count.followingCount());

    return ResponseEntity.ok(count);
  }

}
