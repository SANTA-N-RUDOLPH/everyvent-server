package kr.santanrudolph.everyvent.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "blacklisted_token", indexes = {
    @Index(name = "idx_token", columnList = "token")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlacklistedToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 500)
  private String token;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Builder
  public BlacklistedToken(String token, LocalDateTime expiresAt) {
    this.token = token;
    this.expiresAt = expiresAt;
    this.createdAt = LocalDateTime.now();
  }
}
