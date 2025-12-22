package kr.santanrudolph.everyvent.domain.task;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

  @Query("""
      SELECT t
      FROM Task t
      JOIN FETCH t.calendar
      WHERE t.calendar.id = :calendarId
      ORDER BY t.day ASC
      """)
  List<Task> findAllByCalendarId(@Param("calendarId") Long calendarId);

  @Query("""
      SELECT COUNT(t)
      FROM Task t
      WHERE t.calendar.id = :calendarId
        AND t.day = :day
      """)
  long countByCalendarIdAndDay(
      @Param("calendarId") Long calendarId,
      @Param("day") LocalDate day
  );

  void deleteByCalendarId(Long calendarId);
}
