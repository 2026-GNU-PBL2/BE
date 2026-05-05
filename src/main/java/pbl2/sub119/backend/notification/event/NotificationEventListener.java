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
import pbl2.sub119.backend.notification.event.event.MemberAutoRematchStartedEvent;
import pbl2.sub119.backend.notification.event.event.HostProvisionReminderEvent;
import pbl2.sub119.backend.notification.event.event.InviteLinkRequiredEvent;
import pbl2.sub119.backend.notification.event.event.MemberProvisionReminderEvent;
import pbl2.sub119.backend.notification.event.event.MemberProvisionTimeoutNoticeEvent;
import pbl2.sub119.backend.notification.event.event.PartyMatchedEvent;
import pbl2.sub119.backend.notification.event.event.PartyTerminatedEvent;
import pbl2.sub119.backend.notification.event.event.PaymentFailedEvent;
import pbl2.sub119.backend.notification.event.event.PaymentSucceededEvent;
import pbl2.sub119.backend.notification.event.event.SettlementCompletedEvent;
import pbl2.sub119.backend.notification.event.event.TestCardPaymentNoticeEvent;
import pbl2.sub119.backend.notification.service.NotificationCommandService;
import pbl2.sub119.backend.notification.service.SmsMessageTemplateService;
import pbl2.sub119.backend.notification.service.WebMessageTemplateService;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.party.provision.entity.PartyProvision;
import pbl2.sub119.backend.party.provision.mapper.PartyProvisionMapper;
import pbl2.sub119.backend.subproduct.enumerated.OperationType;
import pbl2.sub119.backend.subproduct.entity.SubProduct;
import pbl2.sub119.backend.subproduct.mapper.SubProductMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd HH:mm");

    private final NotificationCommandService notificationCommandService;
    private final SmsMessageTemplateService smsTemplate;
    private final WebMessageTemplateService webTemplate;
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

        final String memberTitle = smsTemplate.getTitle(NotificationType.PARTY_MATCHED);
        for (final Long userId : event.memberUserIds()) {
            sendSafely(userId, event.partyId(), NotificationType.PARTY_MATCHED, memberTitle,
                    smsTemplate.memberPartyMatched(productName),
                    webTemplate.memberPartyMatched(productName));
        }

        final String hostTitle = smsTemplate.getTitle(NotificationType.HOST_PARTY_MATCHED);
        sendSafely(party.getHostUserId(), event.partyId(), NotificationType.HOST_PARTY_MATCHED, hostTitle,
                smsTemplate.hostPartyMatched(productName),
                webTemplate.hostPartyMatched(productName));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onHostProvisionReminder(final HostProvisionReminderEvent event) {
        final Party party = partyMapper.findById(event.partyId());
        if (party == null) {
            return;
        }

        final String productName = resolveProductName(party.getProductId());
        final String title = smsTemplate.getTitle(NotificationType.HOST_PROVISION_REMINDER);
        sendSafely(event.hostUserId(), event.partyId(), NotificationType.HOST_PROVISION_REMINDER, title,
                smsTemplate.hostProvisionReminder(productName, event.elapsedHours()),
                webTemplate.hostProvisionReminder(productName, event.elapsedHours()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onHostProvisionDelayedNotice(final HostProvisionDelayedNoticeEvent event) {
        final Party party = partyMapper.findById(event.partyId());
        if (party == null) {
            return;
        }

        final String productName = resolveProductName(party.getProductId());
        final String title = smsTemplate.getTitle(NotificationType.HOST_PROVISION_DELAYED_NOTICE);
        for (final Long userId : event.memberUserIds()) {
            sendSafely(userId, event.partyId(), NotificationType.HOST_PROVISION_DELAYED_NOTICE, title,
                    smsTemplate.hostProvisionDelayedNotice(productName),
                    webTemplate.hostProvisionDelayedNotice(productName));
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onPaymentSucceeded(final PaymentSucceededEvent event) {
        final Party party = partyMapper.findById(event.partyId());
        if (party == null) {
            return;
        }

        final String productName = resolveProductName(party.getProductId());
        final String title = smsTemplate.getTitle(NotificationType.PAYMENT_SUCCEEDED);
        sendSafely(event.payerUserId(), event.partyId(), NotificationType.PAYMENT_SUCCEEDED, title,
                smsTemplate.paymentSucceeded(productName),
                webTemplate.paymentSucceeded(productName));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onTestCardPaymentNotice(final TestCardPaymentNoticeEvent event) {
        final String title = smsTemplate.getTitle(NotificationType.TEST_CARD_PAYMENT_NOTICE);
        sendSafely(event.userId(), null, NotificationType.TEST_CARD_PAYMENT_NOTICE, title,
                smsTemplate.testCardPaymentNotice(),
                webTemplate.testCardPaymentNotice());
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
        final String title = smsTemplate.getTitle(NotificationType.PAYMENT_FAILED);
        sendSafely(event.failedUserId(), event.partyId(), NotificationType.PAYMENT_FAILED, title,
                smsTemplate.paymentFailed(productName),
                webTemplate.paymentFailed(productName));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onSettlementCompleted(final SettlementCompletedEvent event) {
        final Party party = partyMapper.findById(event.partyId());
        if (party == null) {
            return;
        }

        final String productName = resolveProductName(party.getProductId());
        final String title = smsTemplate.getTitle(NotificationType.SETTLEMENT_COMPLETED);
        sendSafely(event.hostUserId(), event.partyId(), NotificationType.SETTLEMENT_COMPLETED, title,
                smsTemplate.settlementCompleted(productName),
                webTemplate.settlementCompleted(productName));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onAccountSharedCredentialRequired(final AccountSharedCredentialRequiredEvent event) {
        final Party party = partyMapper.findById(event.partyId());
        if (party == null) {
            return;
        }

        final String productName = resolveProductName(party.getProductId());
        final String title = smsTemplate.getTitle(NotificationType.PROVISION_ACCOUNT_SHARED_REQUIRED);
        for (final Long userId : event.memberUserIds()) {
            sendSafely(userId, event.partyId(), NotificationType.PROVISION_ACCOUNT_SHARED_REQUIRED, title,
                    smsTemplate.provisionAccountSharedRequired(productName),
                    webTemplate.provisionAccountSharedRequired(productName));
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onInviteLinkRequired(final InviteLinkRequiredEvent event) {
        final Party party = partyMapper.findById(event.partyId());
        if (party == null) {
            return;
        }

        final String productName = resolveProductName(party.getProductId());
        final String title = smsTemplate.getTitle(NotificationType.PROVISION_INVITE_CODE_REQUIRED);
        for (final Long userId : event.memberUserIds()) {
            sendSafely(userId, event.partyId(), NotificationType.PROVISION_INVITE_CODE_REQUIRED, title,
                    smsTemplate.provisionInviteLinkRequired(productName),
                    webTemplate.provisionInviteLinkRequired(productName));
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

        if (provision.getOperationType() == OperationType.ACCOUNT_SHARE) {
            final String title = smsTemplate.getTitle(NotificationType.PROVISION_ACCOUNT_SHARED_REMINDER);
            sendSafely(event.memberUserId(), event.partyId(),
                    NotificationType.PROVISION_ACCOUNT_SHARED_REMINDER, title,
                    smsTemplate.provisionAccountSharedReminder(productName),
                    webTemplate.provisionAccountSharedReminder(productName));
            return;
        }

        final String title = smsTemplate.getTitle(NotificationType.PROVISION_INVITE_ACCEPT_REQUIRED);
        sendSafely(event.memberUserId(), event.partyId(),
                NotificationType.PROVISION_INVITE_ACCEPT_REQUIRED, title,
                smsTemplate.provisionInviteAcceptRequired(productName),
                webTemplate.provisionInviteAcceptRequired(productName));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onMemberProvisionTimeoutNotice(final MemberProvisionTimeoutNoticeEvent event) {
        final Party party = partyMapper.findById(event.partyId());
        if (party == null) {
            return;
        }

        final String productName = resolveProductName(party.getProductId());
        final String title = smsTemplate.getTitle(NotificationType.MEMBER_PROVISION_TIMEOUT_NOTICE);
        sendSafely(event.memberUserId(), event.partyId(), NotificationType.MEMBER_PROVISION_TIMEOUT_NOTICE, title,
                smsTemplate.memberProvisionTimeoutNotice(productName),
                webTemplate.memberProvisionTimeoutNotice(productName));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onPartyTerminated(final PartyTerminatedEvent event) {
        final Party party = partyMapper.findById(event.partyId());
        if (party == null) {
            return;
        }

        final String productName = resolveProductName(party.getProductId());

        final String hostTitle = smsTemplate.getTitle(NotificationType.HOST_PROVISION_TIMEOUT_TERMINATED);
        sendSafely(party.getHostUserId(), event.partyId(),
                NotificationType.HOST_PROVISION_TIMEOUT_TERMINATED, hostTitle,
                smsTemplate.hostProvisionTimeoutTerminatedForHost(productName),
                webTemplate.hostProvisionTimeoutTerminatedForHost(productName));

        final String memberTitle = smsTemplate.getTitle(NotificationType.PARTY_TERMINATED);
        for (final Long userId : event.memberUserIds()) {
            if (!userId.equals(party.getHostUserId())) {
                sendSafely(userId, event.partyId(), NotificationType.PARTY_TERMINATED, memberTitle,
                        smsTemplate.hostProvisionTimeoutTerminatedForMember(productName),
                        webTemplate.hostProvisionTimeoutTerminatedForMember(productName));
            }
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onMemberAutoRematchStarted(final MemberAutoRematchStartedEvent event) {
        final Party party = partyMapper.findById(event.partyId());
        if (party == null) {
            return;
        }

        final String productName = resolveProductName(party.getProductId());
        final String title = smsTemplate.getTitle(NotificationType.MEMBER_AUTO_REMATCH_STARTED);
        for (final Long userId : event.requeuedUserIds()) {
            sendSafely(userId, event.partyId(), NotificationType.MEMBER_AUTO_REMATCH_STARTED, title,
                    smsTemplate.memberAutoRematchStarted(productName),
                    webTemplate.memberAutoRematchStarted(productName));
        }
    }

    private void sendSafely(
            final Long userId,
            final Long partyId,
            final NotificationType type,
            final String title,
            final String smsContent,
            final String webContent
    ) {
        try {
            notificationCommandService.notifyWithWebContent(userId, partyId, type, title, smsContent, webContent);
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
