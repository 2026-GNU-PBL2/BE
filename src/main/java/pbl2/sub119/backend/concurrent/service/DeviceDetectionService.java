package pbl2.sub119.backend.concurrent.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.concurrent.dto.response.DeviceResponseResult;
import pbl2.sub119.backend.concurrent.entity.DeviceDetectionEvent;
import pbl2.sub119.backend.concurrent.enumerated.DeviceDetectionStatus;
import pbl2.sub119.backend.concurrent.exception.ConcurrentException;
import pbl2.sub119.backend.concurrent.mapper.DeviceDetectionMapper;
import pbl2.sub119.backend.concurrent.mapper.PartyMemberDeviceMapper;
import pbl2.sub119.backend.notification.enumerated.NotificationType;
import pbl2.sub119.backend.notification.service.NotificationCommandService;
import pbl2.sub119.backend.notification.service.SmsMessageTemplateService;
import pbl2.sub119.backend.notification.service.WebMessageTemplateService;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;
import pbl2.sub119.backend.concurrent.dto.request.DeviceReportRequest;
import pbl2.sub119.backend.concurrent.dto.response.DeviceReportResult;
import pbl2.sub119.backend.concurrent.entity.DeviceDetectionResponse;
import pbl2.sub119.backend.concurrent.entity.PartyMemberDevice;
import pbl2.sub119.backend.concurrent.mapper.DeviceDetectionResponseMapper;
import pbl2.sub119.backend.party.common.entity.PartyMember;
import pbl2.sub119.backend.party.common.mapper.PartyMemberMapper;

@Service
@RequiredArgsConstructor
public class DeviceDetectionService {

    private final DeviceDetectionMapper deviceDetectionMapper;
    private final DeviceDetectionResponseMapper deviceDetectionResponseMapper;
    private final PartyMemberDeviceMapper partyMemberDeviceMapper;
    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final NotificationCommandService notificationCommandService;
    private final SmsMessageTemplateService smsTemplate;
    private final WebMessageTemplateService webTemplate;
    private final ObjectMapper objectMapper;
    private final IncidentService incidentService;

    @Transactional
    public DeviceReportResult report(final Long partyId, final Long reportedBy, final DeviceReportRequest request) {
        if (partyMemberMapper.findByPartyIdAndUserId(partyId, reportedBy) == null) {
            throw new ConcurrentException(ErrorCode.CONCURRENT_NOT_PARTY_MEMBER);
        }

        final List<PartyMember> members = partyMemberMapper.findMembersByPartyId(partyId);
        final List<Long> memberIds = members.stream()
                .map(PartyMember::getUserId)
                .filter(id -> !id.equals(reportedBy))  // 신고자는 알림 대상·응답 대상 모두 제외
                .collect(Collectors.toList());

        String notifiedUserIds;
        try {
            notifiedUserIds = objectMapper.writeValueAsString(memberIds);
        } catch (JsonProcessingException e) {
            notifiedUserIds = "[]";
        }

        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime detectedAt = request.getDetectedAt() != null ? request.getDetectedAt() : now;
        final DeviceDetectionEvent event = DeviceDetectionEvent.builder()
                .partyId(partyId)
                .detectedDevice(request.getDetectedDevice())
                .detectedLocation(request.getDetectedLocation())
                .detectedAt(detectedAt)
                .status(DeviceDetectionStatus.PENDING)
                .notifiedUserIds(notifiedUserIds)
                .expiresAt(now.plusHours(24))
                .build();
        deviceDetectionMapper.insert(event);

        for (final PartyMember member : members) {
            if (member.getUserId().equals(reportedBy)) {
                continue; // 신고자 본인은 제외
            }
            notificationCommandService.notifyWithWebContent(
                    member.getUserId(), partyId,
                    NotificationType.DEVICE_CHECK_REQUEST,
                    smsTemplate.getTitle(NotificationType.DEVICE_CHECK_REQUEST),
                    smsTemplate.deviceCheckRequest(request.getDetectedDevice(), request.getDetectedLocation()),
                    webTemplate.deviceCheckRequest(request.getDetectedDevice(), request.getDetectedLocation()),
                    event.getId()
            );
        }

        final List<PartyMemberDevice> devices = partyMemberDeviceMapper.findByPartyId(partyId);
        final List<DeviceReportResult.RegisteredDeviceInfo> registeredDevices = devices.stream()
                .map(DeviceReportResult.RegisteredDeviceInfo::from)
                .collect(Collectors.toList());

        return DeviceReportResult.builder()
                .alertId(event.getId())
                .partyId(partyId)
                .notifiedCount(memberIds.size())
                .expiresAt(event.getExpiresAt())
                .registeredDevices(registeredDevices)
                .build();
    }

