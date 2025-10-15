package kr.santanrudolph.everyvent.domain.user;

import kr.santanrudolph.everyvent.domain.user.dto.request.UpdateIntroductionRequest;
import kr.santanrudolph.everyvent.domain.user.dto.request.UpdateNicknameRequest;
import kr.santanrudolph.everyvent.domain.user.dto.response.UserResponse;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;

  public UserResponse getUserInfo(Long userId) {
    User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new EveryventException(ErrorCode.USER_NOT_FOUND));

    return UserResponse.from(user);
  }

  @Transactional
  public UserResponse updateIntroduction(Long userId, UpdateIntroductionRequest request) {
    User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new EveryventException(ErrorCode.USER_NOT_FOUND));

    user.updateIntroduction(request.getIntroduction());
    log.info("User introduction updated - User ID: {}", userId);

    return UserResponse.from(user);
  }

  @Transactional
  public UserResponse updateNickname(Long userId, UpdateNicknameRequest request) {
    if (userRepository.existsByNickname(request.getNickname())) {
      throw new EveryventException(ErrorCode.DUPLICATE_NICKNAME);
    }

    User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new EveryventException(ErrorCode.USER_NOT_FOUND));

    user.updateNickname(request.getNickname());
    log.info("User nickname updated - User ID: {}, New nickname: {}", userId,
        request.getNickname());

    return UserResponse.from(user);
  }
}
