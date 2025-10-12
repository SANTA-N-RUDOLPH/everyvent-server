package kr.santanrudolph.everyvent.global;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // 공통 에러
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
  INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "잘못된 타입입니다."),
  HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근이 거부되었습니다."),

  // 인증/인가 에러
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
  EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "권한이 없습니다."),

  // 사용자 에러
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
  DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
  USER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 탈퇴한 사용자입니다."),
  INVALID_USER_INFO(HttpStatus.BAD_REQUEST, "잘못된 사용자 정보입니다."),

  // 캘린더 에러
  CALENDAR_NOT_FOUND(HttpStatus.NOT_FOUND, "캘린더를 찾을 수 없습니다."),
  MAX_CALENDAR_EXCEEDED(HttpStatus.BAD_REQUEST, "캘린더는 최대 3개까지 생성할 수 있습니다."),
  CALENDAR_ACCESS_DENIED(HttpStatus.FORBIDDEN, "캘린더에 접근할 권한이 없습니다."),
  CALENDAR_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 캘린더입니다."),
  INVALID_CALENDAR_PERIOD(HttpStatus.BAD_REQUEST, "잘못된 캘린더 기간입니다."),
  OFFICIAL_CALENDAR_CANNOT_MODIFY(HttpStatus.BAD_REQUEST, "공식 캘린더는 수정할 수 없습니다."),

  // 태스크 에러
  TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "태스크를 찾을 수 없습니다."),
  MAX_TASK_PER_DAY_EXCEEDED(HttpStatus.BAD_REQUEST, "하루에 최대 3개의 태스크만 생성할 수 있습니다."),
  TASK_NOT_ACCESSIBLE_YET(HttpStatus.FORBIDDEN, "아직 열람할 수 없는 태스크입니다."),
  INVALID_TASK_DATE(HttpStatus.BAD_REQUEST, "유효하지 않은 태스크 날짜입니다. (1-25일)"),
  TASK_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 완료된 태스크입니다."),
  TASK_ACCESS_DENIED(HttpStatus.FORBIDDEN, "태스크에 접근할 권한이 없습니다."),

  // 스크랩 에러
  SCRAP_NOT_FOUND(HttpStatus.NOT_FOUND, "스크랩을 찾을 수 없습니다."),
  ALREADY_SCRAPPED(HttpStatus.CONFLICT, "이미 스크랩한 캘린더입니다."),
  CANNOT_SCRAP_OWN_CALENDAR(HttpStatus.BAD_REQUEST, "본인의 캘린더는 스크랩할 수 없습니다."),
  SCRAP_NOT_AVAILABLE_YET(HttpStatus.BAD_REQUEST, "스크랩할 수 있는 기간이 아닙니다."),

  // 팔로우 에러
  FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "팔로우 관계를 찾을 수 없습니다."),
  ALREADY_FOLLOWING(HttpStatus.CONFLICT, "이미 팔로우한 사용자입니다."),
  CANNOT_FOLLOW_SELF(HttpStatus.BAD_REQUEST, "자기 자신을 팔로우할 수 없습니다."),

  // OAuth 에러
  OAUTH_PROVIDER_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인입니다."),
  OAUTH_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "소셜 로그인에 실패했습니다."),
  OAUTH_USER_INFO_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 정보를 가져오는데 실패했습니다.");

  private final HttpStatus httpStatus;
  private final String message;


  public String getCode() {
    return name();
  }

  public String getMessage() {
    return message;
  }

}
