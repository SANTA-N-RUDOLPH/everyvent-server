package kr.santanrudolph.everyvent.domain.calendar.scrap;

import kr.santanrudolph.everyvent.domain.calendar.Calendar;
import kr.santanrudolph.everyvent.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScrapService {
  private final ScrapRepository scrapRepository;

  @Transactional
  public Scrap addScrap(User user, Calendar calendar) {
    Scrap scrap = scrapRepository.findById(calendar.getId()).orElse(Scrap.create(calendar));

    scrap.addScrapper(user);
    return scrapRepository.save(scrap);
  }

  public Long getScrapCount(Long calendarId) {
    Scrap scrap = scrapRepository.findById(calendarId).orElse(null);
    if (scrap == null) {
      return 0L;
    }
    return scrap.getScrapCount();
  }

  public boolean isScrapped(User user, Long calendarId) {
    return scrapRepository.existsScrapper(calendarId, user.getId());
  }

  public Map<Long, Long> getScrapCountMap(List<Long> calendarIds) {
    return scrapRepository
        .findScrapCountsByCalendarIds(calendarIds)
        .stream()
        .collect(
            Collectors
                .toMap(
                    ScrapCountProjection::getCalendarId,
                    ScrapCountProjection::getScrapCount
                ));
  }

}
