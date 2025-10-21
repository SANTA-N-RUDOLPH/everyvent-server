package kr.santanrudolph.everyvent.auth.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

  @Id
  private Long userId;

  @Column(nullable = false, unique = true, length = 500)
  private String token;

  @Column(nullable = false)
  private Instant expiresAt;

  @Builder
  public RefreshToken(Long userId, String token, Instant expiresAt) {
    this.userId = userId;
    this.token = token;
    this.expiresAt = expiresAt;
  }

  public void updateToken(String token, Instant expiresAt) {
    this.token = token;
    this.expiresAt = expiresAt;
  }

  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }
}
