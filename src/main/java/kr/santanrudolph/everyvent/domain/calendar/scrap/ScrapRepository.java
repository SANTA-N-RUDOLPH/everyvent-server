package kr.santanrudolph.everyvent.domain.calendar.scrap;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {


  List<Scrap> findAllByIdIn(List<Long> calendarIds);

  @Query("""
      SELECT s.scrapCount FROM Scrap s WHERE s.calendar.id = :calendarId
      """)
  Long countScrapsByCalendarId(@Param("calendarId") Long calendarId);

  @Query("""
      SELECT s.id AS calendarId, s.scrapCount AS scrapCount
      FROM Scrap s
      WHERE s.id in :calendarIds
      """)
  List<ScrapCountProjection> findScrapCountsByCalendarIds(@Param("calendarIds") List<Long> calendarIds);


  @Query("""
          SELECT
              CASE
                  WHEN COUNT(scrappers) > 0 THEN true
                  ELSE false
              END
          FROM Scrap s
              JOIN s.scrappers scrappers
          WHERE s.id = :calendarId
            AND scrappers.id = :scrapperId
      """)
  boolean existsScrapper(
      @Param("calendarId") Long calendarId,
      @Param("scrapperId") Long scrapperId
  );
}
