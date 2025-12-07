package kr.santanrudolph.everyvent.domain.calendar;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Official {

  @Id
  private Long id;

  @OneToOne
  @MapsId
  @JoinColumn(name = "id")
  private Calendar calendar;

  @Column
  private Long distributedCount;

  public static Official create(Calendar calendar) {
    Official official = new Official();
    official.calendar = calendar;
    return official;
  }

}
