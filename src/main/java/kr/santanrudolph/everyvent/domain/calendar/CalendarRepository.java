package kr.santanrudolph.everyvent.domain.calendar;

import kr.santanrudolph.everyvent.domain.calendar.enums.CalendarType;
import kr.santanrudolph.everyvent.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CalendarRepository extends JpaRepository<Calendar, Long> {

  @Query("""
      SELECT COUNT(c)
      FROM Calendar c
      WHERE c.user = :user
        AND YEAR(c.startDate) = :year
        AND MONTH(c.startDate) = :month
        AND c.calendarType IN :types
        AND c.deletedAt IS NULL
      """)
  long countByUserAndYearMonthAndTypes(
      @Param("user") User user,
      @Param("year") int year,
      @Param("month") int month,
      @Param("types") List<CalendarType> types
  );

  @Query("""
      SELECT c
      FROM Calendar c
      WHERE c.user = :user
        AND c.deletedAt IS NULL
      ORDER BY c.startDate DESC
      """)
  List<Calendar> findAllByUserAndDeletedAtIsNull(@Param("user") User user);

  @Query("""
          SELECT c
          FROM Calendar c
          WHERE c.user = :user
            AND c.deletedAt IS NULL
            AND c.startDate >= :startDate
            AND c.startDate <= :endDate
          ORDER BY c.startDate DESC
      """)
  List<Calendar> findByUserAndStartDateBetween(
      @Param("user") User user,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );


  Optional<Calendar> findFirstByUserAndDeletedAtIsNullAndStartDateBeforeOrderByStartDateDesc(User user, LocalDate startDate);

}
