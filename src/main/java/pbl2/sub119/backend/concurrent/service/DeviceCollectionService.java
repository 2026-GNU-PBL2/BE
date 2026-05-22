package pbl2.sub119.backend.concurrent.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.concurrent.dto.response.DeviceRegisterResult;
import pbl2.sub119.backend.concurrent.entity.PartyMemberDevice;
import pbl2.sub119.backend.concurrent.enumerated.RegistrationMethod;
import pbl2.sub119.backend.concurrent.exception.ConcurrentException;
import pbl2.sub119.backend.concurrent.mapper.PartyMemberDeviceMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMemberMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceCollectionService {

    private final PartyMemberDeviceMapper partyMemberDeviceMapper;
    private final PartyMemberMapper partyMemberMapper;

    @Transactional
    public void collect(
            final Long userId,
            final Long partyId,
            final String deviceType,
            final String os,
            final String browser,
            final String ipLocation,
            final boolean isVpn
    ) {
        try {
            partyMemberDeviceMapper.insert(PartyMemberDevice.builder()
                    .userId(userId)
                    .partyId(partyId)
                    .deviceType(deviceType)
                    .os(os)
                    .browser(browser)
                    .ipLocation(ipLocation)
                    .vpn(isVpn)
                    .registrationMethod(RegistrationMethod.AUTO)
                    .build());
        } catch (Exception e) {
            log.warn("기기 정보 수집 실패. userId={}, error={}", userId, e.getMessage());
        }
    }

    @Transactional
    public DeviceRegisterResult registerManual(
            final Long userId,
            final Long partyId,
            final String deviceType,
            final String os,
            final String browser
    ) {
        if (partyMemberMapper.findByPartyIdAndUserId(partyId, userId) == null) {
            throw new ConcurrentException(ErrorCode.CONCURRENT_NOT_PARTY_MEMBER);
        }

        final PartyMemberDevice device = PartyMemberDevice.builder()
                .userId(userId)
                .partyId(partyId)
                .deviceType(deviceType)
                .os(os)
                .browser(browser)
                .vpn(false)
                .registrationMethod(RegistrationMethod.MANUAL)
                .registeredAt(LocalDateTime.now())
                .build();
        partyMemberDeviceMapper.insert(device);

        return DeviceRegisterResult.builder()
                .deviceId(device.getId())
                .partyId(partyId)
                .deviceType(deviceType)
                .os(os)
                .browser(browser)
                .registrationMethod(RegistrationMethod.MANUAL)
                .registeredAt(device.getRegisteredAt())
                .build();
    }
}
