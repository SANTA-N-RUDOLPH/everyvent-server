package kr.santanrudolph.everyvent.domain.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import kr.santanrudolph.everyvent.global.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"social_id", "social_provider"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "socialId는 필수입니다.")
  @Column(nullable = false)
  private String socialId;

  @NotNull(message = "socialProvider는 필수입니다.")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SocialProvider socialProvider;

  @Email
  @Column(unique = true)
  private String email;

  @NotBlank(message = "닉네임은 필수입니다.")
  @Column(nullable = false, unique = true)
  private String nickname;

  @Column(length = 500)
  private String introduction;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Column
  private Instant deletedAt;

  public User(String socialId, SocialProvider socialProvider, String email, String nickname,
      String introduction) {
    this.socialId = socialId;
    this.socialProvider = socialProvider;
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
    this.deletedAt = Instant.now();
  }

  public boolean isDeleted() {
    return this.deletedAt != null;
  }
}
