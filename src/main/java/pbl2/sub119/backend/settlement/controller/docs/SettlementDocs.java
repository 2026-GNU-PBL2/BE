package pbl2.sub119.backend.settlement.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.settlement.dto.request.WithdrawCreateRequest;
import pbl2.sub119.backend.settlement.dto.response.PointBalanceResponse;
import pbl2.sub119.backend.settlement.dto.response.SettlementHistoryResponse;
import pbl2.sub119.backend.settlement.dto.response.WithdrawRequestResponse;

import java.util.List;

@Tag(name = "Settlement API", description = "파티장 정산 및 포인트 환급 API")
public interface SettlementDocs {

    @Operation(
            summary = "포인트 환급 요청",
            description = """
                    파티장이 적립된 포인트를 현금으로 환급 요청합니다.

                    처리 기준:
                    - 환급 요청 금액: 최소 10,000원 / 최대 100,000원
                    - 요청 즉시 point_wallet 잔액을 차감합니다.
                    - 대표 정산 계좌(SETTLEMENT 유형, VERIFIED 상태)가 반드시 등록되어 있어야 합니다.
                    - 잔액이 부족하면 요청이 실패합니다.

                    응답 필드 안내:
                    - internalPayoutRef : 백엔드 내부 추적 식별자 (PWR-yyyyMMddHHmmss-userId-RANDOM6). 외부 이체 추적 시 어드민에 전달.
                    - externalTxId      : 실제 외부 이체 완료 시 어드민이 입력하는 거래번호 (요청 시점에는 null)

                    에러 안내:
                    - 400 (WITHDRAW001) : 금액 범위 오류 (10,000원 ~ 100,000원)
                    - 400 (WITHDRAW003) : 포인트 잔액 부족
                    - 404 (WITHDRAW002) : 대표 정산 계좌 없음
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "환급 요청 성공",
                            content = @Content(schema = @Schema(implementation = WithdrawRequestResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "금액 범위 오류 또는 잔액 부족"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "404", description = "대표 정산 계좌 없음")
            }
    )
    @PostMapping("/withdraw-requests")
    ResponseEntity<WithdrawRequestResponse> createWithdrawRequest(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @RequestBody WithdrawCreateRequest request
    );

    @Operation(
            summary = "나의 환급 요청 목록 조회",
            description = """
                    로그인한 파티장의 환급 요청 이력을 페이지 단위로 조회합니다.

                    요청 상태 안내:
                    - REQUESTED : 요청 접수 (처리 대기 중)
                    - COMPLETED : 환급 완료
                    - REJECTED  : 환급 거절 (포인트 복구됨)
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = WithdrawRequestResponse.class))
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @GetMapping("/withdraw-requests")
    ResponseEntity<List<WithdrawRequestResponse>> getMyWithdrawRequests(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(
            summary = "나의 포인트 잔액 조회",
            description = "로그인한 파티장의 현재 point_wallet 잔액을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = PointBalanceResponse.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @GetMapping("/points")
    ResponseEntity<PointBalanceResponse> getMyPointBalance(
            @Parameter(hidden = true) @Auth Accessor accessor
    );

    @Operation(
            summary = "나의 정산 이력 조회",
            description = """
                    로그인한 파티장의 정산(settlement) 이력을 페이지 단위로 조회합니다.

                    정산 상태 안내:
                    - ACCRUED : 정산 적립 완료

                    안내:
                    - settlement.status는 적립 시점 이후 변경되지 않습니다.
                    - 환급 요청 및 처리 이력은 GET /api/v1/settlements/withdraw-requests 에서 확인하세요.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = SettlementHistoryResponse.class))
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @GetMapping("/history")
    ResponseEntity<List<SettlementHistoryResponse>> getMySettlementHistory(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    );
}
