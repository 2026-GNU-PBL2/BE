package pbl2.sub119.backend.concurrent.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.concurrent.dto.response.CredentialResponse;

@Tag(
        name = "Credential API",
        description = "공유 계정 이용 정보 조회 및 변경 알림 API. 공유 계정형(ACCOUNT_SHARE) 파티에서만 사용합니다."
)
public interface CredentialDocs {

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
