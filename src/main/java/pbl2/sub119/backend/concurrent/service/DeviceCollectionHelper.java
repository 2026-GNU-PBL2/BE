package pbl2.sub119.backend.concurrent.service;

import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 컨트롤러 계층에서 공통으로 기기 정보를 수집하기 위한 헬퍼.
 * 기기 수집이 필요한 모든 엔드포인트에서 이 클래스를 주입하여 사용한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceCollectionHelper {

    private final DeviceCollectionService deviceCollectionService;
    private final IpLocationService ipLocationService;

    public void collectSilently(final Long userId, final Long partyId, final HttpServletRequest request) {
        try {
            final String uaString = request.getHeader("User-Agent");
            if (uaString == null || uaString.isBlank()) return;

            final UserAgent ua = UserAgent.parseUserAgentString(uaString);
            final String deviceType = ua.getOperatingSystem().getDeviceType().getName();
            final String os = ua.getOperatingSystem().getName();
            final String browser = ua.getBrowser().getName();

            final String xForwardedFor = request.getHeader("X-Forwarded-For");
            final String ip = (xForwardedFor != null && !xForwardedFor.isBlank())
                    ? xForwardedFor.split(",")[0].trim()
                    : request.getRemoteAddr();
            final String ipLocation = ipLocationService.resolve(ip);

            final String vpnHeader = request.getHeader("X-Is-VPN");
            final boolean isVpn = "true".equalsIgnoreCase(vpnHeader);

            deviceCollectionService.collect(userId, partyId, deviceType, os, browser, ipLocation, isVpn);
        } catch (Exception e) {
            log.debug("기기 정보 수집 실패. userId={}, partyId={}", userId, partyId);
        }
    }
}
