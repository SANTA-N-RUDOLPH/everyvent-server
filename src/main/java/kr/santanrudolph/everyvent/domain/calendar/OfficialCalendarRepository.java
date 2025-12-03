package kr.santanrudolph.everyvent.domain.calendar;

import kr.santanrudolph.everyvent.domain.calendar.entity.OfficialCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OfficialCalendarRepository extends JpaRepository<OfficialCalendar, Long> {

  Optional<OfficialCalendar> findByOriginalCalendarId(Long calendarId);

  @Query("""
    SELECT officialCalendar
    FROM OfficialCalendar officialCalendar
    WHERE officialCalendar.deletedAt IS NULL
  """)
  List<OfficialCalendar> findAllOfficialCalendars();

  boolean existsByOriginalCalendarId(Long calendarId);
}
