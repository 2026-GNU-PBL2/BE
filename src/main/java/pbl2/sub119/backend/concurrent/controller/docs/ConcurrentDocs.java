package pbl2.sub119.backend.concurrent.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import pbl2.sub119.backend.concurrent.dto.request.DeviceReportRequest;
import pbl2.sub119.backend.concurrent.dto.request.DeviceResponseRequest;
import pbl2.sub119.backend.concurrent.dto.request.LeaderActionRequest;
import pbl2.sub119.backend.concurrent.dto.request.ManualDeviceRegisterRequest;
import pbl2.sub119.backend.concurrent.dto.request.ReportRequest;
import pbl2.sub119.backend.concurrent.dto.response.CredentialResponse;
import pbl2.sub119.backend.concurrent.dto.response.DeviceRegisterResult;
import pbl2.sub119.backend.concurrent.dto.response.DeviceReportResult;
import pbl2.sub119.backend.concurrent.dto.response.DeviceResponseResult;
import pbl2.sub119.backend.concurrent.dto.response.IncidentHistoryResponse;
import pbl2.sub119.backend.concurrent.dto.response.IncidentResult;
import pbl2.sub119.backend.concurrent.dto.response.OttServicePlanResponse;
import pbl2.sub119.backend.concurrent.dto.response.ResolveResult;
import pbl2.sub119.backend.concurrent.dto.response.ViolationHistoryResponse;

@Tag(name = "Concurrent API", description = "동시접속 제어 API. 공유 계정형(ACCOUNT_SHARE) 파티에서 사용합니다.")
public class ConcurrentDocs {

    private ConcurrentDocs() {}

    @Tag(name = "Concurrent API")
    public interface Issue {

        @Operation(
                summary = "동시접속 위반 신고",
                description = """
                        파티 멤버가 동시접속 위반을 신고합니다.

                        이 API는 아래 화면에서 사용합니다.
                        - 파티 상세 화면 → 동시접속 위반 신고 버튼
                        - 공유 계정형(ACCOUNT_SHARE) 파티에서만 호출 가능합니다.

                        경고 단계 안내
                        - 1차 신고 : 파티 전체에 경고 알림이 발송되고 파티장에게 24시간 내 조치 안내가 전달됩니다.
                        - 2차 신고 (기존 1차 경고가 있는 경우) : 해체 예정 상태로 전환되고 익일 자정에 파티가 해체됩니다.

                        상태값 안내 (warningLevel)
                        - FIRST : 1차 경고 상태입니다.
                        - SECOND : 2차 경고, 해체 예정 상태입니다.

                        상태값 안내 (status)
                        - FIRST_WARNING_SENT : 1차 경고 발송 완료 상태입니다.
                        - DISSOLUTION_SCHEDULED : 해체 일정이 확정된 상태입니다.

                        응답 필드 안내
                        - hostDeadline : 파티장이 조치를 완료해야 하는 마감 시각입니다.
                        - dissolutionDate : 해체 예정일입니다. 1차 경고 시에는 null 입니다.
                        """,
                requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                        required = true,
                        content = @Content(
                                schema = @Schema(implementation = ReportRequest.class),
                                examples = {
                                        @ExampleObject(
                                                name = "위반 신고 예시",
                                                summary = "동시접속 위반 의심 신고",
                                                value = """
                                                        {
                                                          "reportType": "동시접속 위반 의심"
                                                        }
                                                        """
                                        )
                                }
                        )
                ),
                responses = {
                        @ApiResponse(
                                responseCode = "200",
                                description = "신고 처리 성공",
                                content = @Content(
                                        schema = @Schema(implementation = IncidentResult.class),
                                        examples = {
                                                @ExampleObject(
                                                        name = "1차 경고 응답 예시",
                                                        summary = "이전 경고가 없을 때 — 1차 경고 발송",
                                                        value = """
                                                                {
                                                                  "incidentId": 1,
                                                                  "partyId": 42,
                                                                  "warningLevel": "FIRST",
                                                                  "status": "FIRST_WARNING_SENT",
                                                                  "hostDeadline": "2025-05-23T14:00:00",
                                                                  "dissolutionDate": null
                                                                }
                                                                """
                                                ),
                                                @ExampleObject(
                                                        name = "2차 경고 응답 예시",
                                                        summary = "기존 1차 경고가 있을 때 — 해체 예정 전환",
                                                        value = """
                                                                {
                                                                  "incidentId": 2,
                                                                  "partyId": 42,
                                                                  "warningLevel": "SECOND",
                                                                  "status": "DISSOLUTION_SCHEDULED",
                                                                  "hostDeadline": "2025-05-23T02:00:00",
                                                                  "dissolutionDate": "2025-05-24"
                                                                }
                                                                """
                                                )
                                        }
                                )
                        ),
                        @ApiResponse(responseCode = "400", description = "CONCURRENT001 - 공유 계정형 파티가 아닙니다."),
                        @ApiResponse(responseCode = "403", description = "CONCURRENT003 - 해당 파티의 멤버가 아닙니다.")
                }
        )
        @PostMapping("/{partyId}")
        ResponseEntity<IncidentResult> report(
                @Parameter(hidden = true) @Auth Accessor accessor,
                @PathVariable Long partyId,
                @RequestBody @Valid ReportRequest request
        );

