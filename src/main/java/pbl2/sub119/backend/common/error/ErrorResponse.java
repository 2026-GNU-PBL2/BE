package pbl2.sub119.backend.common.error;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse<T> {
    private String code;
    private String message;
    private T data;

    public ErrorResponse(final ErrorCode code, final T data) {
        this.code = code.getCode();
        this.message = code.getMessage();
        this.data = data;
    }

    public ErrorResponse(final ErrorCode code, final String message) {
        this.code = code.getCode();
        this.message = message;
    }

    public static <T> ErrorResponse<T> of(final ErrorCode code, final T data) {
        return new ErrorResponse<>(code, data);
    }

    public static ErrorResponse<Void> of(final ErrorCode code, final String message) {
        return new ErrorResponse<>(code, message);
    }

    public static ErrorResponse<Void> of(final ErrorCode code) {
        return new ErrorResponse<>(code, null);
    }

}
