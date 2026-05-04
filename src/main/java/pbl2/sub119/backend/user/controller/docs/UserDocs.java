package pbl2.sub119.backend.user.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;

import pbl2.sub119.backend.user.dto.request.PhoneVerificationConfirmRequest;
import pbl2.sub119.backend.user.dto.request.PhoneVerificationRequest;
import pbl2.sub119.backend.user.dto.request.UserRequest;
import pbl2.sub119.backend.user.dto.response.DuplicateCheckResponse;
import pbl2.sub119.backend.user.dto.response.UserResponse;
import pbl2.sub119.backend.user.dto.response.UserSignUpResponse;
import pbl2.sub119.backend.user.dto.response.UserUpdateResponse;

@Tag(
        name = "User API",
        description = "회원 정보 등록, 조회, 수정, 탈퇴 API"
)
public interface UserDocs {

    @Operation(
            summary = "회원가입 완료",
            description = """
                    소셜 로그인 후 최초 생성된 회원에 대해
                    전화번호, 자사 이메일 아이디, 닉네임, PIN 번호를 등록합니다.
                    회원 상태를 PENDING_SIGNUP에서 ACTIVE로 전환합니다.
                    휴대폰 인증(/phone/verify/confirm)이 선행되어야 합니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "회원가입 완료",
                            content = @Content(schema = @Schema(implementation = UserSignUpResponse.class))
                    )
            }
    )
    @PostMapping
    ResponseEntity<UserSignUpResponse> signUp(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @Parameter(description = "회원가입 시 입력할 추가 정보", required = true)
            UserRequest request
    );

    @Operation(
            summary = "내 정보 조회",
            description = "현재 로그인한 회원의 정보를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = UserResponse.class))
                    )
            }
    )
    @GetMapping
    ResponseEntity<UserResponse> find(
            @Parameter(hidden = true) @Auth Accessor accessor
    );

    @Operation(
            summary = "회원 정보 수정",
            description = "현재 로그인한 회원의 전화번호, 자사 이메일 아이디, 닉네임, PIN 번호를 수정합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "수정 완료",
                            content = @Content(schema = @Schema(implementation = UserUpdateResponse.class))
                    )
            }
    )
    @PatchMapping
    ResponseEntity<UserUpdateResponse> update(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @Parameter(description = "수정할 회원 정보", required = true)
            UserRequest request
    );

    @Operation(
            summary = "회원 탈퇴",
            description = """
                    회원을 소프트 삭제 처리합니다.
                    회원 상태를 WITHDRAWN으로 변경합니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "탈퇴 성공"
                    )
            }
    )
    @DeleteMapping
    ResponseEntity<Void> withdraw(
            @Parameter(hidden = true) @Auth Accessor accessor
    );

    @Operation(
            summary = "이메일 중복확인",
            description = "서브메이트 이메일 아이디(@submate.cloud 앞 부분)의 사용 가능 여부를 확인합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "중복확인 완료",
                            content = @Content(schema = @Schema(implementation = DuplicateCheckResponse.class))
                    )
            }
    )
    @GetMapping("/check/email")
    ResponseEntity<DuplicateCheckResponse> checkEmail(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @Parameter(description = "이메일 아이디 (도메인 제외)", example = "hajin", required = true)
            @RequestParam String email
    );

    @Operation(
            summary = "닉네임 중복확인",
            description = "닉네임의 사용 가능 여부를 확인합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "중복확인 완료",
                            content = @Content(schema = @Schema(implementation = DuplicateCheckResponse.class))
                    )
            }
    )
    @GetMapping("/check/nickname")
    ResponseEntity<DuplicateCheckResponse> checkNickname(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @Parameter(description = "닉네임", example = "하진", required = true)
            @RequestParam String nickname
    );

    @Operation(
            summary = "휴대폰 인증번호 요청",
            description = "입력된 휴대폰 번호로 6자리 인증번호를 SMS 발송합니다. 인증번호 유효시간은 3분입니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "인증번호 발송 완료")
            }
    )
    @PostMapping("/phone/verify/request")
    ResponseEntity<Void> requestPhoneVerification(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @Parameter(description = "휴대폰 번호", required = true) PhoneVerificationRequest request
    );

    @Operation(
            summary = "휴대폰 인증번호 확인",
            description = """
                    SMS로 받은 6자리 인증번호를 검증합니다.
                    인증 성공 시 10분간 인증 완료 상태가 유지되며,
                    이 시간 내에 회원가입을 완료해야 합니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "인증 완료")
            }
    )
    @PostMapping("/phone/verify/confirm")
    ResponseEntity<Void> confirmPhoneVerification(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @Parameter(description = "휴대폰 번호 및 인증번호", required = true) PhoneVerificationConfirmRequest request
    );
}