        @Operation(
                summary = "파티장 조치 완료 처리",
                description = """
                        파티장이 비밀번호 변경 등 조치를 완료한 뒤 호출합니다.

                        이 API는 아래 화면에서 사용합니다.
                        - 파티장 경고 알림 상세 화면 → 조치 완료 버튼
                        - 비밀번호 변경 완료 후 처리 확인 버튼

                        조치 완료 시 동작
                        - 인시던트 상태가 RESOLVED 로 변경됩니다.
                        - 파티의 경고 레벨이 0으로 초기화됩니다.
                        - 해체 예정일(dissolutionDate)이 제거됩니다.

                        안내
                        - 이미 RESOLVED 또는 PARTY_DISSOLVED 상태인 인시던트에는 호출할 수 없습니다.
                        - 요청 바디의 incidentId 는 신고 API 응답의 incidentId 값을 그대로 사용합니다.
                        """,
                requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                        required = true,
                        content = @Content(
                                schema = @Schema(implementation = LeaderActionRequest.class),
                                examples = {
                                        @ExampleObject(
                                                name = "조치 완료 요청 예시",
                                                value = """
                                                        {
                                                          "incidentId": 1
                                                        }
                                                        """
                                        )
                                }
                        )
                ),
                responses = {
                        @ApiResponse(
                                responseCode = "200",
                                description = "조치 완료 처리 성공",
                                content = @Content(schema = @Schema(implementation = ResolveResult.class))
                        ),
                        @ApiResponse(responseCode = "404", description = "CONCURRENT002 - 인시던트를 찾을 수 없습니다."),
                        @ApiResponse(responseCode = "400", description = "CONCURRENT004 - 이미 처리된 인시던트입니다.")
                }
        )
        @PostMapping("/{partyId}/resolve")
        ResponseEntity<ResolveResult> resolve(
                @Parameter(hidden = true) @Auth Accessor accessor,
                @PathVariable Long partyId,
                @RequestBody @Valid LeaderActionRequest request
        );

        @Operation(
                summary = "파티 인시던트 이력 조회",
                description = """
                        파티의 동시접속 위반 인시던트 이력을 조회합니다.

                        이 API는 아래 화면에서 사용합니다.
                        - 파티 상세 화면 → 경고 이력 탭
                        - 파티 관리 화면 → 동시접속 현황 확인

                        상태값 안내 (status)
                        - OPEN : 처리 대기 중인 상태입니다.
                        - FIRST_WARNING_SENT : 1차 경고 발송 완료 상태입니다.
                        - DISSOLUTION_SCHEDULED : 해체 일정이 확정된 상태입니다.
                        - RESOLVED : 파티장 조치로 해결된 상태입니다.
                        - PARTY_DISSOLVED : 파티가 해체된 상태입니다.

                        상태값 안내 (detectionSource)
                        - MEMBER_REPORT : 멤버 신고로 감지된 인시던트입니다.
                        """,
                responses = {
                        @ApiResponse(
                                responseCode = "200",
                                description = "인시던트 이력 조회 성공",
                                content = @Content(
                                        array = @ArraySchema(schema = @Schema(implementation = IncidentHistoryResponse.class))
                                )
                        )
                }
        )
        @GetMapping("/{partyId}/history")
        ResponseEntity<List<IncidentHistoryResponse>> getHistory(
                @Parameter(hidden = true) @Auth Accessor accessor,
                @PathVariable Long partyId
        );
    }

    @Tag(name = "Concurrent API")
    public interface Credential {

        @Operation(
                summary = "공유 계정 이용 정보 조회",
                description = """
                        파티장 및 파티원이 공유 계정의 이메일과 비밀번호 평문을 조회합니다.

                        이 API는 아래 화면에서 사용합니다.
                        - 파티장 화면 → 공유 계정 정보 확인 버튼
                        - 파티원 화면 → 계정 정보 보기 버튼
                        - 비밀번호 변경 알림 수신 후 새 비밀번호 확인 화면

                        사용 조건
                        - 해당 파티의 파티장 또는 파티원만 조회할 수 있습니다.
                        - 공유 계정형(ACCOUNT_SHARE) 파티에서만 사용합니다.

                        보안 정책
                        - 비밀번호는 복호화된 평문으로 반환됩니다.
                        - 이 API는 명시적으로 사용자가 조회 요청을 한 경우에만 호출하는 것을 권장합니다.

                        응답 필드 안내
                        - sharedAccountEmail : 공유 계정 이메일입니다.
                        - sharedAccountPassword : 공유 계정 비밀번호 평문입니다.
                        """,
                responses = {
                        @ApiResponse(
                                responseCode = "200",
                                description = "이용 정보 조회 성공",
                                content = @Content(schema = @Schema(implementation = CredentialResponse.class))
                        ),
                        @ApiResponse(responseCode = "403", description = "CONCURRENT003 - 해당 파티의 멤버가 아닙니다.")
                }
        )
        @GetMapping("/{partyId}")
        ResponseEntity<CredentialResponse> getCredential(
                @Parameter(hidden = true) @Auth Accessor accessor,
                @PathVariable Long partyId
        );

        @Operation(
                summary = "비밀번호 변경 후 파티원 재알림",
                description = """
                        파티장이 공유 계정 비밀번호를 변경한 뒤 파티원 전체에게 변경 알림을 발송합니다.

                        이 API는 아래 화면에서 사용합니다.
                        - 파티장 이용 관리 화면 → 비밀번호 변경 완료 후 알림 보내기 버튼
                        - 경고 조치 완료 화면 → 비밀번호 변경 안내 발송 버튼

                        동작 안내
                        - 파티장 본인을 제외한 모든 파티원에게 "이용 정보가 변경되었습니다" 알림을 발송합니다.
                        - 파티원은 이 알림을 받으면 계정 정보 조회 API를 통해 변경된 비밀번호를 확인할 수 있습니다.

                        안내
                        - 파티장만 호출할 수 있습니다.
                        - 응답 바디는 없습니다.
                        """,
                responses = {
                        @ApiResponse(responseCode = "200", description = "알림 발송 성공"),
                        @ApiResponse(responseCode = "403", description = "CONCURRENT003 - 해당 파티의 멤버가 아닙니다.")
                }
        )
        @PostMapping("/{partyId}/notify-update")
        ResponseEntity<Void> notifyUpdate(
                @Parameter(hidden = true) @Auth Accessor accessor,
                @PathVariable Long partyId
        );
    }

    @Tag(name = "Concurrent API")
    public interface DeviceAlert {

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

    @Tag(name = "Concurrent API")
    public interface OttServicePlan {

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

    @Tag(name = "Concurrent API")
    public interface PartyMemberDevice {

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

    @Tag(name = "Concurrent API")
    public interface Violation {

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
}
