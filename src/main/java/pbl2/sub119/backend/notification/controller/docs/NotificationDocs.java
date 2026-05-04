package pbl2.sub119.backend.notification.controller.docs;

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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.notification.dto.response.NotificationResponse;
import pbl2.sub119.backend.notification.dto.response.UnreadNotificationCountResponse;

@Tag(name = "Notification API", description = "알림 조회 및 읽음 처리 API")
public interface NotificationDocs {

    @Operation(
            summary = "내 알림 목록 조회",
            description = """
                    로그인한 사용자의 알림 목록을 최신순으로 반환합니다.

                    알림 정책
                    - SMS는 단순 안내용 (읽음 추적 불가)
                    - 실제 읽음/안읽음은 웹 알림 기준으로 관리

                    주요 알림 타입

                    [파티 관련]
                    - PARTY_MATCHED : 파티 매칭 완료 (아직 이용 시작 아님)
                    - HOST_PARTY_MATCHED : 파티장에게 파티원 모집 완료 알림

                    [Provision]
                    - HOST_PROVISION_REQUIRED : 파티장 이용 정보 등록 요청 (24시간 제한)
                    - HOST_PROVISION_REMINDER : 파티장 등록 기한 임박
                    - PROVISION_ACCOUNT_SHARED_REQUIRED : 공유 계정 확인 필요
                    - PROVISION_INVITE_CODE_REQUIRED : 초대 링크 수락 필요
                    - PROVISION_ACCOUNT_SHARED_REMINDER : 계정 확인 리마인드
                    - PROVISION_INVITE_ACCEPT_REQUIRED : 초대 수락 리마인드

                    [정산 / 결제]
                    - PAYMENT_FAILED : 결제 실패
                    - SETTLEMENT_COMPLETED : 정산 완료

                    [파티 해체]
                    - PARTY_TERMINATED : 파티 해체 (자동 재매칭 포함)

                    status 안내
                    - UNREAD : 읽지 않은 알림
                    - READ : 읽은 알림
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = NotificationResponse.class))
                            )
                    )
            }
    )
    @GetMapping
    ResponseEntity<List<NotificationResponse>> getNotifications(
            @Parameter(hidden = true) @Auth Accessor accessor
    );

    @Operation(
            summary = "읽지 않은 알림 수 조회",
            description = "웹 알림 기준 읽지 않은 알림 개수 반환 (SMS 기준 아님)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = UnreadNotificationCountResponse.class))
                    )
            }
    )
    @GetMapping("/unread-count")
    ResponseEntity<UnreadNotificationCountResponse> getUnreadCount(
            @Parameter(hidden = true) @Auth Accessor accessor
    );

    @Operation(
            summary = "알림 단건 읽음 처리",
            description = "사용자가 웹에서 알림 클릭 시 호출",
            responses = {
                    @ApiResponse(responseCode = "200", description = "읽음 처리 성공")
            }
    )
    @PatchMapping("/{notificationId}/read")
    ResponseEntity<Void> markRead(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @PathVariable Long notificationId
    );

    @Operation(
            summary = "전체 알림 읽음 처리",
            description = "웹에서 '전체 읽음' 클릭 시 호출",
            responses = {
                    @ApiResponse(responseCode = "200", description = "전체 읽음 처리 성공")
            }
    )
    @PatchMapping("/read-all")
    ResponseEntity<Void> markAllRead(
            @Parameter(hidden = true) @Auth Accessor accessor
    );
}