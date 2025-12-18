package kr.santanrudolph.everyvent.domain.calendar;

import jakarta.persistence.*;
import kr.santanrudolph.everyvent.domain.user.User;
import kr.santanrudolph.everyvent.global.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Scrap extends BaseEntity {

  @Id
  private Long id;

  @OneToOne
  @MapsId
  @JoinColumn(name = "id")
  private Calendar calendar;

  @Column(nullable = false)
  private Long scrapCount = 0L;

  @ManyToMany
  @JoinTable(
      name = "scrap_user",
      joinColumns = @JoinColumn(name = "scrap_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id")
  )
  private List<User> scrappers = new ArrayList<>();

  public static Scrap create(Calendar calendar) {
    Scrap scrap = new Scrap();
    scrap.calendar = calendar;
    scrap.scrapCount = 0L;
    scrap.scrappers = new ArrayList<>();
    return scrap;
  }

  public void addScrapper(User user) {
    if (!scrappers.contains(user)) {
      scrappers.add(user);
      scrapCount++;
    }
  }

  public void removeScrapper(User user) {
    if (scrappers.remove(user)) {
      scrapCount--;
    }
  }

  public boolean hasScrapper(User user) {
    return scrappers.contains(user);
  }

}
