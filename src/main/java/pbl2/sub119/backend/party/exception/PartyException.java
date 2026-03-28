package pbl2.sub119.backend.party.exception;

import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.common.exception.BusinessException;

public class PartyException extends BusinessException {

    public PartyException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PartyException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}