package pbl2.sub119.backend.concurrent.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.concurrent.controller.docs.ConcurrentDocs;
import pbl2.sub119.backend.concurrent.dto.request.ManualDeviceRegisterRequest;
import pbl2.sub119.backend.concurrent.dto.response.DeviceRegisterResult;
import pbl2.sub119.backend.concurrent.dto.response.PartyMemberDeviceResponse;
import pbl2.sub119.backend.concurrent.service.DeviceCollectionHelper;
import pbl2.sub119.backend.concurrent.service.DeviceCollectionService;

@Slf4j
@RestController
@RequestMapping("/api/v1/party-member-devices")
@RequiredArgsConstructor
public class PartyMemberDeviceController implements ConcurrentDocs.PartyMemberDevice {

    private final DeviceCollectionService deviceCollectionService;
    private final DeviceCollectionHelper deviceCollectionHelper;
    private final HttpServletRequest httpServletRequest;

    @GetMapping("/{partyId}")
    public ResponseEntity<List<PartyMemberDeviceResponse>> getDevices(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId
    ) {
        deviceCollectionHelper.collectSilently(accessor.getUserId(), partyId, httpServletRequest);
        return ResponseEntity.ok(
                deviceCollectionService.getPartyDevices(partyId, accessor.getUserId())
        );
    }

    @PostMapping("/{partyId}")
    public ResponseEntity<DeviceRegisterResult> registerDevice(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId,
            @RequestBody @Valid final ManualDeviceRegisterRequest request
    ) {
        return ResponseEntity.ok(
                deviceCollectionService.registerManual(
                        accessor.getUserId(),
                        partyId,
                        request.getDeviceType(),
                        request.getOs(),
                        request.getBrowser()
                )
        );
    }
}
