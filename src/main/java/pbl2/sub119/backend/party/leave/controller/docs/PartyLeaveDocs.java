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

                    안내
                    - 이 API를 호출해도 즉시 탈퇴되지는 않습니다.
                    - 다음 결제일에 새로운 이용 주기가 시작될 때 탈퇴가 반영됩니다.
                    - 파티원 탈퇴 예약 시 파티원 결원으로 반영됩니다.
                    - 파티장 탈퇴 예약 시 파티장 결원으로 반영됩니다.

                    상태값 안내
                    - ACTIVE : 현재 정상 이용 중
                    - LEAVE_RESERVED : 다음 결제일 기준 탈퇴 예약된 상태
                    - LEFT : 실제 이용 종료가 반영된 상태
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
            description = """
                    등록한 탈퇴 예약을 취소하고 기존 이용 상태로 되돌립니다.

                    상태값 안내
                    - LEAVE_RESERVED 상태에서 취소하면 다시 ACTIVE 로 돌아갑니다.
                    """,
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
            description = """
                    파티장이 현재 탈퇴 예약된 멤버 목록을 조회합니다.

                    이 API는 아래 화면에서 사용합니다.
                    - 파티장 파티 관리 화면
                    - 다음 회차에 결원이 발생할 멤버를 확인하는 화면

                    상태값 안내
                    - LEAVE_RESERVED : 다음 결제일 기준으로 탈퇴가 예약된 멤버
                    """,
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