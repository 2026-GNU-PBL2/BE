package pbl2.sub119.backend.toss.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.payment.dto.response.PaymentHistoryItem;
import pbl2.sub119.backend.toss.dto.request.BillingKeyIssueRequest;
import pbl2.sub119.backend.toss.dto.response.BillingKeyInfoResponse;

import java.util.List;
import java.util.Map;

@Tag(
        name = "Payment API",
        description = "결제 수단 등록 및 자동결제 관련 API"
)
public interface PaymentDocs {

    @Operation(
            summary = "빌링용 customerKey 조회",
            description = """
                    현재 로그인한 사용자의 토스 자동결제용 customerKey를 조회합니다.
                    프론트는 이 값을 사용해서 토스 SDK 결제창을 초기화합니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "customerKey 조회 성공"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 필요",
                            content = @Content
                    )
            }
    )
    @GetMapping("/billing/customer-key")
    ResponseEntity<Map<String, String>> getCustomerKey(
            @Parameter(hidden = true) @Auth Accessor accessor
    );

    @Operation(
            summary = "빌링키 발급",
            description = """
                    프론트에서 토스 SDK 결제창으로 카드 등록을 완료한 뒤,
                    success callback에서 전달받은 authKey와 customerKey를 받아 빌링키를 발급합니다.
                    
                    발급된 빌링키는 서버에 저장되며, 이후 자동결제 승인에 사용됩니다.
                    빌링키 발급 성공 후 후속 파티 결제 준비 로직은 이벤트 기반으로 처리됩니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "빌링키 발급 성공"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 또는 빌링키 발급 실패",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 필요",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "이미 등록된 결제 수단 존재",
                            content = @Content
                    )
            }
    )
    @PostMapping("/billing/authorize")
    ResponseEntity<Void> issueBillingKey(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @Parameter(description = "빌링키 발급 요청 정보", required = true)
            @Valid @RequestBody BillingKeyIssueRequest request
    );

    @Operation(
            summary = "내 결제수단 조회",
            description = """
                    현재 로그인한 사용자의 ACTIVE 결제수단 메타데이터를 조회합니다.
                    등록된 결제수단이 없으면 hasBillingKey=false로 응답합니다.
                    billingKey 원문은 반환하지 않습니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공 (결제수단 없을 경우 hasBillingKey=false)"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 필요",
                            content = @Content
                    )
            }
    )
    @GetMapping("/billing/me")
    ResponseEntity<BillingKeyInfoResponse> getBillingInfo(
            @Parameter(hidden = true) @Auth Accessor accessor
    );

    @Operation(
            summary = "결제수단 변경",
            description = """
                    기존 등록된 결제수단을 새 카드로 교체합니다.
                    프론트에서 토스 SDK 카드 등록 후 받은 authKey를 전달하면,
                    Toss API로 새 빌링키를 발급받아 기존 row를 UPDATE합니다.
                    Toss 호출 실패 시 DB는 변경되지 않습니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "결제수단 변경 성공"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 또는 Toss 빌링키 발급 실패",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 필요",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "등록된 결제수단 없음",
                            content = @Content
                    )
            }
    )
    @PostMapping("/billing/change")
    ResponseEntity<Void> changeBillingKey(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @Parameter(description = "새 카드 authKey", required = true)
            @Valid @RequestBody BillingKeyIssueRequest request
    );

    @Operation(
            summary = "내 결제내역 조회",
            description = """
                    현재 로그인한 사용자의 결제내역을 최신순으로 조회합니다.
                    page(0-based), size 파라미터로 페이징을 제어합니다.
                    실패 건은 failureReason / failureCode 필드를 포함합니다.
                    상태값: PAYMENT_PENDING / PROCESSING / PAID / FAILED / CANCELLED
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 필요",
                            content = @Content
                    )
            }
    )
    @GetMapping("/me/history")
    ResponseEntity<List<PaymentHistoryItem>> getMyPaymentHistory(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @Parameter(description = "페이지 번호 (0-based, 기본값 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (기본값 20, 최대 100)") @RequestParam(defaultValue = "20") int size
    );
}