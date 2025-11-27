package kr.santanrudolph.everyvent.domain.calendar.entity;

import jakarta.persistence.*;
import kr.santanrudolph.everyvent.domain.calendar.enums.Category;
import kr.santanrudolph.everyvent.domain.calendar.enums.Visibility;
import kr.santanrudolph.everyvent.domain.calendar.validator.ValidColor;
import kr.santanrudolph.everyvent.domain.user.User;
import kr.santanrudolph.everyvent.global.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@Table(name = "calendar")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Calendar extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false)
  private LocalDate startDate;

  @Column(nullable = false)
  private LocalDate endDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Visibility visibility;

  @ValidColor
  @Column(nullable = false)
  private String color;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Category category;

  private Instant deletedAt;

  protected Calendar(User user,
                     String title,
                     String description,
                     LocalDate startDate,
                     LocalDate endDate,
                     Visibility visibility,
                     String color,
                     Category category) {
    this.user = user;
    this.title = title;
    this.description = description;
    this.startDate = startDate;
    this.endDate = endDate;
    this.visibility = visibility;
    this.color = color;
    this.category = category;
  }

  public abstract boolean isScrapable();

  public void softDelete() {
    this.deletedAt = Instant.now();
  }


  // 필드 업데이트 메서드
  public void updateTitle(String title) {
    this.title = title;
  }

  public void updateDescription(String description) {
    this.description = description != null ? description : "";
  }

  public void updatePeriod(LocalDate startDate, LocalDate endDate) {
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public void updateVisibility(Visibility visibility) {
    this.visibility = visibility;
  }

  public void updateColor(String color) {
    this.color = color;
  }

  public void updateCategory(Category category) {
    this.category = category;
  }

}
