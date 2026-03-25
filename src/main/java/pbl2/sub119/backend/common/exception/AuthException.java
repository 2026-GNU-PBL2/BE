package pbl2.sub119.backend.common.exception;

import lombok.Getter;
import pbl2.sub119.backend.common.error.ErrorCode;

@Getter
public class AuthException extends RuntimeException {
  private final ErrorCode errorCode;

  public AuthException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}