package kr.santanrudolph.everyvent.domain.follow;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import kr.santanrudolph.everyvent.domain.user.User;
import kr.santanrudolph.everyvent.global.entity.BaseEntity;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "follows",
    indexes = {
        @Index(name = "idx_follow_follower", columnList = "follower_id"),
        @Index(name = "idx_follow_target", columnList = "target_id"),
        @Index(name = "idx_follow_follower_target", columnList = "follower_id, target_id", unique = true)
    }
)

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "follower_id", nullable = false)
  private User follower;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "target_id", nullable = false)
  private User target;


  public static Follow create(User follower, User target) {
    if (follower == null) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "팔로워가 null 입니다.");
    }

    if (target == null) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "팔로우 대상이 null 입니다.");
    }

    if (follower.equals(target)) {
      throw new EveryventException(ErrorCode.INVALID_INPUT, "자기 자신을 팔로우할 수 없습니다.");
    }

    Follow follow = new Follow();
    follow.follower = follower;
    follow.target = target;
    return follow;
  }
}
