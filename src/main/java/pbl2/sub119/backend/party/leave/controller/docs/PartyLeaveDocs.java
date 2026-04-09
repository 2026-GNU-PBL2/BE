package pbl2.sub119.backend.party.leave.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.party.leave.dto.response.PartyLeaveReservationMemberResponse;
import pbl2.sub119.backend.party.leave.dto.response.PartyLeaveReserveResponse;

@Tag(name = "Party Leave API", description = "파티 탈퇴 예약 및 취소 API")
public interface PartyLeaveDocs {

    @Operation(
            summary = "탈퇴 예약",
            description = """
                    현재 이용 중인 파티에서 다음 결제일 기준으로 탈퇴를 예약합니다.
                    파티원 탈퇴 예약 시 파티원 결원, 파티장 탈퇴 예약 시 파티장 결원으로 반영됩니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "탈퇴 예약 성공",
                            content = @Content(schema = @Schema(implementation = PartyLeaveReserveResponse.class))
                    )
            }
    )
    @PostMapping("/{partyId}/reserve")
    ResponseEntity<PartyLeaveReserveResponse> reserveLeave(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId
    );

    @Operation(
            summary = "탈퇴 예약 취소",
            description = "등록한 탈퇴 예약을 취소하고 기존 이용 상태로 되돌립니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "탈퇴 예약 취소 성공")
            }
    )
    @DeleteMapping("/{partyId}/reserve")
    ResponseEntity<Void> cancelLeave(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId
    );

    @Operation(
            summary = "탈퇴 예약 멤버 목록 조회",
            description = "파티장이 현재 탈퇴 예약된 멤버 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = PartyLeaveReservationMemberResponse.class))
                            )
                    )
            }
    )
    @GetMapping("/{partyId}/reservations")
    ResponseEntity<List<PartyLeaveReservationMemberResponse>> getLeaveReservations(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId
    );
}