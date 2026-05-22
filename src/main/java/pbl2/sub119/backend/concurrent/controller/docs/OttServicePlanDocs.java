package pbl2.sub119.backend.concurrent.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import pbl2.sub119.backend.concurrent.dto.response.OttServicePlanResponse;

@Tag(
        name = "OTT Service Plan API",
        description = "OTT 서비스별 플랜 정보(동시접속 허용 수) 조회 API"
)
public interface OttServicePlanDocs {

    @Operation(
            summary = "OTT 서비스 플랜 목록 조회",
            description = """
                    OTT 서비스별 플랜 정보를 조회합니다. 인증 없이 호출 가능합니다.

                    이 API는 아래 화면에서 사용합니다.
                    - 파티 생성 화면 → 서비스 플랜 선택 드롭다운
                    - 공유 계정형 파티 신청 전 플랜 조건 확인 화면

                    응답 필드 안내
                    - serviceName : OTT 서비스 식별자입니다. (예: NETFLIX, TVING, WATCHA, DISNEY_PLUS, APPLE_TV, WAVVE, LAFTEL)
                    - planName : 플랜명입니다. 해상도가 포함됩니다. (예: 프리미엄(4K), 프리미엄(FHD), Apple TV+)
                    - concurrentLimit : 동시에 접속 가능한 기기 수입니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "플랜 목록 조회 성공",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = OttServicePlanResponse.class))
                            )
                    )
            }
    )
    @GetMapping
    ResponseEntity<List<OttServicePlanResponse>> getPlans();
}
