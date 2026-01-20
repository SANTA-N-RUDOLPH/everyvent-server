package kr.santanrudolph.everyvent.domain.calendar.service;

import kr.santanrudolph.everyvent.domain.calendar.Calendar;
import kr.santanrudolph.everyvent.domain.calendar.Scrap;
import kr.santanrudolph.everyvent.domain.calendar.dto.reponse.ScrapperResponse;
import kr.santanrudolph.everyvent.domain.calendar.dto.reponse.ScrapperScrollResponse;
import kr.santanrudolph.everyvent.domain.calendar.repository.ScrapCountProjection;
import kr.santanrudolph.everyvent.domain.calendar.repository.ScrapRepository;
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
    Scrap saved = scrapRepository.save(scrap);
    log.info("Scrap added - calendarId={}, userId={}, totalScrapCount={}",
        calendar.getId(), user.getId(), saved.getScrapCount());
    return saved;
  }

  @Transactional
  public void removeScrap(User user, Long calendarId) {
    Scrap scrap = scrapRepository.findById(calendarId).orElse(null);
    if (scrap != null) {
      scrap.removeScrapper(user);
      Scrap saved = scrapRepository.save(scrap);
      log.info("Scrap removed - calendarId={}, userId={}, remainingScrapCount={}",
          calendarId, user.getId(), saved.getScrapCount());
    }
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

  public ScrapperScrollResponse getScrappers(Long calendarId, Long cursor, Integer size) {
    List<User> scrappers = scrapRepository.findScrappersByCalendarId(calendarId, cursor, size + 1);

    boolean hasNext = scrappers.size() > size;
    Long nextCursor = null;

    if (hasNext) {
      List<User> limitedScrappers = scrappers.subList(0, size);
      nextCursor = limitedScrappers.get(limitedScrappers.size() - 1).getId();
      scrappers = limitedScrappers;
    }

    List<ScrapperResponse> scrapperResponses = scrappers.stream()
        .map(ScrapperResponse::from)
        .toList();

    return new ScrapperScrollResponse(scrapperResponses, nextCursor, hasNext);
  }

}
