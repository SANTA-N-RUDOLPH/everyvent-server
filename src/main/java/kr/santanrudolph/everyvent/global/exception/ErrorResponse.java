package kr.santanrudolph.everyvent.global.exception;


public record ErrorResponse(
    int status,
    String code,
    String message,
    String detail

) {

  public static ErrorResponse of(ErrorCode errorCode) {
    return new ErrorResponse(
        errorCode.getHttpStatus().value(),
        errorCode.getCode(),
        errorCode.getMessage(),
        null
    );
  }

  public static ErrorResponse of(ErrorCode errorCode, String detail) {
    return new ErrorResponse(
        errorCode.getHttpStatus().value(),
        errorCode.getCode(),
        errorCode.getMessage(),
        detail
    );
  }
}
