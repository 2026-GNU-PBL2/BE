package pbl2.sub119.backend.party.settings.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.party.settings.dto.response.PartyFeeDetailResponse;
import pbl2.sub119.backend.party.settings.dto.response.PartySettingsResponse;

@Tag(name = "Party Settings API", description = "파티 설정 및 정산/결제 정보 조회 API")
public interface PartySettingsDocs {

    @Operation(
            summary = "파티 설정 정보 조회",
            description = """
                    파티 상세 화면의 설정 버튼 클릭 시 호출합니다.
                    로그인한 사용자의 역할(파티장/파티원)에 따라 다른 필드를 반환합니다.

                    파티장(HOST) 반환 필드
                    - ottServiceName : OTT 서비스명
                    - partyCreatedAt : 파티 생성일
                    - settlementDayOfMonth : 정산일 (매달 n일)
                    - monthlySettlementAmount : 매달 정산받는 금액 (원)
                    - settlementBankName : 정산 계좌 은행명 (미등록 시 null)
                    - settlementAccountMasked : 마스킹된 계좌번호 (미등록 시 null)

                    파티원(MEMBER) 반환 필드
                    - ottServiceName : OTT 서비스명
                    - partyCreatedAt : 파티 생성일
                    - monthlyPaymentAmount : 매달 결제 금액 (수수료 포함, 원)
                    - billingDayOfMonth : 결제일 (매달 n일)
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "설정 정보 조회 성공",
                            content = @Content(schema = @Schema(implementation = PartySettingsResponse.class))
                    ),
                    @ApiResponse(responseCode = "403", description = "해당 파티의 멤버가 아님"),
                    @ApiResponse(responseCode = "404", description = "파티, 서브상품 또는 결제 사이클 없음")
            }
    )
    @GetMapping("/{partyId}/settings")
    ResponseEntity<PartySettingsResponse> getPartySettings(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId
    );

    @Operation(
            summary = "수수료 및 정산/결제 상세 안내 조회",
            description = """
                    설정 페이지의 '정산 내역 자세히 보기' 버튼 클릭 시 호출합니다.
                    파티장과 파티원 각각 다른 정보를 반환합니다.

                    파티장(HOST) — 수수료 및 정산일 안내
                    - memberCount : 파티원 수
                    - membersShareAmount : 파티원 n명의 OTT 분담금 합계 (원)
                    - platformFee : 픽플러스 수수료 490원 (파티원 할인 적용 고정값)
                    - monthlySettlementAmount : 매달 정산받는 금액 (원)
                    - nextSettlementDate : 다음 정산일
                    - isSettlementGuaranteeApplied : 정산 보장제 적용 여부

                    파티원(MEMBER) — 수수료 및 결제일 안내
                    - monthlyPaymentAmount : 매달 결제 금액 (수수료 포함, 원)
                    - platformFee : 픽플러스 수수료 990원 (고정값)
                    - ottUsageFee : OTT 순수 분담금 (결제금액 - 수수료, 원)
                    - nextBillingDate : 다음 결제일

                    계산 공식
                    - membersShareAmount = 파티원 수 × (결제금액 - 990)
                    - monthlySettlementAmount = membersShareAmount - 490
                    - ottUsageFee = monthlyPaymentAmount - 990
                    - 다음 정산/결제일 = 현재 billingDueAt + 1개월
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "수수료 상세 조회 성공",
                            content = @Content(schema = @Schema(implementation = PartyFeeDetailResponse.class))
                    ),
                    @ApiResponse(responseCode = "403", description = "해당 파티의 멤버가 아님"),
                    @ApiResponse(responseCode = "404", description = "파티 또는 결제 사이클 없음")
            }
    )
    @GetMapping("/{partyId}/settings/fee-detail")
    ResponseEntity<PartyFeeDetailResponse> getPartyFeeDetail(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId
    );
}
