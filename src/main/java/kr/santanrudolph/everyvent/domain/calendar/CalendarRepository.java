package kr.santanrudolph.everyvent.domain.calendar;

import kr.santanrudolph.everyvent.domain.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarRepository extends JpaRepository<Calendar,Long> {

    @Query("SELECT COUNT(c) FROM Calendar c " +
            "WHERE c.user.id = :userId " +
            "AND c.startDate = :startDate " +
            "AND c.endDate = :endDate " +
            "AND c.deletedAt IS NULL")
    long countByUserIdInMonth(
            @Param("userId") Long userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    @Query("SELECT c FROM Calendar c " +
            "WHERE c.user.id = :userId " +
            "AND c.startDate = :startDate " +
            "AND c.endDate = :endDate " +
            "AND c.deletedAt IS NULL")
    List<Calendar> findAllByUserIdInMonth(
            @Param("userId") Long userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    @Query("SELECT c FROM OriginalCalendar c " +
            "WHERE c.isOfficial = true AND c.user.role = :role " +
            "AND c.user.id <> :userId " +
            "AND c.startDate = :startDate " +
            "AND c.endDate = :endDate " +
            "AND c.deletedAt IS NULL")
    List<Calendar> findAllOfficialCalendars(
            @Param("userId") Long userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("role") Role role
    );

    @Query("SELECT c.user.id FROM OriginalCalendar c " +
            "WHERE c.officialId = :officialId")
    List<Long> findUserIdsByOfficialId(@Param("officialId") Long officialId);
}
