package kr.santanrudolph.everyvent.global.exception;

import lombok.Getter;

@Getter
public class EveryventException extends RuntimeException {

  private final ErrorCode errorCode;

  public EveryventException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public EveryventException(ErrorCode errorCode, String fieldName) {
    super(fieldName + errorCode.getMessage());
    this.errorCode = errorCode;
  }

}
