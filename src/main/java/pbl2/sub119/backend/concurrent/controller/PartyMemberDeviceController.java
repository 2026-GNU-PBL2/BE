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
import pbl2.sub119.backend.concurrent.dto.request.ManualDeviceRegisterRequest;
import pbl2.sub119.backend.concurrent.dto.response.DeviceRegisterResult;
import pbl2.sub119.backend.concurrent.service.DeviceCollectionService;

@RestController
@RequestMapping("/api/v1/party-member-devices")
@RequiredArgsConstructor
public class PartyMemberDeviceController implements ConcurrentDocs.PartyMemberDevice {

    private final DeviceCollectionService deviceCollectionService;

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
