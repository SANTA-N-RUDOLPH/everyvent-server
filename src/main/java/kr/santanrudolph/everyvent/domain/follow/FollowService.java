package kr.santanrudolph.everyvent.domain.follow;


import java.util.List;

import kr.santanrudolph.everyvent.auth.CurrentUserProvider;
import kr.santanrudolph.everyvent.domain.follow.dto.FollowCountDto;
import kr.santanrudolph.everyvent.domain.follow.dto.FollowResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import kr.santanrudolph.everyvent.domain.follow.dto.FollowCreateResponse;
import kr.santanrudolph.everyvent.domain.user.User;
import kr.santanrudolph.everyvent.domain.user.UserRepository;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {

  private final FollowRepository followRepository;
  private final UserRepository userRepository;
  private final CurrentUserProvider currentUserProvider;


  @Transactional
  public FollowCreateResponse createFollow(Long targetId) {
    Long followerId = currentUserProvider.getCurrentUserId();
    if (followerId.equals(targetId)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "자기 자신을 팔로우할 수 없습니다.");
    }

    User follower = userRepository.findByIdAndDeletedAtIsNull(followerId)
        .orElseThrow(() -> new EveryventException(
            ErrorCode.NOT_FOUND, "현재 유저를 찾을 수 없습니다."));

    User target = userRepository.findByIdAndDeletedAtIsNull(targetId)
        .orElseThrow(() -> new EveryventException(
            ErrorCode.NOT_FOUND, "팔로우 대상 id를 찾을 수 없습니다."));

    if (followRepository.existsByFollowerIdAndTargetId(followerId, targetId)) {
      throw new EveryventException(ErrorCode.ALREADY_EXIST, "이미 팔로우 중입니다.");
    }

    Follow follow = Follow.create(follower, target);
    Follow saved = followRepository.save(follow);

    log.info("Follow created - follower: {}, target: {}, follow: {}",
        followerId, targetId, follow.getId());

    return FollowCreateResponse.from(saved);
  }

  public FollowCountDto getFollowCount(Long userId) {
    userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new EveryventException(
            ErrorCode.NOT_FOUND, "팔로우 정보를 찾을 유저가 존재하지 않습니다.")
        );

    int followerCount = getFollowerCount(userId);
    int followingCount = getFollowingCount(userId);

    return new FollowCountDto(followerCount, followingCount);
  }

  int getFollowerCount(Long targetId) {
    return followRepository.countActiveFollowersByTargetId(targetId);
  }

  int getFollowingCount(Long followerId) {
    return followRepository.countActiveFollowingsByFollowerId(followerId);
  }

  public List<FollowResponse> getFollowers(Long targetId) {

    userRepository.findByIdAndDeletedAtIsNull(targetId)
        .orElseThrow(() -> new EveryventException(
            ErrorCode.NOT_FOUND,
            "팔로워 정보를 가져올 대상 사용자가 존재하지 않습니다. (ID: " + targetId + ")"
        ));

    return followRepository.findActiveFollowersByTargetId(targetId);

  }

  public List<FollowResponse> getFollowings(Long followerId) {

    userRepository.findByIdAndDeletedAtIsNull(followerId)
        .orElseThrow(() -> new EveryventException(
            ErrorCode.NOT_FOUND,
            "팔로잉 정보를 가져올 대상 사용자가 존재하지 않습니다. (ID: " + followerId + ")"
        ));

    return followRepository.findActiveFollowingsByFollowerId(followerId);

  }

  @Transactional
  public void deleteFollower(Long followerId) {
    Long targetId = currentUserProvider.getCurrentUserId();
    deleteFollow(followerId, targetId);
  }

  @Transactional
  public void deleteTarget(Long targetId) {
    Long followerId = currentUserProvider.getCurrentUserId();
    deleteFollow(followerId, targetId);
  }

  @Transactional
  void deleteFollow(Long followerId, Long targetId) {
    if (!userRepository.existsByIdAndDeletedAtIsNull(followerId)) {
      throw new EveryventException(
          ErrorCode.NOT_FOUND,
          "삭제할 팔로워 사용자를 찾을 수 없습니다. (ID: " + followerId + ")"
      );
    }

    if (!userRepository.existsByIdAndDeletedAtIsNull(targetId)) {
      throw new EveryventException(
          ErrorCode.NOT_FOUND,
          "삭제할 팔로우 대상 사용자를 찾을 수 없습니다. (ID: " + targetId + ")"
      );
    }

    int deletedCount = followRepository.deleteByFollowerIdAndTargetId(followerId, targetId);

    if (deletedCount == 0) {
      throw new EveryventException(
          ErrorCode.NOT_FOUND,
          "삭제할 팔로우 관계가 존재하지 않습니다."
      );
    }

    log.info("Follow deleted - follower: {}, target: {}", followerId, targetId);
  }

  public boolean isFollowing(Long followerId, Long targetId) {
    return followRepository.existsByFollowerIdAndTargetId(followerId, targetId);
  }

  public boolean isMutualFollow(Long userId1, Long userId2) {
    return isFollowing(userId1, userId2) && isFollowing(userId2, userId1);
  }

}
