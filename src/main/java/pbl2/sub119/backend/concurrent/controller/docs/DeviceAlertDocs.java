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
import pbl2.sub119.backend.concurrent.dto.request.DeviceReportRequest;
import pbl2.sub119.backend.concurrent.dto.request.DeviceResponseRequest;
import pbl2.sub119.backend.concurrent.dto.response.DeviceReportResult;
import pbl2.sub119.backend.concurrent.dto.response.DeviceResponseResult;

@Tag(
        name = "Device Alert API",
        description = "낯선 기기 감지 신고 및 응답 API"
)
public interface DeviceAlertDocs {

    @Operation(
            summary = "낯선 기기 감지 신고",
            description = """
                    파티장 또는 파티원이 낯선 기기를 감지했을 때 신고합니다.

                    이 API는 아래 화면에서 사용합니다.
                    - 파티장 화면 → OTT 이메일에서 새 기기 로그인 확인 후 '낯선 기기 감지됨' 신고 버튼
                    - 파티원 화면 → 동시 시청 제한 알림 수신 후 '낯선 기기 신고' 버튼

                    신고 후 동작
                    - 전체 파티원에게 "내 기기인지 확인해주세요" 알림이 발송됩니다.
                    - 각 파티원은 응답 API(/device-alerts/{alertId}/respond)로 응답합니다.
                    - 응답 기한은 24시간입니다.

                    응답 필드 안내
                    - alertId : 파티원이 /respond 호출 시 사용하는 감지 이벤트 ID입니다.
                    - notifiedCount : 알림을 받은 파티원 수입니다.
                    - expiresAt : 응답 기한입니다.
                    - registeredDevices : 해당 파티에 등록된 기기 목록입니다. 파티장이 신고한 기기와 대조하는 데 사용합니다.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = DeviceReportRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "기기 감지 신고 예시",
                                            value = """
                                                    {
                                                      "detectedDevice": "Windows PC",
                                                      "detectedLocation": "부산"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "신고 접수 성공",
                            content = @Content(
                                    schema = @Schema(implementation = DeviceReportResult.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                              "alertId": 10,
                                                              "partyId": 42,
                                                              "notifiedCount": 4,
                                                              "expiresAt": "2025-05-23T14:00:00",
                                                              "registeredDevices": [
                                                                {
                                                                  "userId": 1,
                                                                  "deviceType": "DESKTOP",
                                                                  "os": "Windows 11",
                                                                  "browser": "Chrome"
                                                                },
                                                                {
                                                                  "userId": 2,
                                                                  "deviceType": "MOBILE",
                                                                  "os": "Android 14",
                                                                  "browser": "Chrome Mobile"
                                                                }
                                                              ]
                                                            }
                                                            """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "CONCURRENT003 - 해당 파티의 멤버가 아닙니다.")
            }
    )
    @PostMapping("/{partyId}/report")
    ResponseEntity<DeviceReportResult> report(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId,
            @RequestBody @Valid DeviceReportRequest request
    );

    @Operation(
            summary = "기기 감지 알림 응답",
            description = """
                    낯선 기기 감지 알림을 받은 파티원이 해당 기기가 본인 기기인지 아닌지를 응답합니다.

                    이 API는 아래 화면에서 사용합니다.
                    - 기기 감지 알림 팝업 → "내 기기입니다" / "모르는 기기입니다" 버튼

                    응답 처리 안내
                    - 파티원 과반수가 "내 기기입니다"로 응답하면 CONFIRMED_MINE 상태로 종료됩니다.
                    - 파티원 과반수가 "모르는 기기입니다"로 응답하면 파티장에게 즉시 경보 알림이 발송됩니다.
                    - 응답 기한이 지난 알림(EXPIRED)에는 응답할 수 없습니다.

                    상태값 안내 (status)
                    - PENDING : 아직 응답이 집계 중인 상태입니다.
                    - CONFIRMED_MINE : 과반수가 내 기기로 응답한 상태입니다.
                    - REPORTED_UNKNOWN : 과반수가 모르는 기기로 응답해 파티장 알림이 발송된 상태입니다.
                    - EXPIRED : 응답 기한이 만료된 상태입니다.

                    응답 필드 안내
                    - mineCount : 현재까지 "내 기기입니다"로 응답한 인원 수입니다.
                    - unknownCount : 현재까지 "모르는 기기입니다"로 응답한 인원 수입니다.
                    - responseCount : 현재까지 응답한 전체 인원 수입니다.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = DeviceResponseRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "내 기기 응답 예시",
                                            summary = "해당 기기가 본인 기기인 경우",
                                            value = """
                                                    {
                                                      "isMyDevice": true
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "모르는 기기 응답 예시",
                                            summary = "해당 기기가 본인 기기가 아닌 경우",
                                            value = """
                                                    {
                                                      "isMyDevice": false
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "응답 처리 성공",
                            content = @Content(schema = @Schema(implementation = DeviceResponseResult.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "CONCURRENT005 - 감지 알림을 찾을 수 없습니다."),
                    @ApiResponse(responseCode = "400", description = "CONCURRENT006 - 만료된 감지 알림입니다.")
            }
    )
    @PostMapping("/{alertId}/respond")
    ResponseEntity<DeviceResponseResult> respond(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long alertId,
            @RequestBody @Valid DeviceResponseRequest request
    );
}
