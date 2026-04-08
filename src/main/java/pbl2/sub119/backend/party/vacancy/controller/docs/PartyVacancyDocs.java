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
import pbl2.sub119.backend.party.vacancy.dto.response.MemberVacancyPartyResponse;
import pbl2.sub119.backend.party.vacancy.dto.response.PartyVacancyDetailResponse;
import pbl2.sub119.backend.party.vacancy.dto.response.PartyVacancyJoinResponse;

@Tag(
        name = "Party Vacancy API",
        description = "결원 파티 조회 및 직접 참여 API"
)
public interface PartyVacancyDocs {

    @Operation(
            summary = "파티원 결원 파티 목록 조회",
            description = "현재 파티원을 찾고 있는 결원 파티 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "파티원 결원 파티 목록 조회 성공",
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
            summary = "결원 파티 상세 조회",
            description = "직접 참여 전에 결원 파티의 상품 정보, 인원 현황, 결제 금액을 확인합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "결원 파티 상세 조회 성공",
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
            description = "바로 참여 가능한 결원 파티에 직접 참여합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "결원 파티 직접 참여 성공",
                            content = @Content(schema = @Schema(implementation = PartyVacancyJoinResponse.class))
                    )
            }
    )
    @PostMapping("/{partyId}/join")
    ResponseEntity<PartyVacancyJoinResponse> joinMemberVacancyParty(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId
    );
}