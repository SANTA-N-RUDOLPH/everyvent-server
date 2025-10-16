package kr.santanrudolph.everyvent.global.exception;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public enum ErrorCode {

  // 공통 에러
  INVALID_INPUT_VALUE("잘못된 입력값입니다."),
  METHOD_NOT_ALLOWED("지원하지 않는 HTTP 메서드입니다."),
  INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다."),
  INVALID_TYPE_VALUE("잘못된 타입입니다."),
  HANDLE_ACCESS_DENIED("접근이 거부되었습니다."),

  // 인증/인가 에러
  UNAUTHORIZED("인증이 필요합니다."),
  INVALID_TOKEN("유효하지 않은 토큰입니다."),
  EXPIRED_TOKEN("만료된 토큰입니다."),
  BLACKLISTED_TOKEN("로그아웃된 토큰입니다."),
  ACCESS_DENIED("권한이 없습니다."),

  // 사용자 에러
  USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
  DUPLICATE_NICKNAME("이미 사용 중인 닉네임입니다."),
  USER_ALREADY_DELETED("이미 탈퇴한 사용자입니다."),
  INVALID_USER_INFO("잘못된 사용자 정보입니다."),

  // 캘린더 에러
  CALENDAR_NOT_FOUND("캘린더를 찾을 수 없습니다."),
  MAX_CALENDAR_EXCEEDED("캘린더는 최대 3개까지 생성할 수 있습니다."),
  CALENDAR_ACCESS_DENIED("캘린더에 접근할 권한이 없습니다."),
  CALENDAR_ALREADY_DELETED("이미 삭제된 캘린더입니다."),
  INVALID_CALENDAR_PERIOD("잘못된 캘린더 기간입니다."),
  OFFICIAL_CALENDAR_CANNOT_MODIFY("공식 캘린더는 수정할 수 없습니다."),

  // 태스크 에러
  TASK_NOT_FOUND("태스크를 찾을 수 없습니다."),
  MAX_TASK_PER_DAY_EXCEEDED("하루에 최대 3개의 태스크만 생성할 수 있습니다."),
  TASK_NOT_ACCESSIBLE_YET("아직 열람할 수 없는 태스크입니다."),
  INVALID_TASK_DATE("유효하지 않은 태스크 날짜입니다. (1-25일)"),
  TASK_ALREADY_COMPLETED("이미 완료된 태스크입니다."),
  TASK_ACCESS_DENIED("태스크에 접근할 권한이 없습니다."),

  // 스크랩 에러
  SCRAP_NOT_FOUND("스크랩을 찾을 수 없습니다."),
  ALREADY_SCRAPPED("이미 스크랩한 캘린더입니다."),
  CANNOT_SCRAP_OWN_CALENDAR("본인의 캘린더는 스크랩할 수 없습니다."),
  SCRAP_NOT_AVAILABLE_YET("스크랩할 수 있는 기간이 아닙니다."),

  // 팔로우 에러
  FOLLOW_NOT_FOUND("팔로우 관계를 찾을 수 없습니다."),
  ALREADY_FOLLOWING("이미 팔로우한 사용자입니다."),
  CANNOT_FOLLOW_SELF("자기 자신을 팔로우할 수 없습니다."),

  // OAuth 에러
  OAUTH_PROVIDER_NOT_SUPPORTED("지원하지 않는 소셜 로그인입니다."),
  OAUTH_AUTHENTICATION_FAILED("소셜 로그인에 실패했습니다."),
  OAUTH_USER_INFO_FETCH_FAILED("사용자 정보를 가져오는데 실패했습니다.");

  private final String message;

  public String getCode() {
    return name();
  }

  public String getMessage() {
    return message;
  }

}
