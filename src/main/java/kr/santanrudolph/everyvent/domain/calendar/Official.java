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

  @OneToOne(cascade = CascadeType.REMOVE)
  @MapsId
  @JoinColumn(name = "id")
  private Calendar calendar;

  @Column
  private Long distributedCount;

  public static Official create(Calendar calendar) {
    Official official = new Official();
    official.calendar = calendar;
    official.distributedCount = 0L;
    return official;
  }

}