    @Transactional
    public DeviceResponseResult respond(final Long alertId, final Long userId, final boolean isMyDevice) {
        // FOR UPDATE로 행 락을 잡아 동시 요청 직렬화
        final DeviceDetectionEvent event = deviceDetectionMapper.findByIdForUpdate(alertId);
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

        // 중복 응답 체크 — DB UNIQUE 제약이 최후 방어선이지만 사전 체크로 명확한 에러 반환
        if (deviceDetectionResponseMapper.existsByEventIdAndUserId(alertId, userId)) {
            throw new ConcurrentException(ErrorCode.DEVICE_ALERT_ALREADY_RESPONDED);
        }

        deviceDetectionResponseMapper.insert(DeviceDetectionResponse.builder()
                .detectionEventId(alertId)
                .userId(userId)
                .mine(isMyDevice)
                .respondedAt(LocalDateTime.now())
                .build());

        if (isMyDevice) {
            // 1명이라도 본인 기기라고 확인하면 즉시 CONFIRMED_MINE
            final int affected = deviceDetectionMapper.updateStatusIfPending(alertId, DeviceDetectionStatus.CONFIRMED_MINE);
            if (affected > 0) {
                notifyMembersOnConfirmedMine(event);
            }
        } else {
            // 알림 대상자 전원이 응답했고 아무도 내 기기라고 하지 않은 경우 → REPORTED_UNKNOWN → 인시던트 경고 처리
            final List<Long> notifiedIds = parseNotifiedUserIds(event.getNotifiedUserIds());
            final int responseCount = deviceDetectionResponseMapper.countByEventId(alertId);
            final int mineCount = deviceDetectionResponseMapper.countMineByEventId(alertId);
            if (responseCount >= notifiedIds.size() && mineCount == 0) {
                final int affected = deviceDetectionMapper.updateStatusIfPending(alertId, DeviceDetectionStatus.REPORTED_UNKNOWN);
                if (affected > 0) {
                    incidentService.processWarningFromDeviceDetection(event.getPartyId());
                }
            }
        }

        final DeviceDetectionEvent result = deviceDetectionMapper.findById(alertId);
        return DeviceResponseResult.builder()
                .alertId(alertId)
                .status(result.getStatus())
                .mineCount(deviceDetectionResponseMapper.countMineByEventId(alertId))
                .unknownCount(deviceDetectionResponseMapper.countUnknownByEventId(alertId))
                .responseCount(deviceDetectionResponseMapper.countByEventId(alertId))
                .build();
    }

    private void notifyMembersOnConfirmedMine(final DeviceDetectionEvent event) {
        final Party party = partyMapper.findById(event.getPartyId());
        if (party == null) return;

        final String device = event.getDetectedDevice() != null ? event.getDetectedDevice() : "알 수 없는 기기";
        final String location = event.getDetectedLocation() != null ? event.getDetectedLocation() : "알 수 없는 위치";

        final List<PartyMember> members = partyMemberMapper.findMembersByPartyId(party.getId());
        for (final PartyMember member : members) {
            notificationCommandService.notifyWithWebContent(
                    member.getUserId(), party.getId(),
                    NotificationType.DEVICE_CONFIRMED_MINE,
                    smsTemplate.getTitle(NotificationType.DEVICE_CONFIRMED_MINE),
                    smsTemplate.deviceConfirmedMine(device, location),
                    webTemplate.deviceConfirmedMine(device, location)
            );
        }
    }

    private List<Long> parseNotifiedUserIds(final String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<Long>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}
