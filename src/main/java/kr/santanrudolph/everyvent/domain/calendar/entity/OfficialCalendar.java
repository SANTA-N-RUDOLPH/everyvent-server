package kr.santanrudolph.everyvent.domain.calendar.entity;

import jakarta.persistence.*;
import kr.santanrudolph.everyvent.global.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "official_calendar")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OfficialCalendar extends BaseEntity {

  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "calendar_id", nullable = false, unique = true)
  private OriginalCalendar originalCalendar;

  private Instant distributedAt;

  private Instant deletedAt;

  @Builder
  public OfficialCalendar(OriginalCalendar originalCalendar, Instant distributedAt) {
    this.originalCalendar = originalCalendar;
    this.distributedAt = distributedAt;
  }

  public void softDelete() {
    this.deletedAt = Instant.now();
  }


  public void updateDistributedAt(Instant distributedAt) {
    this.distributedAt = distributedAt;
  }

}
