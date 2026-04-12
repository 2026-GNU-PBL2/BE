package pbl2.sub119.backend.party.cycle.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.auth.annotation.AdminOnly;
import pbl2.sub119.backend.party.cycle.dto.request.PartyCycleStartEventRequest;
import pbl2.sub119.backend.party.cycle.service.PartyCycleService;

@AdminOnly
@RestController
@RequestMapping("/internal/v1/party")
@RequiredArgsConstructor
public class PartyCycleInternalController {

    private final PartyCycleService partyCycleService;

    // 내부 이용 주기 시작 이벤트 수신
    @PostMapping("/cycle-start")
    public ResponseEntity<Void> handleCycleStart(
            @RequestBody @Valid final PartyCycleStartEventRequest request
    ) {
        partyCycleService.handleCycleStart(request.partyId());
        return ResponseEntity.ok().build();
    }
}