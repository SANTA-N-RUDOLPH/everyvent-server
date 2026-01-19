package kr.santanrudolph.everyvent.domain.user;

import kr.santanrudolph.everyvent.auth.CurrentUserProvider;
import kr.santanrudolph.everyvent.domain.follow.FollowService;
import kr.santanrudolph.everyvent.domain.follow.FollowStatus;
import kr.santanrudolph.everyvent.domain.user.dto.response.UserBasicResponse;
import kr.santanrudolph.everyvent.domain.user.dto.response.UserSearchResponse;
import kr.santanrudolph.everyvent.domain.user.repository.CommonFollowerProjection;
import kr.santanrudolph.everyvent.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSearchService {

  private static final int SEARCH_LIMIT = 100;

  private final UserRepository userRepository;
  private final FollowService followService;
  private final CurrentUserProvider currentUserProvider;


  public List<UserSearchResponse> searchUsers(String keyword) {
    Long currentUserId = currentUserProvider.getCurrentUserId();
    Pageable pageable = PageRequest.of(0, SEARCH_LIMIT);

    List<User> relatedUsers = userRepository.searchRelatedUsers(currentUserId, keyword, pageable);
    List<User> unRelatedUsers = userRepository.searchUnrelatedUsers(currentUserId, keyword, pageable);

    List<User> allCandidateUsers = new ArrayList<>();
    allCandidateUsers.addAll(relatedUsers);
    allCandidateUsers.addAll(unRelatedUsers);

    log.debug("Batch fetching data for {} candidates ({} related, {} unrelated)",
        allCandidateUsers.size(), relatedUsers.size(), unRelatedUsers.size());

    List<Long> relatedUserIds = relatedUsers.stream().map(User::getId).collect(Collectors.toList());
    List<Long> allCandidateIds = allCandidateUsers.stream().map(User::getId).collect(Collectors.toList());

    FollowRelationContext followRelationContext = fetchFollowRelationContext(
        currentUserId,
        relatedUserIds,
        allCandidateIds);

    List<UserSearchResponse> responses = buildSearchResults(
        allCandidateUsers,
        keyword,
        followRelationContext
    );

    log.info("Returning {} search results for keyword: {}", responses.size(), keyword);
    return responses;
  }

  private FollowRelationContext fetchFollowRelationContext(
      Long currentUserId,
      List<Long> relatedUserIds,
      List<Long> allCandidateIds
  ) {
    // 1. 관계 점수를 위한 내 팔로잉/팔로워 정보 조회
    Set<Long> myFollowings = relatedUserIds.isEmpty()
        ? Set.of()
        : followService.findFollowingIds(currentUserId, relatedUserIds);
    Set<Long> myFollowers = relatedUserIds.isEmpty()
        ? Set.of()
        : followService.findFollowerIds(currentUserId, relatedUserIds);

    // 2. 인기 점수를 위한 팔로워 수 조회
    Map<Long, Integer> followerCountMap = followService.getFollowerCountMap(allCandidateIds);

    // 3. 공통 팔로워 목록 조회 -> "@@님, ##님 외 여러 명"을 위함
    List<CommonFollowerProjection> commonFollowerProjections = allCandidateIds.isEmpty()
        ? List.of()
        : userRepository.findCommonFollowersBatch(currentUserId, allCandidateIds);
    Map<Long, List<UserBasicResponse>> commonFollowersMap = groupCommonFollowers(commonFollowerProjections);

    return new FollowRelationContext(
        myFollowings,
        myFollowers,
        followerCountMap,
        commonFollowersMap
    );
  }

  private List<UserSearchResponse> buildSearchResults(
      List<User> candidates,
      String keyword,
      FollowRelationContext followRelationContext
  ) {
    return candidates.stream()
        .map(user -> buildSearchResult(user, keyword, followRelationContext))
        .sorted(Comparator.comparingDouble(UserSearchResponse::score).reversed())
        .limit(100)
        .collect(Collectors.toList());
  }

  private UserSearchResponse buildSearchResult(
      User targetUser,
      String keyword,
      FollowRelationContext followRelationContext
  ) {
    Long targetId = targetUser.getId();
    FollowStatus status = followService.getFollowStatus(
        followRelationContext.myFollowings.contains(targetId),
        followRelationContext.myFollowers.contains(targetId)
    );
    int followerCount = followRelationContext.followerCountMap.getOrDefault(targetId, 0);
    List<UserBasicResponse> commonFollowers = followRelationContext.commonFollowersMap.getOrDefault(targetId,
        List.of());
    long commonFollowersCount = commonFollowers.size();

    double totalScore = calculateTotalScore(targetUser, keyword, status, followerCount);

    return UserSearchResponse.of(
        targetUser,
        status,
        commonFollowers,
        commonFollowersCount,
        totalScore
    );
  }

  private double calculateTextScore(User user, String keyword) {
    String lowerKeyword = keyword.toLowerCase();
    String nickname = user.getNickname().toLowerCase();

    if (nickname.equals(lowerKeyword)) return 100;
    if (nickname.startsWith(lowerKeyword)) return 80;
    if (nickname.contains(lowerKeyword)) return 60;

    return 0;
  }

  private double calculateFollowScore(FollowStatus followStatus) {
    if (followStatus == FollowStatus.MUTUAL) return 50;
    if (followStatus == FollowStatus.FOLLOWING) return 30;
    if (followStatus == FollowStatus.FOLLOWER) return 20;

    return 0;
  }

  private double calculatePopularityScore(int followerCount) {
    return Math.min(50, Math.log10(followerCount + 1) * 10);
  }

  private double calculateTotalScore(
      User target,
      String keyword,
      FollowStatus followStatus,
      int followerCount
  ) {
    double textScore = calculateTextScore(target, keyword);
    double followScore = calculateFollowScore(followStatus);
    double popularityScore = calculatePopularityScore(followerCount);

    return textScore * 1.5 + followScore + popularityScore * 0.5;
  }

  /**
   * Batch 조회 결과를 Map으로 변환 (targetUserId -> 공통팔로워 최대 3명)
   * DB에서 이미 각 타겟당 3개씩만 조회하므로, 단순 그룹핑만 수행
   */
  private Map<Long, List<UserBasicResponse>> groupCommonFollowers(List<CommonFollowerProjection> commonFollowerProjections) {
    return commonFollowerProjections.stream()
        .collect(Collectors.groupingBy(
            CommonFollowerProjection::getTargetUserId,
            Collectors.mapping(
                batch -> new UserBasicResponse(
                    batch.getUserId(),
                    batch.getNickname(),
                    batch.getProfileImageKey(),
                    null  // introduction은 공통 팔로워 정보에서 불필요
                ),
                Collectors.toList()
            )
        ));
  }

  private record FollowRelationContext(
      Set<Long> myFollowings,
      Set<Long> myFollowers,
      Map<Long, Integer> followerCountMap,
      Map<Long, List<UserBasicResponse>> commonFollowersMap
  ) {
  }

}
