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
            @Parameter(description = "파티 생성 대상 상품 ID", required = true, example = "1")
            @RequestParam Long productId
    );

    @Operation(
            summary = "금융결제원 계좌 연결 콜백",
            description = "OAuth 인가코드(code)와 state를 받아 연결 계좌 후보를 저장한 뒤 프론트 계좌등록 페이지로 리다이렉트합니다."
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
            summary = "정산/환불 계좌 등록",
            description = "연결된 계좌(fintech_use_num)를 기준으로 정산계좌 메타데이터를 저장하고 실명검증 상태를 반영합니다."
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
            summary = "연결 계좌 목록 조회",
            description = "인증 사용자의 연결된 계좌 목록을 조회합니다."
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
            summary = "금융결제원 계좌 연결 인증 URL 조회",
            description = """
                프론트가 axios로 호출하여 오픈뱅킹 authorize URL을 응답받습니다.
                이 API는 Authorization 헤더 기반 인증을 통과한 뒤 state를 생성/저장하고,
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
            @Parameter(description = "파티 생성 대상 상품 ID", required = true, example = "1")
            @RequestParam Long productId
    );


}