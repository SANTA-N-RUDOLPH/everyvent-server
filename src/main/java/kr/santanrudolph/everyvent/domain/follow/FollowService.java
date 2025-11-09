package kr.santanrudolph.everyvent.domain.follow;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import kr.santanrudolph.everyvent.domain.follow.dto.FollowCreateCommand;
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


  @Transactional
  public FollowCreateResponse createFollow(FollowCreateCommand command) {
    User follower = userRepository.findByIdAndDeletedAtIsNull(command.followerId())
        .orElseThrow(() -> new EveryventException(
            ErrorCode.NOT_FOUND, "팔로워 id를 찾을 수 없습니다."));
    User target = userRepository.findByIdAndDeletedAtIsNull(command.targetId())
        .orElseThrow(() -> new EveryventException(
            ErrorCode.NOT_FOUND, "팔로우 대상 id를 찾을 수 없습니다."));
    if (followRepository.existsByFollowerIdAndTargetId(
        command.followerId(),
        command.targetId()
    )) {
      throw new EveryventException(ErrorCode.ALREADY_EXIST, "이미 팔로우 중입니다.");
    }

    Follow follow = Follow.create(follower, target);
    Follow saved = followRepository.save(follow);

    log.info("Follow created - follower: {}, target: {}, follow: {}",
        command.followerId(), command.targetId(), follow.getId());

    return FollowCreateResponse.from(saved);
  }

}
