package kr.santanrudolph.everyvent.domain.calendar;

import kr.santanrudolph.everyvent.domain.calendar.entity.OfficialCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OfficialCalendarRepository extends JpaRepository<OfficialCalendar, Long> {

  Optional<OfficialCalendar> findByOriginalCalendarId(Long calendarId);

  @Query("""
    SELECT officialCalendar
    FROM OfficialCalendar officialCalendar
    WHERE officialCalendar.originalCalendar.startDate <= :end
      AND officialCalendar.originalCalendar.endDate >= :start
      AND officialCalendar.deletedAt IS NULL
  """)
  List<OfficialCalendar> findAllByPeriod(
          @Param("start") Instant start,
          @Param("end") Instant end
  );

  boolean existsByOriginalCalendarId(Long calendarId);
}
