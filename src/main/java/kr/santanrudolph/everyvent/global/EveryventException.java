package kr.santanrudolph.everyvent.global;

import lombok.Getter;

@Getter
public class EveryventException extends RuntimeException {

  private final ErrorCode errorCode;

  public EveryventException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

}
