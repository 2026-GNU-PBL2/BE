package pbl2.sub119.backend.party.provision.controller.docs;

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
import pbl2.sub119.backend.party.provision.dto.request.PartyProvisionResetRequest;
import pbl2.sub119.backend.party.provision.dto.request.PartyProvisionSetupRequest;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionConfirmResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionDashboardResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionMeResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionMemberResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionSetupResponse;

@Tag(name = "Party Provision API", description = "파티 이용 정보 등록, 확인, 조회 API")
public interface PartyProvisionDocs {

    @Operation(
            summary = "파티 이용 정보 등록",
            description = """
                    파티장이 결제 완료 후 파티 이용에 필요한 정보를 등록합니다.

                    이용 정보 제공 방식
                    - ACCOUNT_SHARED : 파티장이 공유 계정 이메일과 비밀번호를 등록하는 방식입니다.
                    - INVITE_LINK : 파티장이 OTT 추가 회원 초대 링크를 등록하는 방식입니다.

                    request body 입력 안내
                    - 공유계정형이면 sharedAccountEmail, sharedAccountPassword 를 입력합니다.
                    - 초대링크형이면 inviteValue 를 입력합니다.
                    - 사용하지 않는 필드는 null 로 보내면 됩니다.
                    - provisionGuide 는 파티원이 실제 이용 절차를 진행할 때 보는 안내 문구입니다.

                    상태값 안내
                    - REQUIRED : 파티원이 아직 이용 확인을 하지 않아 확인이 필요한 상태입니다.
                    - ACTIVE : 파티원이 이용 확인까지 완료하여 현재 정상 이용 중인 상태입니다.
                    - RESET_REQUIRED : 파티장이 이용 정보를 다시 변경하여 파티원이 다시 확인해야 하는 상태입니다.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PartyProvisionSetupRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "공유계정형 요청 예시",
                                            summary = "공유 계정 이메일/비밀번호 제공 방식",
                                            value = """
                                                    {
                                                      "provisionType": "ACCOUNT_SHARED",
                                                      "inviteValue": null,
                                                      "sharedAccountEmail": "submate.test@gmail.com",
                                                      "sharedAccountPassword": "Test1234!",
                                                      "provisionGuide": "로그인 후 본인 프로필을 새로 만들어 사용해 주세요."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "초대링크형 요청 예시",
                                            summary = "OTT 추가 회원 초대 링크 제공 방식",
                                            value = """
                                                    {
                                                      "provisionType": "INVITE_LINK",
                                                      "inviteValue": "https://www.netflix.com/invite/example",
                                                      "sharedAccountEmail": null,
                                                      "sharedAccountPassword": null,
                                                      "provisionGuide": "링크 접속 후 초대를 완료한 뒤 이용 확인 완료를 눌러 주세요."
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
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
                    파티장이 파티 이용 진행 현황을 조회합니다.

                    이 API는 아래 화면에서 사용합니다.
                    - 파티장 이용 관리 화면
                    - 이용 확인 완료 인원 수를 확인하는 화면
                    - 아직 확인하지 않은 멤버를 확인하는 화면

                    상태값 안내
                    - REQUIRED : 아직 이용 확인 전인 상태
                    - ACTIVE : 이용 확인까지 끝나 현재 정상 이용 중인 상태
                    - RESET_REQUIRED : 이용 정보가 다시 바뀌어 재확인이 필요한 상태
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

                    상태값 안내
                    - REQUIRED : 아직 이용 확인을 하지 않은 멤버
                    - ACTIVE : 이용 확인까지 끝나 현재 정상 이용 중인 멤버
                    - RESET_REQUIRED : 파티장이 정보를 다시 바꿔 재확인이 필요한 멤버
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
            summary = "파티원 이용 확인 완료",
            description = """
                    파티원이 초대 링크 활성화 또는 공유계정 로그인 절차를 끝낸 뒤 이용 확인 완료를 처리합니다.

                    이 API는 아래 화면에서 사용합니다.
                    - 초대 링크 활성화 완료 후 확인 버튼
                    - 공유계정 로그인 및 프로필 설정 완료 후 확인 버튼

                    상태값 안내
                    - 호출 전 : REQUIRED 상태에서 이용 확인 완료를 진행할 수 있습니다.
                    - 호출 후 : ACTIVE 상태로 변경됩니다.
                    - RESET_REQUIRED 상태라면 파티장이 이용 정보를 다시 반영한 뒤 다시 진행해야 할 수 있습니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이용 확인 완료 성공",
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

                    예를 들어 이런 경우에 사용할 수 있습니다.
                    - 공유 계정 이메일 변경
                    - 공유 계정 비밀번호 변경
                    - 초대 링크 재발급
                    - 이용 안내 문구 변경

                    이 API를 호출하면 기존 파티원은 다시 확인 절차를 진행해야 합니다.

                    request body 입력 안내
                    - provisionMessage 는 재설정 사유 또는 파티원에게 보여줄 안내 메시지입니다.

                    상태값 안내
                    - 기존 ACTIVE 멤버도 RESET_REQUIRED 로 변경될 수 있습니다.
                    - 이후 파티원은 다시 이용 정보를 확인해야 합니다.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PartyProvisionResetRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "이용 재설정 요청 예시",
                                            summary = "공유 계정 또는 링크를 다시 바꾼 경우",
                                            value = """
                                                    {
                                                      "provisionMessage": "공유 계정 정보가 변경되어 다시 확인해 주세요."
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
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
                    파티원이 본인에게 필요한 이용 정보를 조회합니다.

                    이 API는 아래 화면에서 사용합니다.
                    - 초대 링크 확인 화면
                    - 공유계정 아이디/비밀번호 확인 화면
                    - 이용 완료 후 계정 정보와 이용 가이드를 다시 보는 화면
                    - 이용 재설정 후 다시 절차를 진행하는 화면

                    상태값 안내
                    - REQUIRED : 아직 이용 확인 전인 상태입니다.
                    - ACTIVE : 이용 확인까지 끝나 현재 정상 이용 중인 상태입니다.
                    - RESET_REQUIRED : 파티장이 정보를 다시 바꿔 다시 확인해야 하는 상태입니다.
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