package kr.santanrudolph.everyvent.domain.calendar;

import kr.santanrudolph.everyvent.domain.calendar.entity.OfficialCalendar;
import kr.santanrudolph.everyvent.domain.calendar.entity.OriginalCalendar;
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

  boolean existsByOriginalCalendar(OriginalCalendar calendar);

  void deleteByOriginalCalendar(OriginalCalendar oc);

  @Query("""
    SELECT oc
    FROM OfficialCalendar oc
    WHERE oc.originalCalendar.startDate = :start
      AND oc.originalCalendar.endDate = :end
  """)
  List<OfficialCalendar> findAllByPeriod(
          @Param("start") Instant start,
          @Param("end") Instant end
  );
}
