package pbl2.sub119.backend.concurrent.controller.docs;

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
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.concurrent.dto.response.ViolationHistoryResponse;

@Tag(
        name = "Violation API",
        description = "사용자 동시접속 위반 이력 조회 API"
)
public interface ViolationDocs {

    @Operation(
            summary = "내 위반 이력 조회",
            description = """
                    로그인한 사용자의 동시접속 위반 이력을 조회합니다.

                    이 API는 아래 화면에서 사용합니다.
                    - 마이페이지 → 위반 이력 탭
                    - 서비스 이용 제한 안내 화면

                    상태값 안내 (violationType)
                    - FIRST_WARNING : 동시접속 1차 경고를 받은 이력입니다.
                    - PARTY_DISSOLVED : 소속 파티가 동시접속 위반으로 해체된 이력입니다.
                    - DEVICE_ALERT_NO_RESPONSE : 기기 감지 알림에 응답하지 않아 기록된 이력입니다.

                    응답 필드 안내
                    - weight : 위반 가중치 값입니다. 누적 가중치가 임계값을 넘으면 이용 제한이 적용될 수 있습니다.
                    - createdAt : 위반 이력이 기록된 시각입니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "위반 이력 조회 성공",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = ViolationHistoryResponse.class))
                            )
                    )
            }
    )
    @GetMapping("/me")
    ResponseEntity<List<ViolationHistoryResponse>> getMyViolations(
            @Parameter(hidden = true) @Auth Accessor accessor
    );
}
