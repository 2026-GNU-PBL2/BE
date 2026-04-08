package pbl2.sub119.backend.party.provision.controller.docs;

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
import pbl2.sub119.backend.party.provision.dto.request.PartyProvisionResetRequest;
import pbl2.sub119.backend.party.provision.dto.request.PartyProvisionSetupRequest;
import pbl2.sub119.backend.party.provision.dto.response.*;

@Tag(name = "Party Operation API", description = "파티 운영 등록, 확인, 조회 API")
public interface PartyProvisionDocs {

    @Operation(
            summary = "파티 운영 정보 등록",
            description = """
                    파티장이 결제 완료 후 운영 정보를 등록합니다.

                    operationType:
                    - INVITE_LINK
                    - ACCOUNT_SHARED
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "운영 정보 등록 성공",
                            content = @Content(schema = @Schema(implementation = PartyProvisionSetupResponse.class))
                    )
            }
    )
    @PostMapping("/{partyId}/operation")
    ResponseEntity<PartyProvisionSetupResponse> setupOperation(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId,
            @RequestBody @Valid PartyProvisionSetupRequest request
    );

    @Operation(
            summary = "파티 운영 대시보드 조회",
            description = "파티 운영 방식, 운영 상태, 완료 인원 수, 멤버별 운영 상태를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "운영 대시보드 조회 성공",
                            content = @Content(schema = @Schema(implementation = PartyProvisionDashboardResponse.class))
                    )
            }
    )
    @GetMapping("/{partyId}/operation")
    ResponseEntity<PartyProvisionDashboardResponse> getOperationDashboard(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId
    );

    @Operation(
            summary = "파티 운영 멤버 목록 조회",
            description = "파티 운영 멤버 상태 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "운영 멤버 목록 조회 성공",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = PartyProvisionMemberResponse.class))
                            )
                    )
            }
    )
    @GetMapping("/{partyId}/operation/members")
    ResponseEntity<List<PartyProvisionMemberResponse>> getOperationMembers(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId
    );

    @Operation(
            summary = "파티원 운영 확인",
            description = "파티원이 초대 수락 또는 계정 로그인 완료 후 확인 처리합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "운영 확인 성공",
                            content = @Content(schema = @Schema(implementation = PartyProvisionConfirmResponse.class))
                    )
            }
    )
    @PostMapping("/{partyId}/operation/confirm")
    ResponseEntity<PartyProvisionConfirmResponse> confirmOperation(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId
    );

    @Operation(
            summary = "운영 재설정",
            description = "파티장이 운영 재설정 상태로 전환합니다."
    )
    @PostMapping("/{partyId}/operation/reset")
    ResponseEntity<Void> resetOperation(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId,
            @RequestBody @Valid PartyProvisionResetRequest request
    );

    @Operation(
            summary = "내 운영 정보 조회",
            description = """
                본인에게 필요한 파티 운영 상세 정보를 조회합니다.

                operationType:
                - INVITE_LINK: inviteValue 반환
                - ACCOUNT_SHARED: sharedAccountEmail, sharedAccountPassword 반환
                """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "내 운영 정보 조회 성공",
                            content = @Content(schema = @Schema(implementation = PartyProvisionMeResponse.class))
                    )
            }
    )
    @GetMapping("/{partyId}/operation/me")
    ResponseEntity<PartyProvisionMeResponse> getMyOperationInfo(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId
    );
}