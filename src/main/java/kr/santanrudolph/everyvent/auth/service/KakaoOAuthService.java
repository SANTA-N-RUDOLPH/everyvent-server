package kr.santanrudolph.everyvent.auth.service;

import kr.santanrudolph.everyvent.auth.security.EveryventOAuth2User;
import kr.santanrudolph.everyvent.domain.user.SocialProvider;
import kr.santanrudolph.everyvent.domain.user.User;
import kr.santanrudolph.everyvent.domain.user.UserRepository;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuthService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);

    String kakaoId = oAuth2User.getName();
    log.info("Kakao Login - Kakao ID: {}", kakaoId);

    Map<String, Object> attributes = oAuth2User.getAttributes();
    @SuppressWarnings("unchecked")
    Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

    String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
    log.info("Kakao Email: {}", email);

    // socialId + provider로 사용자 조회 또는 생성
    User user = userRepository.findBySocialIdAndSocialProviderAndDeletedAtIsNull(kakaoId,
            SocialProvider.KAKAO)
        .orElseGet(() -> createUser(kakaoId, email));

    return new EveryventOAuth2User(user, oAuth2User.getAttributes());
  }

  private User createUser(String socialId, String email) {
    log.info("Creating new user with socialId: {}", socialId);

    // email이 null이 아닌 경우, 중복 체크
    if (email != null && userRepository.findByEmail(email).isPresent()) {
      log.warn("Email already exists: {}", email);
      throw new EveryventException(ErrorCode.ALREADY_EXIST, "이미 존재하는 이메일입니다.");
    }

    // 임시 닉네임 생성
    String tempNickname = generateUniqueNickname(socialId);

    User newUser = User.createFromOAuth(socialId, SocialProvider.KAKAO, email, tempNickname);

    return userRepository.save(newUser);
  }

  private String generateUniqueNickname(String socialId) {
    String baseNickname = SocialProvider.KAKAO.name() + "_" + socialId;
    String nickname = baseNickname;
    int suffix = 1;

    while (userRepository.existsByNickname(nickname)) {
      nickname = baseNickname + "_" + suffix++;
    }

    return nickname;
  }
}