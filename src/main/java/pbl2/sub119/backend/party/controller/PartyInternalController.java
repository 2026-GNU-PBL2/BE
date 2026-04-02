package pbl2.sub119.backend.party.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.auth.annotation.AdminOnly;
import pbl2.sub119.backend.party.controller.docs.PartyInternalDocs;
import pbl2.sub119.backend.party.dto.request.PartyCycleStartEventRequest;
import pbl2.sub119.backend.party.service.PartyCycleService;

@AdminOnly
@RestController
@RequestMapping("/internal/v1/party")
@RequiredArgsConstructor
public class PartyInternalController implements PartyInternalDocs {

    private final PartyCycleService partyCycleService;

    // 이용 주기 시작 이벤트 수신
    @Override
    public ResponseEntity<Void> handleCycleStart(
            @RequestBody @Valid final PartyCycleStartEventRequest request
    ) {
        partyCycleService.handleCycleStart(request.partyId());
        return ResponseEntity.ok().build();
    }
}