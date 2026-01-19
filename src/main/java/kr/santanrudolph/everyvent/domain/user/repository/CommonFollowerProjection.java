package kr.santanrudolph.everyvent.domain.user.repository;

/**
 * 공통 팔로워 배치 조회용 Projection
 * Native Query 결과를 매핑하기 위한 인터페이스
 */
public interface CommonFollowerProjection {
  Long getTargetUserId();      // 누구의 공통 팔로워인지

  Long getUserId();            // 공통 팔로워 ID

  String getNickname();        // 공통 팔로워 닉네임

  String getProfileImageKey(); // 공통 팔로워 프로필 이미지
}
