package pbl2.sub119.backend.notification.event;

import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pbl2.sub119.backend.notification.enumerated.NotificationType;
import pbl2.sub119.backend.notification.event.event.AccountSharedCredentialRequiredEvent;
import pbl2.sub119.backend.notification.event.event.HostProvisionDelayedNoticeEvent;
import pbl2.sub119.backend.notification.event.event.HostProvisionReminderEvent;
import pbl2.sub119.backend.notification.event.event.InviteLinkRequiredEvent;
import pbl2.sub119.backend.notification.event.event.MemberProvisionReminderEvent;
import pbl2.sub119.backend.notification.event.event.MemberProvisionTimeoutNoticeEvent;
import pbl2.sub119.backend.notification.event.event.PartyMatchedEvent;
import pbl2.sub119.backend.notification.event.event.PartyTerminatedEvent;
import pbl2.sub119.backend.notification.event.event.PaymentFailedEvent;
import pbl2.sub119.backend.notification.event.event.SettlementCompletedEvent;
import pbl2.sub119.backend.notification.service.NotificationCommandService;
import pbl2.sub119.backend.notification.service.SmsMessageTemplateService;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.party.provision.entity.PartyProvision;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionType;
import pbl2.sub119.backend.party.provision.mapper.PartyProvisionMapper;
import pbl2.sub119.backend.subproduct.entity.SubProduct;
import pbl2.sub119.backend.subproduct.mapper.SubProductMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd HH:mm");

    private final NotificationCommandService notificationCommandService;
    private final SmsMessageTemplateService template;
    private final PartyMapper partyMapper;
    private final SubProductMapper subProductMapper;
    private final PartyProvisionMapper partyProvisionMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onPartyMatched(final PartyMatchedEvent event) {
        final Party party = partyMapper.findById(event.partyId());
        if (party == null) {
            return;
        }

        final String productName = resolveProductName(party.getProductId());

        // 파티원에게는 아직 이용 시작이 아니라 "매칭 완료"로 안내
        final String memberTitle = template.getTitle(NotificationType.PARTY_MATCHED);
        final String memberContent = template.memberPartyMatched(productName);

        for (final Long userId : event.memberUserIds()) {
            sendSafely(userId, event.partyId(), NotificationType.PARTY_MATCHED, memberTitle, memberContent);
        }

        // 파티장에게는 이용 정보 등록 요청 안내
        final String hostTitle = template.getTitle(NotificationType.HOST_PARTY_MATCHED);
        final String hostContent = template.hostPartyMatched(productName);

        sendSafely(party.getHostUserId(), event.partyId(), NotificationType.HOST_PARTY_MATCHED, hostTitle, hostContent);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onHostProvisionReminder(final HostProvisionReminderEvent event) {
        final Party party = partyMapper.findById(event.partyId());
        if (party == null) {
            return;
        }

        final String productName = resolveProductName(party.getProductId());
        final String title = template.getTitle(NotificationType.HOST_PROVISION_REMINDER);
        final String content = template.hostProvisionReminder(productName, event.elapsedHours());

        sendSafely(event.hostUserId(), event.partyId(), NotificationType.HOST_PROVISION_REMINDER, title, content);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onHostProvisionDelayedNotice(final HostProvisionDelayedNoticeEvent event) {
        final Party party = partyMapper.findById(event.partyId());
        if (party == null) {
            return;
        }

        final String productName = resolveProductName(party.getProductId());
        final String title = template.getTitle(NotificationType.HOST_PROVISION_DELAYED_NOTICE);
        final String content = template.hostProvisionDelayedNotice(productName);

        for (final Long userId : event.memberUserIds()) {
            sendSafely(userId, event.partyId(), NotificationType.HOST_PROVISION_DELAYED_NOTICE, title, content);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onPaymentFailed(final PaymentFailedEvent event) {
        if (event.failedUserId() == null || event.failedUserId() == 0L) {
            return;
        }

        final Party party = partyMapper.findById(event.partyId());
        if (party == null) {
            return;
        }

        final String productName = resolveProductName(party.getProductId());
        final String title = template.getTitle(NotificationType.PAYMENT_FAILED);
        final String content = template.paymentFailed(productName);

        sendSafely(event.failedUserId(), event.partyId(), NotificationType.PAYMENT_FAILED, title, content);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onSettlementCompleted(final SettlementCompletedEvent event) {
        final Party party = partyMapper.findById(event.partyId());
        if (party == null) {
            return;
        }

        final String productName = resolveProductName(party.getProductId());
        final String title = template.getTitle(NotificationType.SETTLEMENT_COMPLETED);
        final String content = template.settlementCompleted(productName);

        sendSafely(event.hostUserId(), event.partyId(), NotificationType.SETTLEMENT_COMPLETED, title, content);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onAccountSharedCredentialRequired(final AccountSharedCredentialRequiredEvent event) {
        final Party party = partyMapper.findById(event.partyId());
        if (party == null) {
            return;
        }

        final String productName = resolveProductName(party.getProductId());
        final String title = template.getTitle(NotificationType.PROVISION_ACCOUNT_SHARED_REQUIRED);
        final String content = template.provisionAccountSharedRequired(productName);

        for (final Long userId : event.memberUserIds()) {
            sendSafely(userId, event.partyId(), NotificationType.PROVISION_ACCOUNT_SHARED_REQUIRED, title, content);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onInviteLinkRequired(final InviteLinkRequiredEvent event) {
        final Party party = partyMapper.findById(event.partyId());
        if (party == null) {
            return;
        }

        final String productName = resolveProductName(party.getProductId());
        final String title = template.getTitle(NotificationType.PROVISION_INVITE_LINK_REQUIRED);
        final String content = template.provisionInviteLinkRequired(productName);

        for (final Long userId : event.memberUserIds()) {
            sendSafely(userId, event.partyId(), NotificationType.PROVISION_INVITE_LINK_REQUIRED, title, content);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onMemberProvisionReminder(final MemberProvisionReminderEvent event) {
        final Party party = partyMapper.findById(event.partyId());
        if (party == null) {
            return;
        }

        final PartyProvision provision = partyProvisionMapper.findByPartyId(event.partyId());
        if (provision == null) {
            return;
        }

        final String productName = resolveProductName(party.getProductId());

        // 공유계정형/초대링크형에 따라 문구와 타입 분기
        if (provision.getOperationType() == ProvisionType.ACCOUNT_SHARED) {
            final String title = template.getTitle(NotificationType.PROVISION_ACCOUNT_SHARED_REMINDER);
            final String content = template.provisionAccountSharedReminder(productName, event.elapsedHours());

            sendSafely(event.memberUserId(), event.partyId(),
                    NotificationType.PROVISION_ACCOUNT_SHARED_REMINDER, title, content);
            return;
        }

        final String title = template.getTitle(NotificationType.PROVISION_INVITE_ACCEPT_REQUIRED);
        final String content = template.provisionInviteAcceptRequired(productName, event.elapsedHours());

        sendSafely(event.memberUserId(), event.partyId(),
                NotificationType.PROVISION_INVITE_ACCEPT_REQUIRED, title, content);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onMemberProvisionTimeoutNotice(final MemberProvisionTimeoutNoticeEvent event) {
        final Party party = partyMapper.findById(event.partyId());
        if (party == null) {
            return;
        }

        final String productName = resolveProductName(party.getProductId());
        final String title = template.getTitle(NotificationType.MEMBER_PROVISION_TIMEOUT_NOTICE);
        final String content = template.memberProvisionTimeoutNotice(productName);

        sendSafely(event.memberUserId(), event.partyId(), NotificationType.MEMBER_PROVISION_TIMEOUT_NOTICE, title, content);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onPartyTerminated(final PartyTerminatedEvent event) {
        final Party party = partyMapper.findById(event.partyId());
        if (party == null) {
            return;
        }

        final String productName = resolveProductName(party.getProductId());

        // 파티장에게 해체 사유 안내
        final String hostTitle = template.getTitle(NotificationType.HOST_PROVISION_TIMEOUT_TERMINATED);
        final String hostContent = template.hostProvisionTimeoutTerminatedForHost(productName);

        sendSafely(party.getHostUserId(), event.partyId(),
                NotificationType.HOST_PROVISION_TIMEOUT_TERMINATED, hostTitle, hostContent);

        // 파티원에게 자동 재매칭 안내
        final String memberTitle = template.getTitle(NotificationType.PARTY_TERMINATED);
        final String memberContent = template.hostProvisionTimeoutTerminatedForMember(productName);

        for (final Long userId : event.memberUserIds()) {
            if (!userId.equals(party.getHostUserId())) {
                sendSafely(userId, event.partyId(), NotificationType.PARTY_TERMINATED, memberTitle, memberContent);
            }
        }
    }

    private void sendSafely(
            final Long userId,
            final Long partyId,
            final NotificationType type,
            final String title,
            final String content
    ) {
        try {
            notificationCommandService.notify(userId, partyId, type, title, content);
        } catch (Exception e) {
            log.error("알림 발송 실패. userId={}, partyId={}, type={}", userId, partyId, type, e);
        }
    }

    private String resolveProductName(final String productId) {
        return subProductMapper.findById(productId)
                .map(SubProduct::getServiceName)
                .orElse(productId);
    }
}