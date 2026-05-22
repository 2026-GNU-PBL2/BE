package pbl2.sub119.backend.concurrent.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.concurrent.entity.UserViolationRecord;
import pbl2.sub119.backend.concurrent.enumerated.IncidentStatus;
import pbl2.sub119.backend.concurrent.enumerated.ViolationType;
import pbl2.sub119.backend.concurrent.mapper.ConcurrentIncidentMapper;
import pbl2.sub119.backend.concurrent.mapper.UserViolationRecordMapper;
import pbl2.sub119.backend.notification.enumerated.NotificationType;
import pbl2.sub119.backend.notification.service.NotificationCommandService;
import pbl2.sub119.backend.notification.service.SmsMessageTemplateService;
import pbl2.sub119.backend.notification.service.WebMessageTemplateService;
import pbl2.sub119.backend.party.common.entity.MatchWaitingQueue;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.entity.PartyMember;
import pbl2.sub119.backend.party.common.enumerated.MatchWaitingStatus;
import pbl2.sub119.backend.party.common.enumerated.OperationStatus;
import pbl2.sub119.backend.party.common.mapper.MatchWaitingQueueMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMemberMapper;
import pbl2.sub119.backend.subproduct.mapper.SubProductMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class EscalationService {

    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final SubProductMapper subProductMapper;
    private final MatchWaitingQueueMapper matchWaitingQueueMapper;
    private final ConcurrentIncidentMapper incidentMapper;
    private final UserViolationRecordMapper violationRecordMapper;
    private final NotificationCommandService notificationCommandService;
    private final SmsMessageTemplateService smsTemplate;
    private final WebMessageTemplateService webTemplate;

    @Transactional
    public void dissolveParty(final Party party) {
        if (party.getOperationStatus() == OperationStatus.TERMINATED) {
            return;
        }

        final LocalDateTime now = LocalDateTime.now();
        final Long partyId = party.getId();

        partyMapper.terminateParty(partyId, now);

        final String productName = subProductMapper.findById(party.getProductId())
                .map(p -> p.getServiceName())
                .orElse("서비스");

        final List<PartyMember> members = partyMemberMapper.findMembersByPartyId(partyId);
        for (final PartyMember member : members) {
            if (member.getStatus() == PartyMemberStatus.LEFT
                    || member.getStatus() == PartyMemberStatus.REMOVED) {
                continue;
            }

            partyMemberMapper.updateStatusAndLeftAt(member.getId(), PartyMemberStatus.LEFT);

            violationRecordMapper.insert(UserViolationRecord.builder()
                    .userId(member.getUserId())
                    .partyId(partyId)
                    .violationType(ViolationType.PARTY_DISSOLVED)
                    .weight(BigDecimal.ONE)
                    .build());

            notificationCommandService.notifyWithWebContent(
                    member.getUserId(), partyId,
                    NotificationType.PARTY_DISSOLVED_FINAL,
                    smsTemplate.getTitle(NotificationType.PARTY_DISSOLVED_FINAL),
                    smsTemplate.partyDissolvedFinal(productName),
                    webTemplate.partyDissolvedFinal(productName)
            );

            // 파티장 제외 파티원 자동 재매칭 대기열 등록
            if (!member.getUserId().equals(party.getHostUserId())) {
                final MatchWaitingQueue queue = MatchWaitingQueue.builder()
                        .productId(party.getProductId())
                        .userId(member.getUserId())
                        .status(MatchWaitingStatus.WAITING)
                        .requestedAt(now)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                matchWaitingQueueMapper.insertIfAbsent(queue);
            }
        }

        incidentMapper.findByPartyId(partyId).forEach(incident -> {
            if (incident.getStatus() != IncidentStatus.RESOLVED
                    && incident.getStatus() != IncidentStatus.PARTY_DISSOLVED) {
                incidentMapper.updateStatus(incident.getId(), IncidentStatus.PARTY_DISSOLVED);
            }
        });

        partyMapper.updateWarningLevel(partyId, 0);

        log.info("동시접속 위반으로 파티 해체 완료. partyId={}", partyId);
    }
}
