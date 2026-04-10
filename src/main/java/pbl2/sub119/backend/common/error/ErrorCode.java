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
    PARTY_HOST_TRANSFER_REQUIRED(HttpStatus.BAD_REQUEST, "PARTY022", "파티장은 탈퇴 예약 전에 승계를 먼저 진행해야 합니다."),

    // Payment
    PAYMENT_BILLING_KEY_ISSUE_FAILED(HttpStatus.BAD_REQUEST, "PAYMENT001", "빌링키 발급에 실패했습니다."),
    PAYMENT_BILLING_KEY_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "PAYMENT002", "이미 등록된 결제 수단이 있습니다."),
    PAYMENT_CHARGE_FAILED(HttpStatus.BAD_REQUEST, "PAYMENT003", "자동결제 실행에 실패했습니다."),
    PAYMENT_BILLING_EXECUTION_FAILED(HttpStatus.BAD_REQUEST, "PAYMENT_004", "자동결제 실행에 실패했습니다."),
    PAYMENT_BILLING_KEY_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT005", "등록된 결제 수단이 없습니다."),
    PAYMENT_INVALID_BILLING_REQUEST(HttpStatus.BAD_REQUEST,"PAYMENT006", "유효하지 않은 자동결제 요청입니다."),
    PAYMENT_INVALID_IDEMPOTENCY_KEY(HttpStatus.BAD_REQUEST,"PAYMENT007", "유효하지 않은 멱등 키입니다."),

    // Bank
    BANK_CONNECTED_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "BANK001", "연결된 계좌를 찾을 수 없습니다."),
    BANK_INVALID_ACCOUNT_TYPE(HttpStatus.BAD_REQUEST, "BANK002", "유효하지 않은 계좌 유형입니다."),
    BANK_SETTLEMENT_ACCOUNT_REGISTER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "BANK003", "정산 계좌 등록에 실패했습니다."),
    BANK_ACCOUNT_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "BANK004", "계좌 실명 검증에 실패했습니다."),
    BANK_ACCOUNT_VERIFICATION_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "BANK005", "계좌 실명 검증 요청에 실패했습니다."),
    BANK_PRIMARY_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "BANK006", "대표 정산 계좌가 없습니다."),
    BANK_ACCOUNT_CONNECT_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "BANK007", "계좌 연결 요청에 실패했습니다."),

    // Party Operation
    PARTY_OPERATION_NOT_FOUND(HttpStatus.NOT_FOUND, "PARTY023", "파티 운영 정보가 존재하지 않습니다."),
    PARTY_OPERATION_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "PARTY024", "운영 대상 멤버가 아닙니다."),
    PARTY_OPERATION_TYPE_REQUIRED(HttpStatus.BAD_REQUEST, "PARTY025", "운영 유형은 필수입니다."),
    PARTY_OPERATION_INVITE_VALUE_REQUIRED(HttpStatus.BAD_REQUEST, "PARTY026", "초대형 운영은 초대값이 필요합니다."),
    PARTY_OPERATION_SHARED_EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "PARTY027", "계정공유형 운영은 공유 계정 이메일이 필요합니다."),
    PARTY_OPERATION_SHARED_PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "PARTY028", "계정공유형 운영은 공유 계정 비밀번호가 필요합니다."),
    PARTY_OPERATION_ALREADY_ACTIVE(HttpStatus.BAD_REQUEST, "PARTY029", "이미 운영이 완료된 상태입니다."),
    PARTY_OPERATION_RESET_REQUIRED(HttpStatus.BAD_REQUEST, "PARTY030", "운영 재설정이 필요한 상태입니다."),
    PARTY_OPERATION_NOT_READABLE(HttpStatus.BAD_REQUEST, "PARTY031", "현재 상태에서는 운영 정보를 조회할 수 없습니다."),
    PARTY_OPERATION_PASSWORD_DECRYPT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PARTY032", "공유 계정 비밀번호를 불러오는 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(final HttpStatus status, final String code, final String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}