package pbl2.sub119.backend.concurrent.exception;

import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.common.exception.BusinessException;

public class ConcurrentException extends BusinessException {

    public ConcurrentException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
