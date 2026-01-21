package kr.santanrudolph.everyvent.domain.calendar.repository;


import kr.santanrudolph.everyvent.domain.calendar.Official;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface OfficialRepository extends JpaRepository<Official, Long> {
  @Query("SELECT o FROM Official o JOIN FETCH o.calendar")
  List<Official> findAllWithCalendar();
}
