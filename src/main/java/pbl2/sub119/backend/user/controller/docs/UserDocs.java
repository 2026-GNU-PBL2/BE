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
import pbl2.submate.backend.auth.aop.Auth;
import pbl2.submate.backend.auth.entity.Accessor;
import pbl2.sub119.backend.user.dto.request.UserRequest;
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
}