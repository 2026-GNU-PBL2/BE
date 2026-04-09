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
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionConfirmResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionDashboardResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionMeResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionMemberResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionSetupResponse;

@Tag(name = "Party Provision API", description = "파티 provision 등록, 확인, 조회 API")
public interface PartyProvisionDocs {

    @Operation(
            summary = "파티 이용 정보 등록",
            description = """
                    파티장이 결제 완료 후 파티 이용에 필요한 정보를 등록합니다.
                    
                    - 공유계정형이면 공유 아이디/비밀번호를 저장합니다.
                    - 초대형이면 추가 회원 초대 링크를 저장합니다.
                    - 이미 등록된 이용 정보를 다시 저장하면 기존 멤버는 다시 확인해야 합니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이용 정보 등록 성공",
                            content = @Content(schema = @Schema(implementation = PartyProvisionSetupResponse.class))
                    )
            }
    )
    @PostMapping("/{partyId}/provision")
    ResponseEntity<PartyProvisionSetupResponse> setupProvision(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId,
            @RequestBody @Valid PartyProvisionSetupRequest request
    );

    @Operation(
            summary = "파티 이용 현황 조회",
            description = """
                    파티 이용 진행 상태를 조회합니다.
                    
                    이 API는 아래 화면에서 사용합니다.
                    - 파티장이 이용 진행 현황을 확인할 때
                    - 이용 완료 인원 수를 볼 때
                    - 멤버별 완료 여부를 확인할 때
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이용 현황 조회 성공",
                            content = @Content(schema = @Schema(implementation = PartyProvisionDashboardResponse.class))
                    )
            }
    )
    @GetMapping("/{partyId}/provision")
    ResponseEntity<PartyProvisionDashboardResponse> getProvisionDashboard(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId
    );

    @Operation(
            summary = "파티 이용 멤버 목록 조회",
            description = """
                    파티장이 이용 대상 멤버의 상태 목록을 조회합니다.
                    
                    이 API는 아래 화면에서 사용합니다.
                    - 파티장 이용 관리 화면
                    - 누가 아직 확인을 안 했는지 보는 화면
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이용 멤버 목록 조회 성공",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = PartyProvisionMemberResponse.class))
                            )
                    )
            }
    )
    @GetMapping("/{partyId}/provision/members")
    ResponseEntity<List<PartyProvisionMemberResponse>> getProvisionMembers(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId
    );

    @Operation(
            summary = "파티원 이용 완료 확인",
            description = """
                    파티원이 초대 링크 활성화 또는 공유계정 로그인 절차를 끝낸 뒤 확인 완료 처리합니다.
                    
                    이 API는 아래 화면에서 사용합니다.
                    - 초대 링크 활성화 완료 후 확인 버튼
                    - 공유계정 로그인 및 프로필 설정 완료 후 확인 버튼
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이용 완료 확인 성공",
                            content = @Content(schema = @Schema(implementation = PartyProvisionConfirmResponse.class))
                    )
            }
    )
    @PostMapping("/{partyId}/provision/confirm")
    ResponseEntity<PartyProvisionConfirmResponse> confirmProvision(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId
    );

    @Operation(
            summary = "파티 이용 재설정",
            description = """
                    파티장이 초대 링크나 공유계정 정보를 다시 설정해야 할 때 사용합니다.
                    
                    이 API를 호출하면 기존 파티원은 다시 확인 절차를 진행해야 합니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "이용 재설정 성공")
            }
    )
    @PostMapping("/{partyId}/provision/reset")
    ResponseEntity<Void> resetProvision(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId,
            @RequestBody @Valid PartyProvisionResetRequest request
    );

    @Operation(
            summary = "내 이용 정보 조회",
            description = """
                    본인에게 필요한 이용 정보를 조회합니다.
                    
                    이 API는 아래 화면에서 사용합니다.
                    - 파티원이 초대 링크를 확인하는 화면
                    - 파티원이 공유계정 아이디/비밀번호를 확인하는 화면
                    - 이용 완료 후 계정 정보/이용 가이드를 다시 보는 화면
                    - 이용 재설정 후 다시 절차를 진행하는 화면
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "내 이용 정보 조회 성공",
                            content = @Content(schema = @Schema(implementation = PartyProvisionMeResponse.class))
                    )
            }
    )
    @GetMapping("/{partyId}/provision/me")
    ResponseEntity<PartyProvisionMeResponse> getMyProvisionInfo(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long partyId
    );
}