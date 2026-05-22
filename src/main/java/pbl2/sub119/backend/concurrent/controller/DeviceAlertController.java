package pbl2.sub119.backend.concurrent.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.concurrent.controller.docs.ConcurrentDocs;
import pbl2.sub119.backend.concurrent.dto.request.DeviceReportRequest;
import pbl2.sub119.backend.concurrent.dto.request.DeviceResponseRequest;
import pbl2.sub119.backend.concurrent.dto.response.DeviceReportResult;
import pbl2.sub119.backend.concurrent.dto.response.DeviceResponseResult;
import pbl2.sub119.backend.concurrent.service.DeviceDetectionService;

@RestController
@RequestMapping("/api/v1/device-alerts")
@RequiredArgsConstructor
public class DeviceAlertController implements ConcurrentDocs.DeviceAlert {

    private final DeviceDetectionService deviceDetectionService;

    // 낯선 기기 감지 신고 (파티장 또는 파티원)
    @PostMapping("/{partyId}/report")
    public ResponseEntity<DeviceReportResult> report(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId,
            @RequestBody @Valid final DeviceReportRequest request
    ) {
        return ResponseEntity.ok(
                deviceDetectionService.report(partyId, accessor.getUserId(), request)
        );
    }

    // 기기 감지 알림에 응답 (내 기기 여부)
    @PostMapping("/{alertId}/respond")
    public ResponseEntity<DeviceResponseResult> respond(
            @Auth final Accessor accessor,
            @PathVariable final Long alertId,
            @RequestBody @Valid final DeviceResponseRequest request
    ) {
        return ResponseEntity.ok(
                deviceDetectionService.respond(alertId, accessor.getUserId(), request.getIsMyDevice())
        );
    }
}
