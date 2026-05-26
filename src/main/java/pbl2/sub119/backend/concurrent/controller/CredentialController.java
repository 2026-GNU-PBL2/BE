package pbl2.sub119.backend.concurrent.controller;

import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.concurrent.controller.docs.ConcurrentDocs;
import pbl2.sub119.backend.concurrent.dto.response.CredentialResponse;
import pbl2.sub119.backend.concurrent.service.CredentialService;
import pbl2.sub119.backend.concurrent.service.DeviceCollectionService;

@Slf4j
@RestController
@RequestMapping("/api/v1/credentials")
@RequiredArgsConstructor
public class CredentialController implements ConcurrentDocs.Credential {

    private final CredentialService credentialService;
    private final DeviceCollectionService deviceCollectionService;
    private final HttpServletRequest httpServletRequest;

    // 파티원 이용 정보 조회 — 공유 계정 접속 시점에 기기 자동 수집
    @GetMapping("/{partyId}")
    public ResponseEntity<CredentialResponse> getCredential(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId
    ) {
        collectDevice(accessor.getUserId(), partyId);
        return ResponseEntity.ok(credentialService.getCredential(partyId, accessor.getUserId()));
    }

    private void collectDevice(final Long userId, final Long partyId) {
        try {
            final String uaString = httpServletRequest.getHeader("User-Agent");
            if (uaString == null || uaString.isBlank()) return;

            final UserAgent ua = UserAgent.parseUserAgentString(uaString);
            final String deviceType = ua.getOperatingSystem().getDeviceType().getName();
            final String os = ua.getOperatingSystem().getName();
            final String browser = ua.getBrowser().getName();
            final String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
            final String ip = (xForwardedFor != null && !xForwardedFor.isBlank())
                    ? xForwardedFor.split(",")[0].trim()
                    : httpServletRequest.getRemoteAddr();

            deviceCollectionService.collect(userId, partyId, deviceType, os, browser, ip, false);
        } catch (Exception e) {
            log.debug("기기 정보 수집 실패. userId={}, partyId={}", userId, partyId);
        }
    }

}
