package pbl2.sub119.backend.party.vacancy.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.party.vacancy.dto.response.HostVacancyPartyResponse;
import pbl2.sub119.backend.party.vacancy.dto.response.MemberVacancyPartyResponse;
import pbl2.sub119.backend.party.vacancy.dto.response.PartyVacancyDetailResponse;
import pbl2.sub119.backend.party.vacancy.dto.response.PartyVacancyJoinResponse;

@Tag(
        name = "Party Vacancy API",
        description = "결원 예정/결원 파티 조회 및 직접 참여 API"
)
public interface PartyVacancyDocs {

    @Operation(
            summary = "파티원 결원 예정/결원 파티 목록 조회",
            description = """
                    현재 바로 참여 가능한 파티원 결원 파티와,
                    다음 회차 기준으로 파티원 결원이 예정된 파티 목록을 함께 조회합니다.

                    이 API는 아래 화면에서 사용합니다.
                    - 메인 화면의 파티원 모집 섹션
                    - 파티 참여 희망자가 결원 예정/결원 파티를 찾는 화면
                    - 바로 참여 가능한 파티와 예정 파티를 비교하는 화면

                    정렬 기준
                    - 1순위: 현재 바로 참여 가능한 파티
                    - 2순위: 다음 정산/결제 예정일이 빠른 파티
                    - 3순위: 먼저 생성된 파티

                    응답값 안내
                    - currentMemberCount : 다음 회차 기준 예상 인원 수입니다.
                    - remainingSeatCount : 다음 회차 기준 예상 남은 자리 수입니다.
                    - nextPaymentDate : 다음 정산/결제 예정일입니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "파티원 결원 예정/결원 파티 목록 조회 성공",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = MemberVacancyPartyResponse.class))
                            )
                    )
            }
    )
    @GetMapping("/members")
    ResponseEntity<List<MemberVacancyPartyResponse>> getMemberVacancyParties(
            @RequestParam(required = false) String productId
    );

    @Operation(
            summary = "파티장 결원 예정/결원 파티 목록 조회",
            description = """
                    현재 바로 참여 가능한 파티장 결원 파티와,
                    다음 회차 기준으로 파티장 결원이 예정된 파티 목록을 함께 조회합니다.

                    이 API는 아래 화면에서 사용합니다.
                    - 메인 화면의 파티장 모집 섹션
                    - 파티장 참여 희망자가 결원 예정/결원 파티를 찾는 화면

                    정렬 기준
                    - 1순위: 현재 바로 참여 가능한 파티
                    - 2순위: 다음 정산/결제 예정일이 빠른 파티
                    - 3순위: 먼저 생성된 파티

                    응답값 안내
                    - currentMemberCount : 다음 회차 기준 예상 인원 수입니다.
                    - remainingSeatCount : 다음 회차 기준 예상 남은 자리 수입니다.
                    - nextPaymentDate : 다음 정산/결제 예정일입니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "파티장 결원 예정/결원 파티 목록 조회 성공",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = HostVacancyPartyResponse.class))
                            )
                    )
            }
    )
    @GetMapping("/hosts")
    ResponseEntity<List<HostVacancyPartyResponse>> getHostVacancyParties(
            @RequestParam(required = false) String productId
    );

    @Operation(
            summary = "파티원 결원 예정/결원 파티 상세 조회",
            description = """
                    파티 참여 희망자가 직접 참여 전에 파티원 결원 예정/결원 파티의
                    상품 정보, 다음 회차 기준 인원 현황, 결제 금액을 확인합니다.

                    이 API는 아래 화면에서 사용합니다.
                    - 결원 예정/결원 파티 상세 안내 화면
                    - 직접 참여 전 최종 확인 화면

                    응답값 안내
                    - joinAvailable = true : 현재 바로 참여 가능한 상태입니다.
                    - joinAvailable = false : 현재는 만석이지만 다음 회차에 결원이 반영될 예정인 상태입니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "파티원 결원 예정/결원 파티 상세 조회 성공",
                            content = @Content(schema = @Schema(implementation = PartyVacancyDetailResponse.class))
                    )
            }
    )
    @GetMapping("/{partyId}")
    ResponseEntity<PartyVacancyDetailResponse> getMemberVacancyDetail(
            @PathVariable Long partyId
    );

    @Operation(
            summary = "결원 파티 직접 참여",
            description = """
                    현재 바로 참여 가능한 결원 파티에 직접 참여합니다.

                    상태값 안내
                    - 참여 성공 시 해당 파티의 멤버로 등록됩니다.
                    - 참여 가능한 자리가 없으면 실패할 수 있습니다.
                    - 결원 예정 상태만 있는 파티는 상세 조회는 가능하지만 즉시 참여는 불가능할 수 있습니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "결원 파티 직접 참여 성공",
                            content = @Content(schema = @Schema(implementation = PartyVacancyJoinResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증이 필요합니다."),
                    @ApiResponse(responseCode = "403", description = "참여 권한이 없습니다."),
                    @ApiResponse(responseCode = "404", description = "파티를 찾을 수 없습니다."), @ApiResponse(responseCode = "409", description = "참여 가능한 자리가 없거나 이미 참여한 사용자입니다.")
            }
    )
    @PostMapping("/{partyId}/join")
    ResponseEntity<PartyVacancyJoinResponse> joinMemberVacancyParty(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId
    );
}