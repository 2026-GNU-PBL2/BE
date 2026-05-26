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
            case SETTLEMENT_SKIPPED_PAYMENT_FAILED -> "정산 미진행 안내";
            case TEST_CARD_PAYMENT_NOTICE -> "카드 확인 결제 안내";

            case HOST_PROVISION_REQUIRED -> "이용 정보 등록 필요";
            case HOST_PROVISION_REMINDER -> "이용 정보 등록 기한 안내";
            case HOST_PROVISION_TIMEOUT_TERMINATED -> "파티 해체 안내";
            case HOST_PROVISION_DELAYED_NOTICE -> "이용 정보 등록 지연 안내";

            case PROVISION_ACCOUNT_SHARED_REQUIRED -> "공유 계정 확인 필요";
            case PROVISION_INVITE_CODE_REQUIRED -> "초대 링크 수락 필요";
            case PROVISION_ACCOUNT_SHARED_REMINDER -> "공유 계정 확인 리마인드";
            case PROVISION_INVITE_ACCEPT_REQUIRED -> "초대 수락 리마인드";
            case MEMBER_PROVISION_TIMEOUT_NOTICE -> "이용 확인 지연 안내";

            case PARTY_TERMINATED -> "파티 해체 안내";
            case MEMBER_AUTO_REMATCH_STARTED -> "자동 재매칭 시작";

            case CONCURRENT_WARNING_1 -> "동시접속 위반 경고";
            case HOST_ACTION_REQUIRED_24H -> "파티장 조치 요청";
            case HOST_URGENT_PASSWORD_CHANGE -> "비밀번호 변경 긴급 안내";
            case HOST_RENOTIFY -> "파티 해체 예정 재안내";
            case PARTY_DISSOLVING -> "파티 해체 예정 안내";
            case CREDENTIALS_UPDATED -> "이용 정보 재등록 안내";
            case DEVICE_ALERT -> "기기 감지 확인 요청";
            case DEVICE_CHECK_REQUEST -> "기기 확인 요청";
            case DEVICE_CONFIRMED_MINE -> "기기 확인 완료";
            case PARTY_DISSOLVED_FINAL -> "파티 해체 완료 안내";
        };
    }

    // 파티원 매칭 완료 안내
    public String memberPartyMatched(final String productName) {
        return String.format(
                "[Submate] %s 파티 매칭이 완료됐어요.\n파티장이 이용 정보를 등록하면 결제가 진행되고 이용이 시작됩니다.\n진행 상태는 웹에서 확인해주세요.",
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
        final int remainingHours = 24 - elapsedHours;
        return String.format(
                "[Submate] %s 이용 정보가 아직 등록되지 않았어요.\n등록 기한까지 %d시간 남았습니다.\n기한 내 미등록 시 파티가 자동 해체됩니다.",
                productName,
                remainingHours
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
                "[Submate] %s 공유 계정 정보가 등록됐어요.\n웹에서 계정 정보를 확인하고 이용 확인을 완료해주세요.\n미확인 기간은 환불 대상이 아닙니다.",
                productName
        );
    }

    // 초대 링크 수락 요청
    public String provisionInviteLinkRequired(final String productName) {
        return String.format(
                "[Submate] %s 파티 초대 링크가 도착했어요.\n초대 수락이 늦어지면 이용 가능 기간이 줄어들 수 있어요.\n웹에서 수락해주세요.",
                productName
        );
    }

    // 공유 계정 리마인드
    public String provisionAccountSharedReminder(final String productName) {
        return String.format(
                "[Submate] %s 이용 확인 기한이 지났어요.\n지금 바로 웹에서 계정 정보를 확인해주세요.\n미확인 기간은 환불 대상이 아닙니다.",
                productName
        );
    }

    // 초대 링크 리마인드
    public String provisionInviteAcceptRequired(final String productName) {
        return String.format(
                "[Submate] %s 초대 수락 기한이 지났어요.\n지금 바로 웹에서 초대를 수락해주세요.\n늦게 수락하면 이용 기간이 줄어들 수 있어요.",
                productName
        );
    }

    // 파티원 24시간 초과 안내
    public String memberProvisionTimeoutNotice(final String productName) {
        return String.format(
                "[Submate] %s 이용 확인 기한이 지났어요.\n파티는 유지되며, 미확인 기간은 환불 대상이 아닙니다.\n웹에서 언제든 확인할 수 있어요.",
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
                "[Submate] %s 파티 정산이 완료됐어요.\n정산금, 정산일, 입금 계좌는 웹에서 확인해주세요.",
                productName
        );
    }

    // 결제 실패로 인한 정산 미진행
    public String settlementSkippedPaymentFailed(final String productName) {
        return String.format(
                "[Submate] %s 이번 회차는 일부 결제 실패로 정산이 진행되지 않았습니다.\n자세한 내용은 웹에서 확인해주세요.",
                productName
        );
    }

    // 결제 완료
    public String paymentSucceeded(final String productName) {
        return String.format(
                "[Submate] %s 파티 결제가 완료됐어요.\n이용 내역은 웹에서 확인해주세요.",
                productName
        );
    }

    // 결제 실패
    public String paymentFailed(final String productName) {
        return String.format(
                "[Submate] %s 자동 결제에 실패했어요.\n결제 정보를 확인하지 않으면 이용이 제한될 수 있어요.\n웹에서 확인해주세요.",
                productName
        );
    }

    // 동시접속 1차 경고 — 파티원 전체
    public String concurrentWarning1(final String productName) {
        return String.format(
                "[Submate] %s 파티에서 동시접속 위반이 감지됐어요.\n계정 공유 규칙을 준수해주세요.\n위반 반복 시 파티가 해체될 수 있습니다.",
                productName
        );
    }

    // 파티장 24시간 조치 요청
    public String hostActionRequired24h(final String productName, final String deadline) {
        return String.format(
                "[Submate] %s 파티에서 동시접속 위반이 신고됐어요.\n%s 까지 비밀번호를 변경하고 파티원에게 이용 정보를 재공유해주세요.\n미조치 시 파티 해체 절차가 진행됩니다.",
                productName, deadline
        );
    }

    // 파티장 긴급 안내 — 2차, 12시간
    public String hostUrgentPasswordChange(final String productName, final String deadline, final String dissolutionDate) {
        return String.format(
                "[Submate] %s 파티 동시접속 위반 재신고가 접수됐어요.\n%s 까지 조치하지 않으면 %s 파티가 해체됩니다.\n지금 바로 비밀번호를 변경해주세요.",
                productName, deadline, dissolutionDate
        );
    }

    // 재알림 (4h/8h 경과)
    public String hostRenotify(final String productName, final String deadline) {
        return String.format(
                "[Submate] %s 파티 동시접속 위반 조치가 아직 완료되지 않았어요.\n%s 까지 비밀번호를 변경하지 않으면 파티가 해체됩니다.",
                productName, deadline
        );
    }

    // 파티원 해체 예정 안내
    public String partyDissolvingMember(final String productName, final String dissolutionDate) {
        return String.format(
                "[Submate] %s 파티가 동시접속 위반으로 %s 해체될 예정이에요.\n새 파티 매칭은 해체 후 자동으로 시작됩니다.",
                productName, dissolutionDate
        );
    }

    // 파티원 이용 정보 재등록 안내
    public String credentialsUpdated(final String productName) {
        return String.format(
                "[Submate] %s 파티 이용 정보가 업데이트됐어요.\n웹에서 새 계정 정보를 확인해주세요.",
                productName
        );
    }

    // 파티원에게 기기 확인 요청 (신고 발생 시)
    public String deviceCheckRequest(final String device, final String location) {
        return String.format(
                "[Submate] 낯선 기기가 감지됐어요.\n기기: %s / 위치: %s\n본인 기기인지 앱에서 확인해주세요.",
                device, location
        );
    }

    // 기기 파티원 본인 기기 확인 완료
    public String deviceConfirmedMine(final String device, final String location) {
        return String.format(
                "[Submate] 감지된 기기가 파티원 기기로 확인됐어요.\n기기: %s / 위치: %s\n별도 조치는 필요하지 않아요.",
                device, location
        );
    }

    // 기기 감지 확인 요청
    public String deviceAlert(final String device, final String location) {
        return String.format(
                "[Submate] 새로운 기기가 감지됐어요.\n기기: %s / 위치: %s\n본인 기기가 맞는지 웹에서 확인해주세요.",
                device, location
        );
    }

    // 해체 완료 알림
    public String partyDissolvedFinal(final String productName) {
        return String.format(
                "[Submate] %s 파티가 동시접속 위반으로 해체됐어요.\n자동으로 새 파티 매칭이 시작됩니다.",
                productName
        );
    }
}