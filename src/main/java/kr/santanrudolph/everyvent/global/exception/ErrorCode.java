package kr.santanrudolph.everyvent.global.exception;

import lombok.Getter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

  // 클라이언트 에러
  INVALID_INPUT(HttpStatus.BAD_REQUEST, "유효하지 않은 입력값입니다."),
  NOT_FOUND(HttpStatus.NOT_FOUND, "해당 리소스를 찾을 수 없습니다."),
  ALREADY_EXIST(HttpStatus.CONFLICT, "이미 존재하는 리소스입니다."),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),

  // 인증/인가 에러
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 요청입니다."),
  FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
  INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
  ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 액세스 토큰입니다."),
  INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
  REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 리프레시 토큰입니다."),

  // 서버 에러
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),

  // OAuth 에러
  OAUTH_CLIENT_ERROR(HttpStatus.BAD_GATEWAY, "OAuth 처리 중 오류가 발생했습니다."),

  // 핵심 비즈니스 에러 (프론트에서 분기 필요)



  /*=================================================================================================*/
  /*                                     에러 코드 설계 원칙                                             */
  /*=================================================================================================*/
  /*                                                                                                 */
  /* [원칙 1] 범용 에러 코드 사용을 기본으로 한다                                                         */
  /*   - CRUD 작업: NOT_FOUND, ALREADY_EXIST, INVALID_INPUT 등 사용                                  */
  /*   - detail 필드로 구체적인 에러 메시지 전달                                                         */
  /*   - 예시: throw new EveryventException(ErrorCode.NOT_FOUND, "캘린더를 찾을 수 없습니다.")          */
  /*                                                                                                 */
  /* [원칙 2] 핵심 비즈니스 규칙만 별도 에러 코드로 선언한다                                               */
  /*   - 💡 클라이언트가 특별한 UI/UX 처리가 필요한 경우                                                    */
  /*   - 모니터링/분석이 중요한 비즈니스 제약사항                                                         */
  /*   - 판단 기준:                                                                                   */
  /*     ✅ 자주 발생하고 사용자 경험에 중요한 영향                                                       */
  /*     ✅ 클라이언트가 에러 코드 기반으로 분기 처리 필요                                                 */
  /*     ❌ 단순 안내 메시지면 범용 코드 + detail 사용                                                   */
  /*                                                                                                 */
  /* [HTTP 상태 코드 가이드]                                                                           */
  /*   - 400 BAD_REQUEST: 잘못된 요청 (형식 오류, 비즈니스 규칙 위반)                                    */
  /*   - 401 UNAUTHORIZED: 인증 실패 (토큰 없음, 만료, 유효하지 않음)                                    */
  /*   - 403 FORBIDDEN: 권한 없음 (인증은 됐지만 접근 불가, 시간 제약 포함)                               */
  /*   - 404 NOT_FOUND: 리소스 없음                                                                   */
  /*   - 409 CONFLICT: 리소스 충돌 (중복, 이미 처리됨)                                                  */
  /*   - 410 GONE: 영구 삭제된 리소스                                                                  */
  /*   - 500 INTERNAL_SERVER_ERROR: 서버 내부 오류                                                    */
  /*                                                                                                 */
  /*=================================================================================================*/
  /*                                   Deprecated Error Codes                                        */
  /*=================================================================================================*/
  /*                                                                                                 */
  /* 아래 에러 코드들은 범용 에러 코드 정책 도입 전에 사용되던 레거시 코드입니다.                            */
  /* 기존 코드와의 호환성을 위해 유지하되, 새로운 코드에서는 사용을 지양합니다.                              */
  /*                                                                                                 */
  /* [마이그레이션 가이드]                                                                              */
  /*   BEFORE: throw new EveryventException(ErrorCode.CALENDAR_NOT_FOUND);                          */
  /*   AFTER:  throw new EveryventException(ErrorCode.NOT_FOUND, "캘린더를 찾을 수 없습니다.");        */
  /*                                                                                                 */
  /*   BEFORE: throw new EveryventException(ErrorCode.ALREADY_FOLLOWING);                           */
  /*   AFTER:  throw new EveryventException(ErrorCode.ALREADY_EXIST, "이미 팔로우한 사용자입니다.");   */
  /*                                                                                                 */
  /*=================================================================================================*/

  // 공통 에러
  @Deprecated INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
  @Deprecated INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "잘못된 타입입니다."),
  @Deprecated HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근이 거부되었습니다."),

  // 인증/인가 에러
  @Deprecated INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
  @Deprecated EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
  @Deprecated BLACKLISTED_TOKEN(HttpStatus.UNAUTHORIZED, "로그아웃된 토큰입니다."),
  @Deprecated ACCESS_DENIED(HttpStatus.FORBIDDEN, "권한이 없습니다."),

  // 사용자 에러
  @Deprecated USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
  @Deprecated DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
  @Deprecated USER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 탈퇴한 사용자입니다."),
  @Deprecated INVALID_USER_INFO(HttpStatus.BAD_REQUEST, "잘못된 사용자 정보입니다."),

  // 캘린더 에러
  @Deprecated CALENDAR_NOT_FOUND(HttpStatus.NOT_FOUND, "캘린더를 찾을 수 없습니다."),
  @Deprecated MAX_CALENDAR_EXCEEDED(HttpStatus.BAD_REQUEST, "캘린더는 최대 3개까지 생성할 수 있습니다."),
  @Deprecated CALENDAR_ACCESS_DENIED(HttpStatus.FORBIDDEN, "캘린더에 접근할 권한이 없습니다."),
  @Deprecated CALENDAR_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 캘린더입니다."),
  @Deprecated INVALID_CALENDAR_PERIOD(HttpStatus.BAD_REQUEST, "잘못된 캘린더 기간입니다."),
  @Deprecated OFFICIAL_CALENDAR_CANNOT_MODIFY(HttpStatus.FORBIDDEN, "공식 캘린더는 수정할 수 없습니다."),

  // 태스크 에러
  @Deprecated TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "태스크를 찾을 수 없습니다."),
  @Deprecated TASK_NOT_ACCESSIBLE_YET(HttpStatus.BAD_REQUEST, "아직 열람할 수 없는 태스크입니다."),
  @Deprecated INVALID_TASK_DATE(HttpStatus.BAD_REQUEST, "유효하지 않은 태스크 날짜입니다. (1-25일)"),
  @Deprecated TASK_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 완료된 태스크입니다."),
  @Deprecated TASK_ACCESS_DENIED(HttpStatus.FORBIDDEN, "태스크에 접근할 권한이 없습니다."),

  // 스크랩 에러
  @Deprecated SCRAP_NOT_FOUND(HttpStatus.NOT_FOUND, "스크랩을 찾을 수 없습니다."),
  @Deprecated ALREADY_SCRAPPED(HttpStatus.BAD_REQUEST, "이미 스크랩한 캘린더입니다."),
  @Deprecated CANNOT_SCRAP_OWN_CALENDAR(HttpStatus.BAD_REQUEST, "본인의 캘린더는 스크랩할 수 없습니다."),
  @Deprecated SCRAP_NOT_AVAILABLE_YET(HttpStatus.BAD_REQUEST, "스크랩할 수 있는 기간이 아닙니다."),

  // 팔로우 에러
  @Deprecated FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "팔로우 관계를 찾을 수 없습니다."),
  @Deprecated ALREADY_FOLLOWING(HttpStatus.BAD_REQUEST, "이미 팔로우한 사용자입니다."),
  @Deprecated CANNOT_FOLLOW_SELF(HttpStatus.BAD_REQUEST, "자기 자신을 팔로우할 수 없습니다."),

  // OAuth 에러
  @Deprecated OAUTH_PROVIDER_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인입니다."),
  @Deprecated OAUTH_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "소셜 로그인에 실패했습니다."),
  @Deprecated OAUTH_USER_INFO_FETCH_FAILED(HttpStatus.BAD_REQUEST, "사용자 정보를 가져오는데 실패했습니다.");

  private final HttpStatus httpStatus;
  private final String message;

  public String getCode() {
    return name();
  }

}
