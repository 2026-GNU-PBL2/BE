package pbl2.sub119.backend.bankaccounts.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.bankaccounts.dto.request.RegisterSettlementAccountRequest;
import pbl2.sub119.backend.bankaccounts.dto.response.BankAccountSummaryResponse;
import pbl2.sub119.backend.bankaccounts.dto.response.BankAuthorizeUrlResponse;
import pbl2.sub119.backend.bankaccounts.dto.response.PrimaryBankAccountResponse;
import pbl2.sub119.backend.common.error.ErrorResponse;

import java.util.List;

@Tag(name = "Bank", description = "계좌 연결/정산계좌 관리 API")
public interface BankDocs {

    @Operation(
            summary = "금융결제원 계좌 연결 인증 시작",
            description = "로그인 사용자의 계좌 연결을 위해 금융결제원 authorize URL로 리다이렉트합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "금융결제원 authorize URL로 리다이렉트",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> authorize(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @Parameter(description = "파티 생성 대상 상품 ID", required = true, example = "11111111-1111-1111-1111-111111111111")
            @RequestParam String productId
    );

    @Operation(
            summary = "금융결제원 계좌 연결 콜백",
            description = """
                    OAuth 인가코드(code)와 state를 받아 연결 계좌를 저장한 뒤 flow에 맞는 프론트 경로로 리다이렉트합니다.
                    - PARTY_CREATE flow: /party/create/{productId}/host/account-register 로 복귀
                    - MY_PAGE flow: /mypage/account-register 로 복귀
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "프론트 계좌등록 페이지로 리다이렉트",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "계좌 연결 요청 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> callback(
            @Parameter(description = "금융결제원 OAuth 인가코드", required = true, example = "AbCdEf123456")
            @RequestParam String code,
            @Parameter(description = "동의 범위(scope)", required = false, example = "login inquiry")
            @RequestParam(required = false) String scope,
            @Parameter(description = "인증 요청 식별용 state", required = true, example = "12345678901234567890123456789012")
            @RequestParam String state
    );

    @Operation(
            summary = "정산 계좌 등록 / 교체",
            description = """
                    금융결제원 인증으로 연결된 계좌(fintech_use_num)를 활성 정산계좌로 설정합니다.

                    시퀀스: 금융결제원 재인증 → GET /bank/accounts 로 목록 확인 → 이 API로 계좌 선택
                    - accountType은 반드시 SETTLEMENT 이어야 합니다. 그 외 값은 400(BANK002)으로 거부됩니다.
                    - isPrimary 필드는 요청에 포함하지 않습니다. 등록된 계좌는 항상 대표 계좌가 됩니다.
                    - 기존 활성 정산계좌(account_type=SETTLEMENT, is_primary=true)가 비활성화되고, \
                    선택한 계좌 1건만 활성 정산계좌로 확정됩니다. 나머지 연결 계좌 row는 유지됩니다.
                    - 전체 처리는 단일 트랜잭션 내에서 원자적으로 수행됩니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정산계좌 등록 성공",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 계좌 검증 실패 (BANK002, BANK004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "연결 계좌 없음 (BANK001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "등록/검증 요청 실패 (BANK003, BANK005)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    String registerSettlementAccount(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @Valid @RequestBody RegisterSettlementAccountRequest request
    );

    @Operation(
            summary = "활성 정산계좌 해제",
            description = """
                    현재 활성 정산계좌(account_type=SETTLEMENT, is_primary=true)를 비활성화합니다.
                    - 연결 계좌 row는 삭제되지 않고 목록에 유지됩니다.
                    - 해제 후 POST /bank/settlement 로 다시 계좌를 선택할 수 있습니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "정산계좌 해제 성공",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "활성 정산계좌 없음 (BANK006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    void deleteSettlementAccount(
            @Parameter(hidden = true) @Auth Accessor accessor
    );

    @Operation(
            summary = "연결 계좌 목록 조회",
            description = """
                    금융결제원 최신 인증 후보(Redis, TTL 10분) 기준으로 계좌 목록을 반환합니다.
                    - 재인증 후 TTL 이내: 최신 인증 스냅샷(Redis 후보)만 노출되며 과거 인증 계좌는 누적되지 않습니다.
                    - Redis TTL 만료 또는 캐시 미스: 기존 DB 목록으로 fallback합니다.
                    - 정산계좌 활성 상태(account_type=SETTLEMENT, is_primary=true)는 POST /bank/settlement 호출 시점에만 확정됩니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BankAccountSummaryResponse.class)))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    List<BankAccountSummaryResponse> getAccounts(
            @Parameter(hidden = true) @Auth Accessor accessor
    );

    @Operation(
            summary = "대표 정산계좌 조회",
            description = "인증 사용자의 대표 정산계좌를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PrimaryBankAccountResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "대표 계좌 없음 (BANK006)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    PrimaryBankAccountResponse getPrimaryAccount(
            @Parameter(hidden = true) @Auth Accessor accessor
    );

    @Operation(
            summary = "[파티 생성용] 금융결제원 계좌 연결 인증 URL 조회",
            description = """
                    파티 생성 시 계좌 연결을 위한 오픈뱅킹 authorize URL을 반환합니다. productId 필수.
                    callback 후 /party/create/{productId}/host/account-register 로 복귀합니다.
                    프론트는 응답받은 authorizeUrl로 window.location.href 이동하면 됩니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "오픈뱅킹 authorize URL 조회 성공",
                    content = @Content(schema = @Schema(implementation = BankAuthorizeUrlResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    BankAuthorizeUrlResponse authorizeUrl(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @Parameter(description = "파티 생성 대상 상품 ID", required = true, example = "11111111-1111-1111-1111-111111111111")
            @RequestParam String productId
    );

    @Operation(
            summary = "[마이페이지용] 금융결제원 계좌 재인증 URL 조회",
            description = """
                    마이페이지에서 정산계좌 변경을 위한 오픈뱅킹 authorize URL을 반환합니다. productId 불필요.
                    callback 후 /mypage/account-register 로 복귀합니다.
                    프론트는 응답받은 authorizeUrl로 window.location.href 이동하면 됩니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "오픈뱅킹 authorize URL 조회 성공",
                    content = @Content(schema = @Schema(implementation = BankAuthorizeUrlResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    BankAuthorizeUrlResponse authorizeUrlMyPage(
            @Parameter(hidden = true) @Auth Accessor accessor
    );
}
