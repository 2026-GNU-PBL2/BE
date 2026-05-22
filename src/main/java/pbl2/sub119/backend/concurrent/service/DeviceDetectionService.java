package pbl2.sub119.backend.concurrent.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.concurrent.dto.response.DeviceResponseResult;
import pbl2.sub119.backend.concurrent.entity.DeviceDetectionEvent;
import pbl2.sub119.backend.concurrent.entity.UserViolationRecord;
import pbl2.sub119.backend.concurrent.enumerated.DeviceDetectionStatus;
import pbl2.sub119.backend.concurrent.enumerated.ViolationType;
import pbl2.sub119.backend.concurrent.exception.ConcurrentException;
import pbl2.sub119.backend.concurrent.mapper.DeviceDetectionMapper;
import pbl2.sub119.backend.concurrent.mapper.PartyMemberDeviceMapper;
import pbl2.sub119.backend.concurrent.mapper.UserViolationRecordMapper;
import pbl2.sub119.backend.notification.enumerated.NotificationType;
import pbl2.sub119.backend.notification.service.NotificationCommandService;
import pbl2.sub119.backend.notification.service.SmsMessageTemplateService;
import pbl2.sub119.backend.notification.service.WebMessageTemplateService;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.subproduct.mapper.SubProductMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import pbl2.sub119.backend.concurrent.dto.request.DeviceReportRequest;
import pbl2.sub119.backend.concurrent.dto.response.DeviceReportResult;
import pbl2.sub119.backend.concurrent.entity.PartyMemberDevice;
import pbl2.sub119.backend.party.common.entity.PartyMember;
import pbl2.sub119.backend.party.common.mapper.PartyMemberMapper;

@Service
@RequiredArgsConstructor
public class DeviceDetectionService {

    private final DeviceDetectionMapper deviceDetectionMapper;
    private final PartyMemberDeviceMapper partyMemberDeviceMapper;
    private final PartyMapper partyMapper;
    private final SubProductMapper subProductMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final UserViolationRecordMapper violationRecordMapper;
    private final NotificationCommandService notificationCommandService;
    private final SmsMessageTemplateService smsTemplate;
    private final WebMessageTemplateService webTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public DeviceReportResult report(final Long partyId, final Long reportedBy, final DeviceReportRequest request) {
        if (partyMemberMapper.findByPartyIdAndUserId(partyId, reportedBy) == null) {
            throw new ConcurrentException(ErrorCode.CONCURRENT_NOT_PARTY_MEMBER);
        }

        final List<PartyMember> members = partyMemberMapper.findMembersByPartyId(partyId);
        final List<Long> memberIds = members.stream()
                .map(PartyMember::getUserId)
                .collect(Collectors.toList());

        String notifiedUserIds;
        try {
            notifiedUserIds = objectMapper.writeValueAsString(memberIds);
        } catch (JsonProcessingException e) {
            notifiedUserIds = "[]";
        }

        final LocalDateTime now = LocalDateTime.now();
        final DeviceDetectionEvent event = DeviceDetectionEvent.builder()
                .partyId(partyId)
                .detectedDevice(request.getDetectedDevice())
                .detectedLocation(request.getDetectedLocation())
                .detectedAt(now)
                .status(DeviceDetectionStatus.PENDING)
                .notifiedUserIds(notifiedUserIds)
                .expiresAt(now.plusHours(24))
                .build();
        deviceDetectionMapper.insert(event);

        final Party party = partyMapper.findById(partyId);
        final String productName = subProductMapper.findById(party.getProductId())
                .map(p -> p.getServiceName()).orElse("서비스");

        for (final PartyMember member : members) {
            notificationCommandService.notifyWithWebContent(
                    member.getUserId(), partyId,
                    NotificationType.DEVICE_CHECK_REQUEST,
                    smsTemplate.getTitle(NotificationType.DEVICE_CHECK_REQUEST),
                    smsTemplate.deviceCheckRequest(request.getDetectedDevice(), request.getDetectedLocation()),
                    webTemplate.deviceCheckRequest(request.getDetectedDevice(), request.getDetectedLocation())
            );
        }

        final List<PartyMemberDevice> devices = partyMemberDeviceMapper.findByPartyId(partyId);
        final List<DeviceReportResult.RegisteredDeviceInfo> registeredDevices = devices.stream()
                .map(DeviceReportResult.RegisteredDeviceInfo::from)
                .collect(Collectors.toList());

        return DeviceReportResult.builder()
                .alertId(event.getId())
                .partyId(partyId)
                .notifiedCount(members.size())
                .expiresAt(event.getExpiresAt())
                .registeredDevices(registeredDevices)
                .build();
    }

