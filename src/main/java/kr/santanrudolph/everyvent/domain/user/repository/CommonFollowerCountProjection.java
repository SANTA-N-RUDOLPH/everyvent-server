package kr.santanrudolph.everyvent.domain.user.repository;


public interface CommonFollowerCountProjection {
  Long getTargetUserId();

  Long getCount();
}
