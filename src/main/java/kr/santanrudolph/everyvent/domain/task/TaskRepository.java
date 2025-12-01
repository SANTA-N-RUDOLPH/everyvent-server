package kr.santanrudolph.everyvent.domain.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
  Optional<Task> findByIdAndDeletedAtIsNull(Long taskId);

  List<Task> findByCalendarIdAndDeletedAtIsNull(Long calendarId);

  @Query("""
    SELECT t
    FROM Task t
    WHERE t.calendar.id = :calendarId
      AND t.day = :day
      AND t.deletedAt IS NULL
  """)
  List<Task> findByCalendarIdAndDay(
      @Param("calendarId") Long calendarId,
      @Param("day") int day);

  @Query("""
    SELECT t.day
    FROM Task t
    WHERE t.calendar.id = :calendarId
      AND t.day BETWEEN :start AND :end
      AND t.deletedAt IS NULL
    GROUP BY t.day
    HAVING COUNT(t) >= 3
    """)
  List<Integer> findDaysWithLimitExceeded(
          @Param("calendarId") Long calendarId,
          @Param("start") int start,
          @Param("end") int end
  );
}
