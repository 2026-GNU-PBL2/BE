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
import org.springframework.web.bind.annotation.RequestParam;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.party.dto.request.PartyCreateRequest;
import pbl2.sub119.backend.party.dto.request.PartyJoinRequest;
import pbl2.sub119.backend.party.dto.response.PartyCreateResponse;
import pbl2.sub119.backend.party.dto.response.PartyDetailResponse;
import pbl2.sub119.backend.party.dto.response.PartyListResponse;

@Tag(
        name = "Party API",
        description = "파티 생성, 조회, 참여 API"
)
public interface PartyDocs {

    @Operation(
            summary = "파티 생성",
            description = """
                    특정 상품 기준으로 새로운 파티를 생성합니다.
                    생성한 사용자는 파티장(HOST)으로 등록됩니다.
                    초기 모집 상태는 RECRUITING, 운영 상태는 WAITING_START입니다.
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
            @Parameter(description = "파티 생성 요청 정보", required = true)
            @RequestBody PartyCreateRequest request
    );

    @Operation(
            summary = "파티 상세 조회",
            description = "특정 파티의 상세 정보와 멤버 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "파티 조회 성공",
                            content = @Content(schema = @Schema(implementation = PartyDetailResponse.class))
                    )
            }
    )
    @GetMapping("/{partyId}")
    ResponseEntity<PartyDetailResponse> getPartyDetail(
            @Parameter(description = "파티 ID", required = true, example = "1")
            @PathVariable Long partyId
    );

    @Operation(
            summary = "상품별 파티 목록 조회",
            description = "특정 상품 기준으로 생성된 파티 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "파티 목록 조회 성공",
                            content = @Content(
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = PartyListResponse.class)
                                    )
                            )
                    )
            }
    )
    @GetMapping("/products/{productId}")
    ResponseEntity<List<PartyListResponse>> getPartiesByProduct(
            @Parameter(description = "상품 ID", required = true, example = "100")
            @PathVariable String productId
    );

    @Operation(
            summary = "파티 참여",
            description = """
                    모집 중(RECRUITING)인 파티에 참여합니다.
                    동일 파티 중복 참여는 허용되지 않으며,
                    정원 초과를 방지하기 위해 비관적 락을 사용합니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "파티 참여 성공"
                    )
            }
    )
    @PostMapping("/{partyId}/join")
    ResponseEntity<Void> joinParty(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId
    );
}