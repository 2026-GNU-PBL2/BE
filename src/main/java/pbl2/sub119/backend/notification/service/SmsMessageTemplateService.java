package pbl2.sub119.backend.notification.service;

import org.springframework.stereotype.Service;
import pbl2.sub119.backend.notification.enumerated.NotificationType;

@Service
public class SmsMessageTemplateService {

    public String getTitle(final NotificationType type) {
        return switch (type) {
            case PARTY_MATCHED -> "파티 매칭 완료";
            case HOST_PARTY_MATCHED -> "파티원 모집 완료";
            case PAYMENT_SUCCEEDED -> "결제 완료";
            case PAYMENT_FAILED -> "결제 실패";
            case SETTLEMENT_COMPLETED -> "정산 완료";
            case TEST_CARD_PAYMENT_NOTICE -> "카드 확인 결제 안내";

            case HOST_PROVISION_REQUIRED -> "이용 정보 등록 필요";
            case HOST_PROVISION_REMINDER -> "이용 정보 등록 기한 안내";
            case HOST_PROVISION_DELAYED_NOTICE -> "파티장 이용 정보 등록 지연";
            case HOST_PROVISION_TIMEOUT_TERMINATED -> "파티 해체 안내";

            case PROVISION_ACCOUNT_SHARED_REQUIRED -> "공유 계정 확인 필요";
            case PROVISION_INVITE_CODE_REQUIRED -> "초대 링크 수락 필요";
            case PROVISION_ACCOUNT_SHARED_REMINDER -> "공유 계정 확인 리마인드";
            case PROVISION_INVITE_ACCEPT_REQUIRED -> "초대 수락 리마인드";
            case MEMBER_PROVISION_TIMEOUT_NOTICE -> "이용 확인 지연 안내";

            case PARTY_TERMINATED -> "파티 해체 안내";
            case MEMBER_AUTO_REMATCH_STARTED -> "자동 재매칭 시작";
        };
    }

    // 파티원 매칭 완료 안내
    public String memberPartyMatched(final String productName) {
        return String.format(
                "[Submate] %s 파티 매칭이 완료됐어요.\n파티장이 이용 정보를 등록하면 결제가 진행되고 이용이 시작됩니다.\n진행 상태는 앱에서 확인해주세요.",
                productName
        );
    }

    // 파티장 파티원 모집 완료 안내
    public String hostPartyMatched(final String productName) {
        return String.format(
                "[Submate] %s 파티원 모집이 완료됐어요.\n24시간 안에 구독 후 이용 정보를 등록해주세요.\n기한 내 미등록 시 파티가 자동 해체됩니다.",
                productName
        );
    }

    // 파티장 provision 등록 요청
    public String hostProvisionRequired(final String productName) {
        return String.format(
                "[Submate] %s 이용 정보 등록이 필요해요.\n24시간 안에 공유 계정 또는 초대 링크를 등록해주세요.\n미등록 시 파티가 자동 해체됩니다.",
                productName
        );
    }

    // 파티장 provision 리마인드
    public String hostProvisionReminder(final String productName, final int elapsedHours) {
        final int remainingHours = 48 - elapsedHours;
        return String.format(
                "[Submate] %s 이용 정보가 아직 등록되지 않았어요.\n등록 기한까지 %d시간 남았습니다.\n기한 내 미등록 시 파티가 자동 해체됩니다.",
                productName,
                remainingHours
        );
    }

    // 파티원에게 파티장 지연 안내
    public String hostProvisionDelayedNotice(final String productName) {
        return String.format(
                "[Submate] %s 파티장이 아직 이용 정보를 등록하지 않았어요.\n결제는 아직 진행되지 않았습니다.\n등록이 완료되면 다시 알려드릴게요.",
                productName
        );
    }

    // 파티장 미등록으로 해체
    public String hostProvisionTimeoutTerminatedForHost(final String productName) {
        return String.format(
                "[Submate] %s 이용 정보를 기한 내 등록하지 않아 파티가 해체됐어요.\n파티원은 자동으로 새 파티 매칭이 시작됩니다.",
                productName
        );
    }

    // 파티원 입장 파티 해체
    public String hostProvisionTimeoutTerminatedForMember(final String productName) {
        return String.format(
                "[Submate] 파티장이 기한 내 %s 이용 정보를 등록하지 않아 파티가 해체됐어요.\n결제는 진행되지 않았고, 자동으로 새 파티 매칭을 시작합니다.",
                productName
        );
    }

    // 공유 계정 확인 요청
    public String provisionAccountSharedRequired(final String productName) {
        return String.format(
                "[Submate] %s 공유 계정 정보가 등록됐어요.\n앱에서 계정 정보를 확인하고 이용 확인을 완료해주세요.\n미확인 기간은 환불 대상이 아닙니다.",
                productName
        );
    }

    // 초대 링크 수락 요청
    public String provisionInviteLinkRequired(final String productName) {
        return String.format(
                "[Submate] %s 파티 초대 링크가 도착했어요.\n초대 수락이 늦어지면 이용 가능 기간이 줄어들 수 있어요.\n앱에서 수락해주세요.",
                productName
        );
    }

    // 공유 계정 리마인드
    public String provisionAccountSharedReminder(final String productName) {
        return String.format(
                "[Submate] %s 이용 확인 기한이 지났어요.\n지금 바로 앱에서 계정 정보를 확인해주세요.\n미확인 기간은 환불 대상이 아닙니다.",
                productName
        );
    }

    // 초대 링크 리마인드
    public String provisionInviteAcceptRequired(final String productName) {
        return String.format(
                "[Submate] %s 초대 수락 기한이 지났어요.\n지금 바로 앱에서 초대를 수락해주세요.\n늦게 수락하면 이용 기간이 줄어들 수 있어요.",
                productName
        );
    }

    // 파티원 24시간 초과 안내
    public String memberProvisionTimeoutNotice(final String productName) {
        return String.format(
                "[Submate] %s 이용 확인 기한이 지났어요.\n파티는 유지되며, 미확인 기간은 환불 대상이 아닙니다.\n앱에서 언제든 확인할 수 있어요.",
                productName
        );
    }

    // 자동 재매칭 시작
    public String memberAutoRematchStarted(final String productName) {
        return String.format(
                "[Submate] %s 파티가 해체되어 자동 재매칭이 시작됐어요.\n새 파티 매칭이 완료되면 다시 알려드릴게요.",
                productName
        );
    }

    // 정산 완료
    public String settlementCompleted(final String productName) {
        return String.format(
                "[Submate] %s 파티 정산이 완료됐어요.\n정산금, 정산일, 입금 계좌는 앱에서 확인해주세요.",
                productName
        );
    }

    // 결제 완료
    public String paymentSucceeded(final String productName) {
        return String.format(
                "[Submate] %s 파티 결제가 완료됐어요.\n이용 내역은 앱에서 확인해주세요.",
                productName
        );
    }

    // 결제 실패
    public String paymentFailed(final String productName) {
        return String.format(
                "[Submate] %s 자동 결제에 실패했어요.\n결제 정보를 확인하지 않으면 이용이 제한될 수 있어요.\n앱에서 확인해주세요.",
                productName
        );
    }

    // 테스트 카드 결제
    public String testCardPaymentNotice() {
        return "[Submate] 결제카드 확인을 위해 100원이 결제됐어요.\n정상 카드 확인용 테스트 결제이며 곧바로 환불됩니다.";
    }
}