    @Transactional
    public DeviceResponseResult respond(final Long alertId, final Long userId, final boolean isMyDevice) {
        final DeviceDetectionEvent event = deviceDetectionMapper.findById(alertId);
        if (event == null) {
            throw new ConcurrentException(ErrorCode.DEVICE_ALERT_NOT_FOUND);
        }
        if (event.getStatus() != DeviceDetectionStatus.PENDING
                || event.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ConcurrentException(ErrorCode.DEVICE_ALERT_EXPIRED);
        }
        if (partyMemberMapper.findByPartyIdAndUserId(event.getPartyId(), userId) == null) {
            throw new ConcurrentException(ErrorCode.CONCURRENT_NOT_PARTY_MEMBER);
        }
        if (deviceDetectionMapper.markUserRespondedIfAbsent(alertId, userId) == 0) {
            throw new ConcurrentException(ErrorCode.DEVICE_ALERT_ALREADY_RESPONDED);
        }

        if (isMyDevice) {
            deviceDetectionMapper.incrementMineCount(alertId);
        } else {
            deviceDetectionMapper.incrementUnknownCount(alertId);
        }

        final DeviceDetectionEvent updated = deviceDetectionMapper.findById(alertId);
        final int total = updated.getMineCount() + updated.getUnknownCount();

        if (total > 0 && updated.getUnknownCount() * 2 > total) {
            final int affected = deviceDetectionMapper.updateStatusIfPending(alertId, DeviceDetectionStatus.REPORTED_UNKNOWN);
            if (affected > 0) {
                notifyHostOnUnknownMajority(updated);
            }
        } else if (total > 0 && updated.getMineCount() * 2 > total) {
            deviceDetectionMapper.updateStatusIfPending(alertId, DeviceDetectionStatus.CONFIRMED_MINE);
        }

        final DeviceDetectionEvent result = deviceDetectionMapper.findById(alertId);
        return DeviceResponseResult.builder()
                .alertId(alertId)
                .status(result.getStatus())
                .mineCount(result.getMineCount())
                .unknownCount(result.getUnknownCount())
                .responseCount(result.getResponseCount())
                .build();
    }

    private void notifyHostOnUnknownMajority(final DeviceDetectionEvent event) {
        final Party party = partyMapper.findById(event.getPartyId());
        if (party == null) return;

        final String productName = subProductMapper.findById(party.getProductId())
                .map(p -> p.getServiceName()).orElse("서비스");
        final String device = event.getDetectedDevice() != null ? event.getDetectedDevice() : "알 수 없는 기기";
        final String location = event.getDetectedLocation() != null ? event.getDetectedLocation() : "알 수 없는 위치";

        violationRecordMapper.insert(UserViolationRecord.builder()
                .userId(party.getHostUserId())
                .partyId(party.getId())
                .violationType(ViolationType.DEVICE_ALERT_NO_RESPONSE)
                .weight(new BigDecimal("0.5"))
                .build());

        notificationCommandService.notifyWithWebContent(
                party.getHostUserId(), party.getId(),
                NotificationType.DEVICE_ALERT,
                smsTemplate.getTitle(NotificationType.DEVICE_ALERT),
                smsTemplate.deviceAlert(device, location),
                webTemplate.deviceAlert(device, location)
        );
    }
}
