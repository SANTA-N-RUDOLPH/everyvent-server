package kr.santanrudolph.everyvent.domain.calendar.dto.reponse;

import java.util.List;

public record ScrapperScrollResponse(
    List<ScrapperResponse> scrappers,
    Long nextCursor,
    boolean hasNext
) {
}
