package kr.santanrudolph.everyvent.domain.user;

import jakarta.persistence.*;
import kr.santanrudolph.everyvent.global.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"socialId", "provider"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String socialId;

  @Column(nullable = false)
  private String provider;

  @Column
  private String email;

  @Column(nullable = false, unique = true)
  private String nickname;

  @Column
  private String introduction;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Column
  private LocalDateTime deletedAt;

  @Builder
  public User(String socialId, String provider, String email, String nickname,
      String introduction) {
    this.socialId = socialId;
    this.provider = provider;
    this.email = email;
    this.nickname = nickname;
    this.introduction = introduction;
    this.role = Role.USER; // 기본값 USER
  }

  public void updateIntroduction(String introduction) {
    if (introduction != null) {
      this.introduction = introduction;
    }
  }

  public void updateNickname(String nickname) {
    if (nickname != null && !nickname.isBlank()) {
      this.nickname = nickname;
    }
  }

  public void softDelete() {
    this.deletedAt = LocalDateTime.now();
  }

  public boolean isDeleted() {
    return this.deletedAt != null;
  }
}
