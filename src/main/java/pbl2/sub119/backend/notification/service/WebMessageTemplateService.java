package pbl2.sub119.backend.notification.service;

import org.springframework.stereotype.Service;

@Service
public class WebMessageTemplateService {

    public String memberPartyMatched(final String productName) {
        return String.format("%s 파티 매칭이 완료됐어요.", productName);
    }

    public String hostPartyMatched(final String productName) {
        return String.format("%s 파티원 모집이 완료됐어요. 24시간 내 이용 정보를 등록해주세요.", productName);
    }

    public String hostProvisionRequired(final String productName) {
        return String.format("%s 이용 정보 등록이 필요해요.", productName);
    }

    public String hostProvisionReminder(final String productName, final int elapsedHours) {
        final int remaining = 48 - elapsedHours;
        return String.format("%s 이용 정보 등록 기한까지 %d시간 남았어요.", productName, remaining);
    }

    public String hostProvisionDelayedNotice(final String productName) {
        return String.format("%s 파티장이 아직 이용 정보를 등록하지 않았어요.", productName);
    }

    public String hostProvisionTimeoutTerminatedForHost(final String productName) {
        return String.format("%s 이용 정보 미등록으로 파티가 해체됐어요.", productName);
    }

    public String hostProvisionTimeoutTerminatedForMember(final String productName) {
        return String.format("%s 파티가 해체됐어요. 자동으로 새 파티 매칭을 시작해요.", productName);
    }

    public String provisionAccountSharedRequired(final String productName) {
        return String.format("%s 공유 계정 정보를 확인하고 이용 확인을 완료해주세요.", productName);
    }

    public String provisionInviteLinkRequired(final String productName) {
        return String.format("%s 파티 초대 링크가 도착했어요. 초대를 수락해주세요.", productName);
    }

    public String provisionAccountSharedReminder(final String productName) {
        return String.format("%s 공유 계정 확인이 아직 완료되지 않았어요.", productName);
    }

    public String provisionInviteAcceptRequired(final String productName) {
        return String.format("%s 초대 수락이 아직 완료되지 않았어요.", productName);
    }

    public String memberProvisionTimeoutNotice(final String productName) {
        return String.format("%s 이용 확인 기한이 지났어요. 미확인 기간은 환불 대상에서 제외돼요.", productName);
    }

    public String memberAutoRematchStarted(final String productName) {
        return String.format("%s 파티 해체 후 자동 재매칭이 시작됐어요.", productName);
    }

    public String settlementCompleted(final String productName) {
        return String.format("%s 파티 정산이 완료됐어요.", productName);
    }

    public String paymentSucceeded(final String productName) {
        return String.format("%s 파티 결제가 완료됐어요.", productName);
    }

    public String paymentFailed(final String productName) {
        return String.format("%s 자동 결제에 실패했어요. 결제 정보를 확인해주세요.", productName);
    }

    public String testCardPaymentNotice() {
        return "카드 확인을 위해 100원이 결제됐어요. 곧바로 환불됩니다.";
    }
}
