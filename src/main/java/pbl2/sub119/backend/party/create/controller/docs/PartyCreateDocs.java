package pbl2.sub119.backend.party.create.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
                    파티 생성 전에 예상 금액 정보를 미리 확인합니다.

                    이 API는 아래 화면에서 사용합니다.
                    - 파티 생성 화면
                    - 인원 수를 바꿔보며 금액 변화를 확인하는 화면

                    안내
                    - capacity 는 파티장 포함 전체 인원 수입니다.
                    - 실제 모집되는 파티원 수는 capacity - 1 입니다.
                    - 파티 생성 전에 파티장이 실제로 부담할 금액, 파티원이 낼 금액, 정산 관련 금액을 미리 확인할 수 있습니다.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PartyCreateSummaryRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "2인 파티 예시",
                                            summary = "파티장 포함 2명 파티 생성 전 요약 조회",
                                            value = """
                                                    {
                                                      "productId": "f22ad776-dc20-441e-9d37-65d40707d6e0",
                                                      "capacity": 2
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "4인 파티 예시",
                                            summary = "파티장 포함 4명 파티 생성 전 요약 조회",
                                            value = """
                                                    {
                                                      "productId": "f22ad776-dc20-441e-9d37-65d40707d6e0",
                                                      "capacity": 4
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "파티 생성 전 요약 정보 조회 성공",
                            content = @Content(schema = @Schema(implementation = PartyCreateSummaryResponse.class))
                    )
            }
    )
    @PostMapping("/create-preview")
    ResponseEntity<PartyCreateSummaryResponse> getCreateSummary(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @RequestBody @Valid PartyCreateSummaryRequest request
    );

    @Operation(
            summary = "파티 생성",
            description = """
                    선택한 구독 상품으로 새 파티를 생성합니다.

                    이 API는 아래 화면에서 사용합니다.
                    - 파티 생성 완료 버튼
                    - 파티 생성 최종 확인 화면

                    안내
                    - capacity 는 파티장 포함 전체 인원 수입니다.
                    - 실제 모집되는 파티원 수는 capacity - 1 입니다.
                    - 파티를 생성한 사용자는 파티장으로 등록됩니다.
                    - 생성 직후 파티원 모집을 시작할 수 있습니다.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PartyCreateRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "2인 파티 생성 예시",
                                            summary = "파티장 포함 2명 파티 생성",
                                            value = """
                                                    {
                                                      "productId": "f22ad776-dc20-441e-9d37-65d40707d6e0",
                                                      "capacity": 2
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "4인 파티 생성 예시",
                                            summary = "파티장 포함 4명 파티 생성",
                                            value = """
                                                    {
                                                      "productId": "f22ad776-dc20-441e-9d37-65d40707d6e0",
                                                      "capacity": 4
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
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