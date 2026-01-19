package kr.santanrudolph.everyvent.domain.user.repository;


import kr.santanrudolph.everyvent.domain.user.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserRepositoryCustom {

  /**
   * 관계 있는 사용자 검색 (팔로우/팔로워 우선)
   */
  List<User> searchRelatedUsers(Long currentUserId, String keyword, Pageable pageable);

  /**
   * 관계 없는 사용자 검색
   */
  List<User> searchUnrelatedUsers(Long currentUserId, String keyword, Pageable pageable);

  // findCommonFollowersBatch는 UserRepository에서 Native Query로 직접 정의 (Window Function 사용)

  /**
   * 공통 팔로워 개수 일괄 조회 (Batch)
   */
  List<CommonFollowerCountProjection> countCommonFollowersBatch(Long currentUserId, List<Long> targetUserIds);

}
