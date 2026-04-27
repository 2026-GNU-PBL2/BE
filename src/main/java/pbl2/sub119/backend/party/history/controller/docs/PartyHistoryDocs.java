package pbl2.sub119.backend.party.history.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.party.history.dto.PartyHistoryResponse;

@Tag(
        name = "Party History API",
        description = "마이페이지 파티 히스토리 API"
)
public interface PartyHistoryDocs {

    @Operation(
            summary = "내 파티 히스토리 조회",
            description = """
                    마이페이지에서 사용자가 참여했던 파티 이력을 조회합니다.

                    이 API는 아래 화면에서 사용합니다.
                    - 마이페이지 > 파티 히스토리

                    조회 기준:
                    - party_member 기준으로 사용자가 실제 참여했던 파티를 조회합니다.
                    - 자동 매칭 신청 내역은 포함하지 않습니다.

                    응답 데이터:
                    - partyId : 파티 PK
                    - displayPartyId : 화면 표시용 파티 ID
                    - productId : 상품 ID
                    - productName : 상품명
                    - role : 파티 내 역할
                    - status : 파티 히스토리 상태
                    - startAt : 이용 시작일
                    - endAt : 이용 종료일

                    역할(role) 안내:
                    - HOST : 파티장
                    - MEMBER : 파티원

                    상태값(status) 안내:
                    - USING : 이용 중
                    - ENDED : 종료

                    기간 기준:
                    - startAt : party_member.service_start_at
                    - endAt : party_member.service_end_at
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "내 파티 히스토리 조회 성공",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = PartyHistoryResponse.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "토큰 없음 또는 유효하지 않은 토큰"
                    )
            }
    )
    @GetMapping
    ResponseEntity<List<PartyHistoryResponse>> getMyPartyHistories(
            @Parameter(hidden = true) @Auth Accessor accessor
    );
}