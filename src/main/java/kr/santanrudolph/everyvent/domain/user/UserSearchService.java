package kr.santanrudolph.everyvent.domain.user;

import kr.santanrudolph.everyvent.domain.follow.FollowService;
import kr.santanrudolph.everyvent.domain.follow.FollowStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSearchService {

  private final FollowService followService;


  public double calculateTextScore(User user, String keyword) {
    String lowerKeyword = keyword.toLowerCase();
    String nickname = user.getNickname().toLowerCase();
    String introduction = Objects.toString(user.getIntroduction(), "").toLowerCase();

    if (nickname.equals(lowerKeyword)) return 100;
    if (!introduction.isEmpty() && introduction.equals(lowerKeyword)) return 90;

    if (nickname.startsWith(lowerKeyword)) return 80;
    if (!introduction.isEmpty() && introduction.startsWith(lowerKeyword)) return 70;

    if (nickname.contains(lowerKeyword)) return 60;
    if (!introduction.isEmpty() && introduction.contains(lowerKeyword)) return 50;

    return 0;
  }

  public double calculateFollowScore(FollowStatus followStatus) {
    if (followStatus == FollowStatus.MUTUAL) return 50;
    if (followStatus == FollowStatus.FOLLOWING) return 30;
    if (followStatus == FollowStatus.FOLLOWER) return 20;

    return 0;
  }

  public FollowStatus getFollowStatus(User user, User target) {

    boolean isFollowing = followService.isFollowing(user.getId(), target.getId());
    boolean isFollower = followService.isFollowing(target.getId(), user.getId());

    if (isFollowing && isFollower) return FollowStatus.MUTUAL;
    if (isFollowing) return FollowStatus.FOLLOWING;
    if (isFollower) return FollowStatus.FOLLOWER;
    return FollowStatus.NONE;
  }

  public double calculatePopularityScore(int followerCount) {
    return Math.min(50, Math.log10(followerCount + 1) * 10);
  }

  public double calculateTotalScore(User user, User target, String keyword) {
    double textScore = calculateTextScore(target, keyword);
    double followScore = calculateFollowScore(getFollowStatus(user, target));
    double popularityScore = calculatePopularityScore(followService.getFollowerCount(target.getId()));

    return textScore + followScore * 1.5 + popularityScore * 0.5;
  }

}
