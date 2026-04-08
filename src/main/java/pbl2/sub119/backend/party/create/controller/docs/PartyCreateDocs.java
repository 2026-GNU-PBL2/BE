package pbl2.sub119.backend.party.create.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.party.create.dto.request.PartyCreateRequest;
import pbl2.sub119.backend.party.create.dto.request.PartyCreateSummaryRequest;
import pbl2.sub119.backend.party.create.dto.response.PartyCreateResponse;
import pbl2.sub119.backend.party.create.dto.response.PartyCreateSummaryResponse;

@Tag(
        name = "Party Create API",
        description = "파티 생성 화면에서 사용하는 요약 조회 및 실제 생성 API"
)
public interface PartyCreateDocs {

    @Operation(
            summary = "파티 생성 전 요약 정보 조회",
            description = """
                    파티 생성 화면에서 파티원 수를 바꿀 때 예상 금액 정보를 미리 확인합니다.
                    
                    이런 경우에 사용합니다.
                    - 파티원 수를 2명, 3명, 4명으로 바꿔보며 금액 변화를 보고 싶을 때
                    - 파티 생성 전에 파티장이 실제로 부담할 금액과 예상 정산 금액을 먼저 확인할 때
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "파티 생성 전 요약 정보 조회 성공",
                            content = @Content(schema = @Schema(implementation = PartyCreateSummaryResponse.class))
                    )
            }
    )
    @PostMapping("/create-summary")
    ResponseEntity<PartyCreateSummaryResponse> getCreateSummary(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @RequestBody @Valid PartyCreateSummaryRequest request
    );

    @Operation(
            summary = "파티 생성",
            description = """
                    선택한 OTT 상품으로 새 파티를 생성합니다.
                    파티를 생성한 사용자는 파티장으로 등록되며, 생성 직후 파티원 모집을 시작할 수 있습니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "파티 생성 성공",
                            content = @Content(schema = @Schema(implementation = PartyCreateResponse.class))
                    )
            }
    )
    @PostMapping
    ResponseEntity<PartyCreateResponse> createParty(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @RequestBody @Valid PartyCreateRequest request
    );
}