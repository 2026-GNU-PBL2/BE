package pbl2.sub119.backend.party.join.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.party.join.dto.request.PartyJoinApplyRequest;
import pbl2.sub119.backend.party.join.dto.request.PartyJoinPreviewRequest;
import pbl2.sub119.backend.party.join.dto.response.PartyJoinApplyResponse;
import pbl2.sub119.backend.party.join.dto.response.PartyJoinCancelResponse;
import pbl2.sub119.backend.party.join.dto.response.PartyJoinMeResponse;
import pbl2.sub119.backend.party.join.dto.response.PartyJoinPreviewResponse;

@Tag(
        name = "Party Join API",
        description = "파티 자동 매칭 신청 및 참여 상태 조회 API"
)
public interface PartyJoinDocs {

    @Operation(
            summary = "파티 참여 전 결제 안내 조회",
            description = """
                    선택한 상품으로 파티 참여를 신청하기 전에 상품 금액, 수수료, 보증금,
                    다음 정산일부터 결제될 금액을 확인합니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "결제 안내 조회 성공",
                            content = @Content(schema = @Schema(implementation = PartyJoinPreviewResponse.class))
                    )
            }
    )
    @PostMapping("/preview")
    ResponseEntity<PartyJoinPreviewResponse> getJoinPreview(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @RequestBody @Valid PartyJoinPreviewRequest request
    );

    @Operation(
            summary = "파티 자동 매칭 신청",
            description = """
                    선택한 상품으로 파티 참여를 신청합니다.

                    동작 방식
                    - 즉시 참여 가능한 파티가 있으면 바로 참여합니다.
                    - 바로 참여 가능한 파티가 없으면 자동 매칭 대기 상태로 등록합니다.

                    상태값 안내
                    - ACTIVE : 즉시 참여가 완료된 상태
                    - WAITING : 자동 매칭 대기열에 등록된 상태
                    - CANCELED : 사용자가 자동 매칭 신청을 취소한 상태
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "자동 매칭 신청 성공",
                            content = @Content(schema = @Schema(implementation = PartyJoinApplyResponse.class))
                    )
            }
    )
    @PostMapping("/apply")
    ResponseEntity<PartyJoinApplyResponse> applyJoin(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @RequestBody @Valid PartyJoinApplyRequest request
    );

    @Operation(
            summary = "내 파티 자동 매칭 신청 조회",
            description = """
                    현재 내가 신청한 자동 매칭 상태를 조회합니다.
                    신청일, 파티 시작일, 예상 결제 금액을 확인할 수 있습니다.

                    상태값 안내
                    - WAITING : 자동 매칭 대기 중
                    - ACTIVE : 파티 참여 완료
                    - CANCELED : 자동 매칭 신청 취소 완료
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "내 자동 매칭 신청 조회 성공",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = PartyJoinMeResponse.class))
                            )
                    )
            }
    )
    @GetMapping("/me")
    ResponseEntity<List<PartyJoinMeResponse>> getMyJoinRequests(
            @Parameter(hidden = true) @Auth Accessor accessor
    );

    @Operation(
            summary = "파티 자동 매칭 신청 취소",
            description = """
                    자동 매칭 대기중인 파티 참여 신청을 취소합니다.

                    상태값 안내
                    - WAITING 상태의 신청만 취소할 수 있습니다.
                    - 취소 후에는 CANCELED 상태로 처리됩니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "자동 매칭 신청 취소 성공",
                            content = @Content(schema = @Schema(implementation = PartyJoinCancelResponse.class))
                    )
            }
    )
    @PostMapping("/{joinRequestId}/cancel")
    ResponseEntity<PartyJoinCancelResponse> cancelJoin(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long joinRequestId
    );
}