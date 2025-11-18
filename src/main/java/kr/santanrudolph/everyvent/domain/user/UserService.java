package kr.santanrudolph.everyvent.domain.user;

import kr.santanrudolph.everyvent.domain.follow.FollowRepository;
import kr.santanrudolph.everyvent.domain.user.dto.request.UpdateIntroductionRequest;
import kr.santanrudolph.everyvent.domain.user.dto.request.UpdateNicknameRequest;
import kr.santanrudolph.everyvent.domain.user.dto.response.UserResponse;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
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

  public UserResponse getUserInfo(Long userId) {
    User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new EveryventException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

    return UserResponse.from(user);
  }

  @Transactional
  public UserResponse updateIntroduction(Long userId, UpdateIntroductionRequest request) {
    User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new EveryventException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

    user.updateIntroduction(request.getIntroduction());
    log.info("User introduction updated - User ID: {}", userId);

    return UserResponse.from(user);
  }

  @Transactional
  public UserResponse updateNickname(Long userId, UpdateNicknameRequest request) {
    if (userRepository.existsByNickname(request.getNickname())) {
      throw new EveryventException(ErrorCode.ALREADY_EXIST, "이미 사용 중인 닉네임입니다.");
    }

    User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new EveryventException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

    try {
      user.updateNickname(request.getNickname());
      userRepository.flush();
    } catch (DataIntegrityViolationException e) {
      throw new EveryventException(ErrorCode.ALREADY_EXIST, "이미 사용 중인 닉네임입니다");
    }

    log.info("User nickname updated - User ID: {}, New nickname: {}", userId,
        request.getNickname());

    return UserResponse.from(user);
  }

  @Transactional
  public void deleteUser(Long userId) {
    User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new EveryventException(ErrorCode.NOT_FOUND, "탈퇴할 사용자를 찾을 수 없습니다."));

    user.softDelete();

    int deletedFollowCount = followRepository.deleteAllByUserId(userId);

    log.info("User soft deleted - User ID: {}, Deleted follow count: {}",
        userId, deletedFollowCount);
  }
}
