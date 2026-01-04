package kr.santanrudolph.everyvent.domain.user;

import kr.santanrudolph.everyvent.auth.CurrentUserProvider;
import kr.santanrudolph.everyvent.domain.follow.FollowRepository;
import kr.santanrudolph.everyvent.domain.user.dto.request.UpdateIntroductionRequest;
import kr.santanrudolph.everyvent.domain.user.dto.request.UpdateNicknameRequest;
import kr.santanrudolph.everyvent.domain.user.dto.response.UserBasicResponse;
import kr.santanrudolph.everyvent.domain.user.dto.response.UserResponse;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import kr.santanrudolph.everyvent.infrastructure.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;
  private final FollowRepository followRepository;
  private final S3Service s3Service;
  private final CurrentUserProvider currentUserProvider;


  public UserResponse getUserInfo(Long userId) {
    User user = getCurrentUser();
    return UserResponse.from(user);
  }

  public UserBasicResponse getUserBasicInfo(Long userId) {
    User user = getActiveUser(userId);
    return UserBasicResponse.from(user);
  }

  public User getCurrentUser() {
    return userRepository.findByIdAndDeletedAtIsNull(currentUserProvider.getCurrentUserId())
        .orElseThrow(() -> new EveryventException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
  }

  @Transactional
  public UserResponse updateIntroduction(UpdateIntroductionRequest request) {
    User user = getCurrentUser();

    user.updateIntroduction(request.getIntroduction());
    log.info("User introduction updated - User ID: {}", user.getId());

    return UserResponse.from(user);
  }

  @Transactional
  public UserResponse updateNickname(UpdateNicknameRequest request) {
    if (userRepository.existsByNickname(request.getNickname())) {
      throw new EveryventException(ErrorCode.ALREADY_EXIST, "이미 사용 중인 닉네임입니다.");
    }

    User user = getCurrentUser();
    try {
      user.updateNickname(request.getNickname());
      userRepository.flush();
    } catch (DataIntegrityViolationException e) {
      throw new EveryventException(ErrorCode.ALREADY_EXIST, "이미 사용 중인 닉네임입니다");
    }

    log.info("User nickname updated - User ID: {}, New nickname: {}",
        user.getId(), request.getNickname());

    return UserResponse.from(user);
  }

  @Transactional
  public void deleteUser() {
    User user = getCurrentUser();

    user.softDelete();

    int deletedFollowCount = followRepository.deleteAllByUserId(user.getId());

    log.info("User soft deleted - User ID: {}, Deleted follow count: {}",
        user.getId(), deletedFollowCount);
  }

  private User getActiveUser(Long userId) {
    return userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new EveryventException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
  }

}
