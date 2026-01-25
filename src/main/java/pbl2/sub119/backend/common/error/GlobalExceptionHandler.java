package pbl2.sub119.backend.common.error;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pbl2.sub119.backend.common.exception.AuthException;
import pbl2.sub119.backend.common.exception.BusinessException;
import pbl2.sub119.backend.common.exception.NotFoundException;

import java.util.HashMap;
import java.util.Map;

import static pbl2.sub119.backend.common.error.ErrorCode.INVALID_ENUM_FORMAT;
import static pbl2.sub119.backend.common.error.ErrorCode.MESSAGE_BODY_UNREADABLE;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 지원하지 않는 HTTP method 호출할 경우 발생하는 예외 처리
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse<Void>> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException", e);
        return createErrorResponseEntity(ErrorCode.METHOD_NOT_ALLOWED);
    }

    // @Valid 관련 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse<Map<String, String>>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException", e);

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(
                error -> errors.put(error.getField(), error.getDefaultMessage()));

        return createErrorResponseEntity(errors);
    }

    // Enum 관련 예외
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse<Void>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e) {
        if (isEnumBindingError(e)) {
            return handleEnumBindingError((InvalidFormatException) e.getCause());
        }
        return createErrorResponseEntity(MESSAGE_BODY_UNREADABLE, MESSAGE_BODY_UNREADABLE.getMessage());
    }

    @ExceptionHandler(AuthException.class)
    protected ResponseEntity<ErrorResponse<Void>> handleAuthException(AuthException e) {
        log.error("AuthException", e);
        return createErrorResponseEntity(e.getErrorCode(), e.getMessage());
    }

    // 커스텀 비즈니스 예외 처리
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse<Void>> handleBusinessException(BusinessException e) {
        log.error("BusinessException", e);
        return createErrorResponseEntity(e.getErrorCode());
    }

    // IllegalArgumentException 예외 처리
    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<ErrorResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException", e);
        return createErrorResponseEntity(ErrorCode.BAD_REQUEST, e.getMessage());
    }

    // 404 NOT FOUND 예외처리
    @ExceptionHandler(NotFoundException.class)
    protected ResponseEntity<ErrorResponse<Void>> handleNotFoundException(NotFoundException e) {
        log.error("NotFoundException", e);
        return createErrorResponseEntity(e.getErrorCode());
    }

    // 그외 예외 처리
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse<Void>> handleExceptionInternal(Exception e) {
        log.error("Exception", e);
        return createErrorResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse<Void>> handleEnumBindingError(InvalidFormatException e) {
        String enumName = e.getTargetType().getSimpleName();
        String invalidValue = String.valueOf(e.getValue());
        String message = String.format(INVALID_ENUM_FORMAT.getMessage(), invalidValue, enumName);
        return createErrorResponseEntity(INVALID_ENUM_FORMAT, message);
    }

    private ResponseEntity<ErrorResponse<Void>> createErrorResponseEntity(ErrorCode errorCode) {
        return new ResponseEntity<>(
                ErrorResponse.of(errorCode),
                errorCode.getStatus());
    }

    private <T> ResponseEntity<ErrorResponse<T>> createErrorResponseEntity(T data) {
        return new ResponseEntity<>(
                ErrorResponse.of(ErrorCode.BAD_REQUEST, data),
                ErrorCode.BAD_REQUEST.getStatus());
    }

    private ResponseEntity<ErrorResponse<Void>> createErrorResponseEntity(ErrorCode errorCode, String message) {
        return new ResponseEntity<>(
                ErrorResponse.of(errorCode, message),
                errorCode.getStatus());
    }

    private boolean isEnumBindingError(HttpMessageNotReadableException e) {
        return e.getCause() instanceof InvalidFormatException ife
                && ife.getTargetType().isEnum();
    }
}
