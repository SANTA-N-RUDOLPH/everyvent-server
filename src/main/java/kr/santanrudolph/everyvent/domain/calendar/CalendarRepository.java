package kr.santanrudolph.everyvent.domain.calendar;

import kr.santanrudolph.everyvent.domain.calendar.entity.Calendar;
import kr.santanrudolph.everyvent.domain.calendar.entity.DistributedCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface CalendarRepository extends JpaRepository<Calendar, Long> {

    @Query("""
    SELECT COUNT(c) FROM Calendar c
    WHERE c.user.id = :userId
      AND c.startDate <= :endDate
      AND c.endDate >= :startDate
      AND c.deletedAt IS NULL
      AND TYPE(c) <> DistributedCalendar
      AND c.id NOT IN (
                SELECT oc.id FROM OriginalCalendar oc
                JOIN OfficialCalendar ofc ON ofc.originalCalendar.id = oc.id
          )
    """)
    long countByUserIdInMonth(
            @Param("userId") Long userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    @Query("""
    SELECT c FROM OriginalCalendar c
    WHERE c.user.id = :userId
      AND c.startDate <= :endDate
      AND c.endDate >= :startDate
      AND c.deletedAt IS NULL
      AND NOT EXISTS (
          SELECT 1 FROM OfficialCalendar o WHERE o.originalCalendar.id = c.id
      )
    """)
    List<Calendar> findOriginalCalendarsByUserIdInMonth(
            @Param("userId") Long userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    @Query("""
    SELECT c FROM DistributedCalendar c
    WHERE c.user.id = :userId
      AND c.startDate <= :endDate
      AND c.endDate >= :startDate
      AND c.deletedAt IS NULL
    """)
    List<DistributedCalendar> findDistributedCalendarsByUserIdInMonth(
            @Param("userId") Long userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );
}
