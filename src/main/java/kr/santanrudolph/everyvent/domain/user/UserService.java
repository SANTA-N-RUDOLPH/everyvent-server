package kr.santanrudolph.everyvent.domain.user;

import kr.santanrudolph.everyvent.auth.CurrentUserProvider;
import kr.santanrudolph.everyvent.domain.follow.FollowRepository;
import kr.santanrudolph.everyvent.domain.user.dto.request.ProfileImageUpdateRequest;
import kr.santanrudolph.everyvent.domain.user.dto.request.UpdateIntroductionRequest;
import kr.santanrudolph.everyvent.domain.user.dto.request.UpdateNicknameRequest;
import kr.santanrudolph.everyvent.domain.user.dto.response.UserBasicResponse;
import kr.santanrudolph.everyvent.domain.user.dto.response.UserResponse;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import kr.santanrudolph.everyvent.domain.user.dto.request.ProfileImageUploadRequest;
import kr.santanrudolph.everyvent.infrastructure.s3.ProfileImageUploadResponse;
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


  public UserResponse getCurrentUserInfo() {
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

    if (user.getProfileImageKey() != null) {
      s3Service.deleteObject(user.getProfileImageKey(), user.getId());
    }
  }

  public ProfileImageUploadResponse generateProfileImageUploadUrl(Long userId, ProfileImageUploadRequest request) {
    return s3Service.generatePresignedResponse(
        userId,
        request.filename(),
        request.contentType(),
        request.fileSize()
    );
  }

  @Transactional
  public UserResponse saveProfileImageKey(ProfileImageUpdateRequest request) {
    User user = getCurrentUser();

    s3Service.validateObjectKey(request.objectKey(), user.getId());

    user.updateProfileImageKey(request.objectKey());
    log.info("Profile image key saved - User ID: {}, Object Key: {}",
        user.getId(), request.objectKey());

    return UserResponse.from(user);
  }

  @Transactional
  public UserResponse deleteProfileImage() {
    User user = getCurrentUser();

    if (user.getProfileImageKey() == null) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "삭제할 프로필 이미지가 없습니다.");
    }

    String keyToDelete = user.getProfileImageKey();
    user.deleteProfileImageKey();
    s3Service.deleteObject(keyToDelete, user.getId());

    log.info("Profile image deleted - User ID: {}", user.getId());

    return UserResponse.from(user);
  }

  private User getActiveUser(Long userId) {
    return userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new EveryventException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
  }

}
