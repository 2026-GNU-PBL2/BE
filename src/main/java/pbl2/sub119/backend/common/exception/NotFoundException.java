package pbl2.sub119.backend.common.exception;


import lombok.Getter;
import pbl2.sub119.backend.common.error.ErrorCode;

@Getter
public class NotFoundException extends RuntimeException {
  private final ErrorCode errorCode;


  public NotFoundException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
