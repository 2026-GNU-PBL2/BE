package pbl2.sub119.backend.toss.exception;


import lombok.Getter;
import pbl2.sub119.backend.common.error.ErrorCode;

@Getter
public class PaymentException extends RuntimeException {
    private final ErrorCode errorCode;

    public PaymentException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
