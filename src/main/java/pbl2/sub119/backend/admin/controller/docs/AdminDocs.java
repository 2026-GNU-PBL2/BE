package pbl2.sub119.backend.admin.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;

@Tag(
        name = "Admin API",
        description = "관리자 전용 API"
)
public interface AdminDocs {

    @Operation(
            summary = "관리자 권한 확인",
            description = """
                    현재 로그인한 사용자가 관리자(ADMIN)인지 확인하는 API입니다.
                    관리자 권한이 없는 경우 접근할 수 없습니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "관리자 접근 성공",
                            content = @Content(schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "관리자 권한 없음"
                    )
            }
    )
    @GetMapping("/check")
    ResponseEntity<String> checkAdmin(
            @Parameter(hidden = true) @Auth Accessor accessor
    );
}