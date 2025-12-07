package kr.santanrudolph.everyvent.domain.calendar.dto;

public record DistributeResponse(
    Long officialCalendarId,
    int distributedUserCount  // 배포된 유저 수
) {
}
