package pbl2.sub119.backend.party.controller.docs;

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
import org.springframework.web.bind.annotation.RequestBody;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.party.dto.request.HostTransferRequestCreateRequest;
import pbl2.sub119.backend.party.dto.response.HostTransferResponse;

@Tag(name = "Party Host Transfer API", description = "파티장 승계 API")
public interface PartyHostTransferDocs {

    @Operation(
            summary = "승계 요청 생성",
            description = """
                    파티장이 ACTIVE 상태의 기존 파티원 한 명에게 승계 요청을 보냅니다.

                    hostTransfer.status:
                    - REQUESTED: 요청됨
                    - COMPLETED: 승계 완료
                    - REJECTED: 거절됨
                    - CANCELED: 취소됨
                    - EXPIRED: 만료됨
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "승계 요청 성공",
                            content = @Content(schema = @Schema(implementation = HostTransferResponse.class))
                    )
            }
    )
    @PostMapping("/{partyId}/request")
    ResponseEntity<HostTransferResponse> requestTransfer(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId,
            @RequestBody HostTransferRequestCreateRequest request
    );

    @Operation(
            summary = "승계 요청 수락",
            description = "승계 대상자가 승계 요청을 수락합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "승계 수락 성공")
            }
    )
    @PostMapping("/{requestId}/accept")
    ResponseEntity<Void> acceptTransfer(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long requestId
    );

    @Operation(
            summary = "승계 요청 거절",
            description = "승계 대상자가 승계 요청을 거절합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "승계 거절 성공")
            }
    )
    @PostMapping("/{requestId}/reject")
    ResponseEntity<Void> rejectTransfer(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long requestId
    );

    @Operation(
            summary = "승계 요청 현황 조회",
            description = "파티장이 해당 파티의 승계 요청 현황을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = HostTransferResponse.class))
                            )
                    )
            }
    )
    @GetMapping("/{partyId}/requests")
    ResponseEntity<List<HostTransferResponse>> getTransferRequests(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId
    );
}