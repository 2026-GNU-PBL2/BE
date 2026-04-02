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
import pbl2.sub119.backend.party.dto.request.MatchWaitingRegisterRequest;
import pbl2.sub119.backend.party.dto.response.JoinOrQueueResponse;
import pbl2.sub119.backend.party.dto.response.MatchWaitingResponse;

@Tag(name = "Party Waiting API", description = "즉시 참여/대기열 API")
public interface PartyWaitingDocs {

    @Operation(
            summary = "즉시 참여 또는 대기열 등록",
            description = """
                    동일 상품 기준으로 즉시 참여 가능한 파티를 탐색합니다.
                    참여 가능한 파티가 있으면 즉시 참여하고, 없으면 대기열에 등록합니다.

                    waiting status:
                    - WAITING: 대기 중
                    - MATCHED: 매칭 완료
                    - CANCELED: 대기 취소
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "즉시 참여 또는 대기열 등록 성공",
                            content = @Content(schema = @Schema(implementation = JoinOrQueueResponse.class))
                    )
            }
    )
    @PostMapping("/join-or-queue")
    ResponseEntity<JoinOrQueueResponse> joinOrQueue(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @RequestBody MatchWaitingRegisterRequest request
    );

    @Operation(
            summary = "대기열 취소",
            description = "사용자가 등록한 대기열을 취소합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "대기열 취소 성공")
            }
    )
    @PostMapping("/{waitingId}/cancel")
    ResponseEntity<Void> cancelWaiting(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long waitingId
    );

    @Operation(
            summary = "내 대기열 조회",
            description = "현재 사용자가 등록한 대기열 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "대기열 조회 성공",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = MatchWaitingResponse.class))
                            )
                    )
            }
    )
    @GetMapping("/me")
    ResponseEntity<List<MatchWaitingResponse>> getMyWaitingList(
            @Parameter(hidden = true) @Auth Accessor accessor
    );
}