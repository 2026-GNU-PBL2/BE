package pbl2.sub119.backend.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Common
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON405", "잘못된 HTTP 메서드를 호출했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러가 발생했습니다."),
    MESSAGE_BODY_UNREADABLE(HttpStatus.BAD_REQUEST, "COMMON400", "요청 본문을 읽을 수 없습니다."),
    INVALID_ENUM_FORMAT(HttpStatus.BAD_REQUEST, "COMMON400", "'%s'은(는) 유효한 %s 값이 아닙니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON404", "요청한 리소스를 찾을 수 없습니다."),

    // Auth
    AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH001", "만료된 토큰입니다."),
    AUTH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH002", "유효하지 않은 토큰입니다."),
    AUTH_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH003", "인증 헤더가 누락되었습니다."),
    AUTH_NOT_SUPPORTED_USER_TYPE(HttpStatus.BAD_REQUEST, "AUTH4003", "지원하지 않는 유저 타입입니다."),
    AUTH_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH004", "존재하지 않는 사용자입니다."),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH005", "접근 권한이 없습니다."),
    OAUTH_TOKEN_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "AUTH4006", "소셜 로그인 중 액세스 토큰 요청에 실패했습니다."),
    IS_NOT_VALID_SOCIAL(HttpStatus.BAD_REQUEST, "AUTH001", "지원하지 않는 플랫폼 입니다"),
    OAUTH_USERINFO_RESPONSE_EMPTY(HttpStatus.BAD_REQUEST, "AUTH4007", "소셜 로그인 중 사용자 정보 응답이 비어 있습니다."),

    // SubProduct
    SUB_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT001", "존재하지 않는 상품입니다."),
    SUB_PRODUCT_DUPLICATE_NAME(HttpStatus.BAD_REQUEST, "PRODUCT002", "이미 등록된 서비스명입니다."),

    // MAIL
    RECEIVED_MAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "MAIL001", "존재하지 않는 메일입니다."),


    // Party
    PARTY_INVALID_PRODUCT_ID(HttpStatus.BAD_REQUEST, "PARTY001", "상품 ID는 필수입니다."),
    PARTY_INVALID_CAPACITY(HttpStatus.BAD_REQUEST, "PARTY002", "정원은 2명 이상이어야 합니다."),
    PARTY_INVALID_PRICE(HttpStatus.BAD_REQUEST, "PARTY003", "1인당 금액 정보가 올바르지 않습니다."),
    PARTY_NOT_FOUND(HttpStatus.NOT_FOUND, "PARTY004", "존재하지 않는 파티입니다."),
    PARTY_NOT_RECRUITING(HttpStatus.BAD_REQUEST, "PARTY005", "현재 모집 중인 파티가 아닙니다."),
    PARTY_ALREADY_JOINED(HttpStatus.BAD_REQUEST, "PARTY006", "이미 해당 파티에 참여 중이거나 처리 중인 사용자입니다."),
    PARTY_FULL(HttpStatus.BAD_REQUEST, "PARTY007", "정원이 가득 찼습니다."),
    PARTY_WAITING_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "PARTY008", "이미 해당 상품에 대한 대기열이 존재합니다."),
    PARTY_WAITING_NOT_FOUND(HttpStatus.NOT_FOUND, "PARTY009", "대기열 정보를 찾을 수 없습니다."),
    PARTY_ALREADY_PARTICIPATING_PRODUCT(HttpStatus.BAD_REQUEST, "PARTY010", "이미 해당 상품의 파티에 참여 중입니다."),
    PARTY_WAITING_FORBIDDEN(HttpStatus.FORBIDDEN, "PARTY011", "해당 대기열에 대한 권한이 없습니다."),
    PARTY_INVALID_USER_ID(HttpStatus.BAD_REQUEST, "PARTY012", "유효하지 않은 사용자입니다."),
    PARTY_LEAVE_ALREADY_RESERVED(HttpStatus.BAD_REQUEST, "PARTY013", "이미 탈퇴 예약된 상태입니다."),
    PARTY_LEAVE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PARTY014", "탈퇴 예약이 불가능한 상태입니다."),
    PARTY_LEAVE_FORBIDDEN(HttpStatus.FORBIDDEN, "PARTY015", "해당 파티에 대한 권한이 없습니다."),

    // Host Transfer
    PARTY_HOST_ONLY(HttpStatus.FORBIDDEN, "PARTY016", "파티장만 수행할 수 있습니다."),
    PARTY_HOST_TRANSFER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "PARTY017", "이미 진행 중인 승계 요청이 존재합니다."),
    PARTY_HOST_TRANSFER_NOT_FOUND(HttpStatus.NOT_FOUND, "PARTY018", "승계 요청을 찾을 수 없습니다."),
    PARTY_HOST_TRANSFER_FORBIDDEN(HttpStatus.FORBIDDEN, "PARTY019", "승계 요청에 대한 권한이 없습니다."),
    PARTY_HOST_TRANSFER_INVALID_TARGET(HttpStatus.BAD_REQUEST, "PARTY020", "유효하지 않은 승계 대상입니다."),
    PARTY_HOST_TRANSFER_SELF_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PARTY021", "본인을 승계 대상으로 지정할 수 없습니다."),
    PARTY_HOST_TRANSFER_REQUIRED(HttpStatus.BAD_REQUEST, "PARTY022", "파티장은 탈퇴 예약 전에 승계를 먼저 진행해야 합니다.");
    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(final HttpStatus status, final String code, final String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}