package pbl2.sub119.backend.party.cycle.controller.docs;

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
import pbl2.sub119.backend.party.cycle.dto.response.PartyUsagePeriodResponse;

@Tag(name = "Party Usage API", description = "파티 현재 이용 기간 조회 API")
public interface PartyUsagePeriodDocs {

    @Operation(
            summary = "현재 이용 기간 조회",
            description = "마이파티, 파티 상세, 탈퇴 예약 안내 화면에서 현재 이용 기간과 다음 결제일을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "현재 이용 기간 조회 성공",
                            content = @Content(schema = @Schema(implementation = PartyUsagePeriodResponse.class))
                    )
            }
    )
    @GetMapping("/{partyId}/usage-period")
    ResponseEntity<PartyUsagePeriodResponse> getUsagePeriod(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId
    );
}