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
import pbl2.sub119.backend.concurrent.dto.request.LeaderActionRequest;
import pbl2.sub119.backend.concurrent.dto.request.ReportRequest;
import pbl2.sub119.backend.concurrent.dto.response.IncidentHistoryResponse;
import pbl2.sub119.backend.concurrent.dto.response.IncidentResult;
import pbl2.sub119.backend.concurrent.dto.response.ResolveResult;

@Tag(
        name = "Concurrent Issue API",
        description = "동시접속 위반 신고 및 파티장 조치 처리 API. 공유 계정형(ACCOUNT_SHARE) 파티에서만 사용합니다."
)
public interface ConcurrentIssueDocs {

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
