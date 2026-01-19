package kr.santanrudolph.everyvent.global.config;

import kr.santanrudolph.everyvent.domain.user.User;
import kr.santanrudolph.everyvent.domain.user.repository.UserRepository;
import kr.santanrudolph.everyvent.domain.user.enums.Role;
import kr.santanrudolph.everyvent.domain.user.enums.SocialProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev") // dev 프로파일에서만 실행
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

  private final UserRepository userRepository;

  @Override
  public void run(String... args) {
    // 이미 데이터가 있으면 생성하지 않음
    if (userRepository.count() > 1) {
      log.info("테스트 유저가 이미 존재합니다. 데이터 로딩을 건너뜁니다.");
      return;
    }

    log.info("테스트 유저 데이터를 생성합니다...");

    // 일반 유저 3명
    User user1 = new User(
        "kakao_12345",
        SocialProvider.KAKAO,
        "santa@example.com",
        "산타클로스",
        "크리스마스를 사랑하는 산타입니다 🎅",
        Role.USER
    );

    User user2 = new User(
        "google_67890",
        SocialProvider.GOOGLE,
        "rudolph@example.com",
        "루돌프",
        "빨간 코를 가진 루돌프예요!",
        Role.USER
    );

    User user3 = new User(
        "naver_11111",
        SocialProvider.NAVER,
        "snowman@example.com",
        "눈사람",
        "겨울이 좋아요 ⛄",
        Role.USER
    );

    // 관리자 유저 1명
    User admin = new User(
        "kakao_99999",
        SocialProvider.KAKAO,
        "admin@example.com",
        "관리자",
        "EveryVent 관리자입니다",
        Role.ADMIN
    );

    userRepository.save(user1);
    userRepository.save(user2);
    userRepository.save(user3);
    userRepository.save(admin);

    log.info("테스트 유저 데이터 생성 완료!");
    log.info("생성된 유저: santa@example.com, rudolph@example.com, snowman@example.com, admin@example.com");
  }
}
