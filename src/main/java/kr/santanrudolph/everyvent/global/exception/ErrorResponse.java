package kr.santanrudolph.everyvent.global.exception;

public record ErrorResponse(
    String code,
    String message,
    String status
) {

  public static ErrorResponse of(ErrorCode errorCode) {
    return new ErrorResponse(
        errorCode.getCode(),
        errorCode.getMessage(),
        "ERROR"
    );
  }
}
