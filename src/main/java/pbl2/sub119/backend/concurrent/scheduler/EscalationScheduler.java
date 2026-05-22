package pbl2.sub119.backend.concurrent.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pbl2.sub119.backend.concurrent.entity.ConcurrentIncident;
import pbl2.sub119.backend.concurrent.entity.DeviceDetectionEvent;
import pbl2.sub119.backend.concurrent.enumerated.DeviceDetectionStatus;
import pbl2.sub119.backend.concurrent.mapper.ConcurrentIncidentMapper;
import pbl2.sub119.backend.concurrent.mapper.DeviceDetectionMapper;
import pbl2.sub119.backend.concurrent.service.EscalationService;
import pbl2.sub119.backend.notification.enumerated.NotificationType;
import pbl2.sub119.backend.notification.service.NotificationCommandService;
import pbl2.sub119.backend.notification.service.SmsMessageTemplateService;
import pbl2.sub119.backend.notification.service.WebMessageTemplateService;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.subproduct.mapper.SubProductMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class EscalationScheduler {

    private final ConcurrentIncidentMapper incidentMapper;
    private final DeviceDetectionMapper deviceDetectionMapper;
    private final PartyMapper partyMapper;
    private final SubProductMapper subProductMapper;
    private final EscalationService escalationService;
    private final NotificationCommandService notificationCommandService;
    private final SmsMessageTemplateService smsTemplate;
    private final WebMessageTemplateService webTemplate;

    // 4시간 경과 → 웹+SMS 재알림
    @Scheduled(fixedDelay = 900_000)
    public void sendRenotification() {
        final LocalDateTime threshold = LocalDateTime.now().minusHours(4);
        final List<ConcurrentIncident> targets = incidentMapper.findForWebRenotification(threshold);

        for (final ConcurrentIncident incident : targets) {
            try {
                final Party party = partyMapper.findById(incident.getPartyId());
                if (party == null) continue;

                final String productName = subProductMapper.findById(party.getProductId())
                        .map(p -> p.getServiceName()).orElse("서비스");
                final String deadline = incident.getHostDeadline() != null
                        ? incident.getHostDeadline().toString() : "";

                notificationCommandService.notifyWithWebContent(
                        party.getHostUserId(), party.getId(),
                        NotificationType.HOST_RENOTIFY,
                        smsTemplate.getTitle(NotificationType.HOST_RENOTIFY),
                        smsTemplate.hostRenotify(productName, deadline),
                        webTemplate.hostRenotify(productName, deadline)
                );

                incidentMapper.updateWebNotified(incident.getId());
                log.info("4h 재알림 발송. incidentId={}, partyId={}", incident.getId(), incident.getPartyId());
            } catch (Exception e) {
                log.error("4h 재알림 실패. incidentId={}", incident.getId(), e);
            }
        }
    }

    // 8시간 경과 → SMS 재알림
    @Scheduled(fixedDelay = 900_000)
    public void sendSmsRenotification() {
        final LocalDateTime threshold = LocalDateTime.now().minusHours(8);
        final List<ConcurrentIncident> targets = incidentMapper.findForSmsRenotification(threshold);

        for (final ConcurrentIncident incident : targets) {
            try {
                final Party party = partyMapper.findById(incident.getPartyId());
                if (party == null) continue;

                final String productName = subProductMapper.findById(party.getProductId())
                        .map(p -> p.getServiceName()).orElse("서비스");
                final String deadline = incident.getHostDeadline() != null
                        ? incident.getHostDeadline().toString() : "";

                notificationCommandService.notifyWithWebContent(
                        party.getHostUserId(), party.getId(),
                        NotificationType.HOST_RENOTIFY,
                        smsTemplate.getTitle(NotificationType.HOST_RENOTIFY),
                        smsTemplate.hostRenotify(productName, deadline),
                        webTemplate.hostRenotify(productName, deadline)
                );

                incidentMapper.updateSmsNotified(incident.getId());
                log.info("8h 재알림 발송. incidentId={}, partyId={}", incident.getId(), incident.getPartyId());
            } catch (Exception e) {
                log.error("8h 재알림 실패. incidentId={}", incident.getId(), e);
            }
        }
    }

    // host_deadline 초과 → 파티장 에스컬레이션 플래그
    @Scheduled(fixedDelay = 900_000)
    public void escalateToAdmin() {
        final LocalDateTime now = LocalDateTime.now();
        final List<ConcurrentIncident> targets = incidentMapper.findForAdminEscalation(now);

        for (final ConcurrentIncident incident : targets) {
            try {
                incidentMapper.updateAdminEscalated(incident.getId());
                log.info("관리자 에스컬레이션. incidentId={}, partyId={}", incident.getId(), incident.getPartyId());
            } catch (Exception e) {
                log.error("관리자 에스컬레이션 실패. incidentId={}", incident.getId(), e);
            }
        }
    }

    // 24시간 경과 PENDING 기기 감지 이벤트 → EXPIRED 처리
    @Scheduled(fixedDelay = 900_000)
    public void expireDeviceAlerts() {
        final List<DeviceDetectionEvent> targets = deviceDetectionMapper.findExpiredPending(LocalDateTime.now());

        for (final DeviceDetectionEvent event : targets) {
            try {
                deviceDetectionMapper.updateStatus(event.getId(), DeviceDetectionStatus.EXPIRED);
                log.info("기기 감지 이벤트 만료 처리. alertId={}, partyId={}", event.getId(), event.getPartyId());
            } catch (Exception e) {
                log.error("기기 감지 이벤트 만료 처리 실패. alertId={}", event.getId(), e);
            }
        }
    }

    // 자정 해체 실행
    @Scheduled(cron = "0 0 0 * * *")
    public void processDissolutions() {
        final List<Party> targets = partyMapper.findByDissolutionDate(LocalDate.now());
        log.info("자정 해체 대상 파티 수: {}", targets.size());

        for (final Party party : targets) {
            try {
                escalationService.dissolveParty(party);
            } catch (Exception e) {
                log.error("파티 해체 실패. partyId={}", party.getId(), e);
            }
        }
    }
}
