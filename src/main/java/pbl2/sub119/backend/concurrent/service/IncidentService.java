package pbl2.sub119.backend.concurrent.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.concurrent.dto.response.IncidentResult;
import pbl2.sub119.backend.concurrent.dto.response.ResolveResult;
import pbl2.sub119.backend.concurrent.entity.ConcurrentIncident;
import pbl2.sub119.backend.concurrent.entity.UserViolationRecord;
import pbl2.sub119.backend.concurrent.enumerated.DetectionSource;
import pbl2.sub119.backend.concurrent.enumerated.IncidentStatus;
import pbl2.sub119.backend.concurrent.enumerated.ViolationType;
import pbl2.sub119.backend.concurrent.enumerated.WarningLevel;
import pbl2.sub119.backend.concurrent.exception.ConcurrentException;
import pbl2.sub119.backend.concurrent.mapper.ConcurrentIncidentMapper;
import pbl2.sub119.backend.concurrent.mapper.UserViolationRecordMapper;
import pbl2.sub119.backend.notification.enumerated.NotificationType;
import pbl2.sub119.backend.notification.service.NotificationCommandService;
import pbl2.sub119.backend.notification.service.SmsMessageTemplateService;
import pbl2.sub119.backend.notification.service.WebMessageTemplateService;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.entity.PartyMember;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMemberMapper;
import pbl2.sub119.backend.party.provision.entity.PartyProvision;
import pbl2.sub119.backend.party.provision.enumerated.ProvisionStatus;
import pbl2.sub119.backend.party.provision.mapper.PartyProvisionMapper;
import pbl2.sub119.backend.subproduct.entity.SubProduct;
import pbl2.sub119.backend.subproduct.enumerated.OperationType;
import pbl2.sub119.backend.subproduct.mapper.SubProductMapper;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final SubProductMapper subProductMapper;
    private final ConcurrentIncidentMapper incidentMapper;
    private final UserViolationRecordMapper violationRecordMapper;
    private final PartyProvisionMapper partyProvisionMapper;
    private final NotificationCommandService notificationCommandService;
    private final SmsMessageTemplateService smsTemplate;
    private final WebMessageTemplateService webTemplate;

    @Transactional
    public IncidentResult processReport(final Long partyId, final Long reportedBy, final String reportType) {
        final Party party = partyMapper.findById(partyId);
        if (party == null) {
            throw new ConcurrentException(ErrorCode.NOT_FOUND);
        }

        final SubProduct product = subProductMapper.findById(party.getProductId())
                .orElseThrow(() -> new ConcurrentException(ErrorCode.SUB_PRODUCT_NOT_FOUND));
        if (product.getOperationType() != OperationType.ACCOUNT_SHARE) {
            throw new ConcurrentException(ErrorCode.CONCURRENT_NOT_ACCOUNT_SHARE);
        }

        final PartyMember reporter = partyMemberMapper.findByPartyIdAndUserId(partyId, reportedBy);
        if (reporter == null) {
            throw new ConcurrentException(ErrorCode.CONCURRENT_NOT_PARTY_MEMBER);
        }

        final boolean hasPriorWarning = incidentMapper.hasAnyPriorWarning(partyId);

        if (hasPriorWarning) {
            return processSecondWarning(party, product, reportedBy, reportType);
        } else {
            return processFirstWarning(party, product, reportedBy, reportType);
        }
    }

    // 기기 감지 흐름(REPORTED_UNKNOWN / 만료)에서 호출 — 직접 신고와 동일한 경고 체계로 연결
    @Transactional
    public void processWarningFromDeviceDetection(final Long partyId) {
        final Party party = partyMapper.findById(partyId);
        if (party == null) {
            return;
        }

        final SubProduct product = subProductMapper.findById(party.getProductId()).orElse(null);
        if (product == null || product.getOperationType() != OperationType.ACCOUNT_SHARE) {
            return;
        }

        final boolean hasPriorWarning = incidentMapper.hasAnyPriorWarning(partyId);

        if (hasPriorWarning) {
            processSecondWarning(party, product, null, DetectionSource.DEVICE_DETECTION.name());
        } else {
            processFirstWarning(party, product, null, DetectionSource.DEVICE_DETECTION.name());
        }
    }

    @Transactional
    public ResolveResult resolveIncident(final Long incidentId, final Long hostUserId, final Long partyId) {
        final ConcurrentIncident incident = incidentMapper.findById(incidentId);
        if (incident == null) {
            throw new ConcurrentException(ErrorCode.CONCURRENT_INCIDENT_NOT_FOUND);
        }
        if (!incident.getPartyId().equals(partyId)) {
            throw new ConcurrentException(ErrorCode.CONCURRENT_NOT_PARTY_MEMBER);
        }
        final Party party = partyMapper.findById(partyId);
        if (party == null || !party.getHostUserId().equals(hostUserId)) {
            throw new ConcurrentException(ErrorCode.CONCURRENT_NOT_HOST);
        }
        if (incident.getStatus() != IncidentStatus.FIRST_WARNING_SENT
                && incident.getStatus() != IncidentStatus.DISSOLUTION_SCHEDULED) {
            throw new ConcurrentException(ErrorCode.CONCURRENT_NOT_RESOLVABLE);
        }

        // 인시던트 발생 이후 이용 재등록(setupProvision) 완료 여부 확인
        final PartyProvision provision = partyProvisionMapper.findByPartyId(partyId);
        final boolean provisionUpdated = provision != null
                && provision.getUpdatedAt() != null
                && provision.getUpdatedAt().isAfter(incident.getFirstWarnedAt())
                && provision.getOperationStatus() != ProvisionStatus.WAITING
                && provision.getOperationStatus() != ProvisionStatus.RESET_REQUIRED;
        if (!provisionUpdated) {
            throw new ConcurrentException(ErrorCode.CONCURRENT_PROVISION_NOT_UPDATED);
        }

        incidentMapper.updateResolved(incidentId);
        partyMapper.updateWarningLevel(partyId, 0);
        partyMapper.updateDissolutionDate(partyId, null);

        return ResolveResult.builder()
                .incidentId(incidentId)
                .status(IncidentStatus.RESOLVED)
                .build();
    }

    private IncidentResult processFirstWarning(
            final Party party, final SubProduct product,
            final Long reportedBy, final String reportType) {

        final DetectionSource source = reportedBy != null
                ? DetectionSource.MEMBER_REPORT
                : DetectionSource.DEVICE_DETECTION;
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime hostDeadline = now.plusHours(24);
        final String productName = product.getServiceName();

        final ConcurrentIncident incident = ConcurrentIncident.builder()
                .partyId(party.getId())
                .reportedBy(reportedBy)
                .detectionSource(source)
                .reportType(reportType)
                .status(IncidentStatus.OPEN)
                .build();
        incidentMapper.insert(incident);
        incidentMapper.updateFirstWarned(incident.getId(), now, hostDeadline);

        partyMapper.updateWarningLevel(party.getId(), 1);

        final List<PartyMember> members = partyMemberMapper.findMembersByPartyId(party.getId());
        for (final PartyMember member : members) {
            violationRecordMapper.insert(UserViolationRecord.builder()
                    .userId(member.getUserId())
                    .partyId(party.getId())
                    .incidentId(incident.getId())
                    .violationType(ViolationType.FIRST_WARNING)
                    .weight(BigDecimal.ONE)
                    .build());

            notificationCommandService.notifyWithWebContent(
                    member.getUserId(), party.getId(),
                    NotificationType.CONCURRENT_WARNING_1,
                    smsTemplate.getTitle(NotificationType.CONCURRENT_WARNING_1),
                    smsTemplate.concurrentWarning1(productName),
                    webTemplate.concurrentWarning1(productName)
            );
        }

        notificationCommandService.notifyWithWebContent(
                party.getHostUserId(), party.getId(),
                NotificationType.HOST_ACTION_REQUIRED_24H,
                smsTemplate.getTitle(NotificationType.HOST_ACTION_REQUIRED_24H),
                smsTemplate.hostActionRequired24h(productName, hostDeadline.toString()),
                webTemplate.hostActionRequired24h(productName, hostDeadline.toString())
        );

        return IncidentResult.builder()
                .incidentId(incident.getId())
                .partyId(party.getId())
                .warningLevel(WarningLevel.FIRST)
                .status(IncidentStatus.FIRST_WARNING_SENT)
                .hostDeadline(hostDeadline)
                .build();
    }

    private IncidentResult processSecondWarning(
            final Party party, final SubProduct product,
            final Long reportedBy, final String reportType) {

        final DetectionSource source = reportedBy != null
                ? DetectionSource.MEMBER_REPORT
                : DetectionSource.DEVICE_DETECTION;
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime hostDeadline = now.plusHours(12);
        final LocalDate dissolutionDate = LocalDate.now().plusDays(1);
        final String productName = product.getServiceName();

        final ConcurrentIncident incident = ConcurrentIncident.builder()
                .partyId(party.getId())
                .reportedBy(reportedBy)
                .detectionSource(source)
                .reportType(reportType)
                .status(IncidentStatus.OPEN)
                .build();
        incidentMapper.insert(incident);
        incidentMapper.updateFirstWarned(incident.getId(), now, hostDeadline);
        incidentMapper.updateDissolutionDate(incident.getId(), dissolutionDate);

        partyMapper.updateWarningLevel(party.getId(), 2);
        partyMapper.updateDissolutionDate(party.getId(), dissolutionDate);

        final List<PartyMember> members = partyMemberMapper.findMembersByPartyId(party.getId());
        for (final PartyMember member : members) {
            notificationCommandService.notifyWithWebContent(
                    member.getUserId(), party.getId(),
                    NotificationType.PARTY_DISSOLVING,
                    smsTemplate.getTitle(NotificationType.PARTY_DISSOLVING),
                    smsTemplate.partyDissolvingMember(productName, dissolutionDate.toString()),
                    webTemplate.partyDissolvingMember(productName, dissolutionDate.toString())
            );
        }

        notificationCommandService.notifyWithWebContent(
                party.getHostUserId(), party.getId(),
                NotificationType.HOST_URGENT_PASSWORD_CHANGE,
                smsTemplate.getTitle(NotificationType.HOST_URGENT_PASSWORD_CHANGE),
                smsTemplate.hostUrgentPasswordChange(productName, hostDeadline.toString(), dissolutionDate.toString()),
                webTemplate.hostUrgentPasswordChange(productName, hostDeadline.toString(), dissolutionDate.toString())
        );

        return IncidentResult.builder()
                .incidentId(incident.getId())
                .partyId(party.getId())
                .warningLevel(WarningLevel.SECOND)
                .status(IncidentStatus.DISSOLUTION_SCHEDULED)
                .hostDeadline(hostDeadline)
                .dissolutionDate(dissolutionDate)
                .build();
    }
}
