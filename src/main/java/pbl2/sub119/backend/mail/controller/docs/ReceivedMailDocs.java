package pbl2.sub119.backend.mail.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.mail.dto.response.ReceivedMailDetailResponse;
import pbl2.sub119.backend.mail.dto.response.ReceivedMailResponse;

import java.util.List;

@Tag(
        name = "Received Mail API",
        description = "수신 메일 조회 API"
)
public interface ReceivedMailDocs {

    @Operation(
            summary = "내 메일함 조회",
            description = "현재 로그인한 사용자의 수신 메일 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = ReceivedMailResponse.class))
                    )
            }
    )
    @GetMapping
    ResponseEntity<List<ReceivedMailResponse>> getMyMails(
            @Parameter(hidden = true) @Auth Accessor accessor
    );

    @Operation(
            summary = "메일 상세 조회",
            description = "현재 로그인한 사용자의 특정 수신 메일을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = ReceivedMailDetailResponse.class))
                    )
            }
    )
    @GetMapping("/{mailId}")
    ResponseEntity<ReceivedMailDetailResponse> getMail(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long mailId
    );
}