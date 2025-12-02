package kr.santanrudolph.everyvent.domain.follow.dto;

import kr.santanrudolph.everyvent.domain.user.dto.response.UserBasicResponse;

public record FollowResponse(
    Long id,
    UserBasicResponse user
) {
}
