package pbl2.sub119.backend.concurrent.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.concurrent.dto.request.ManualDeviceRegisterRequest;
import pbl2.sub119.backend.concurrent.dto.response.DeviceRegisterResult;

@Tag(
        name = "Party Member Device API",
        description = "파티 멤버 기기 수동 등록 API"
)
public interface PartyMemberDeviceDocs {

    @Operation(
            summary = "내 기기 수동 등록",
            description = """
                    파티원이 본인 기기를 직접 등록합니다.

                    이 API는 아래 화면에서 사용합니다.
                    - 파티 이용 화면 → 내 기기 관리 → 기기 추가 버튼
                    - 기기 감지 알림 응답 전 내 기기를 미리 등록해두는 화면

                    안내
                    - 공유 계정 이용 정보 조회 시 기기는 자동으로 등록됩니다.
                    - 이 API는 자동 수집되지 않은 기기(예: TV, 게임기 등)를 추가로 등록할 때 사용합니다.
                    - 해당 파티의 멤버만 등록할 수 있습니다.

                    요청 필드 안내
                    - deviceType : 기기 유형입니다. (예: PC, 모바일, 태블릿, TV)
                    - os : 운영체제입니다. (예: Windows 11, macOS, iOS 17, Android 14)
                    - browser : 브라우저 또는 앱 정보입니다. 선택값입니다. (예: Chrome, Safari, Netflix 앱)
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ManualDeviceRegisterRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "PC 등록 예시",
                                            value = """
                                                    {
                                                      "deviceType": "PC",
                                                      "os": "Windows 11",
                                                      "browser": "Chrome"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "TV 등록 예시",
                                            value = """
                                                    {
                                                      "deviceType": "TV",
                                                      "os": "Tizen",
                                                      "browser": "Netflix 앱"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "기기 등록 성공",
                            content = @Content(schema = @Schema(implementation = DeviceRegisterResult.class))
                    ),
                    @ApiResponse(responseCode = "403", description = "CONCURRENT003 - 해당 파티의 멤버가 아닙니다.")
            }
    )
    @PostMapping("/{partyId}")
    ResponseEntity<DeviceRegisterResult> registerDevice(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId,
            @RequestBody @Valid ManualDeviceRegisterRequest request
    );
}
