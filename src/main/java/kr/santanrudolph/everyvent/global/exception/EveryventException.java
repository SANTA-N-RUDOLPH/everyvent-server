package kr.santanrudolph.everyvent.global.exception;

import lombok.Getter;

@Getter
public class EveryventException extends RuntimeException {

  private final ErrorCode errorCode;
  private final String detail;

  public EveryventException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.detail = null;
  }

  public EveryventException(ErrorCode errorCode, String detail) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.detail = detail;
  }

  public EveryventException(ErrorCode errorCode, String fieldName) {
    super(fieldName + errorCode.getMessage());
    this.errorCode = errorCode;
  }

}
