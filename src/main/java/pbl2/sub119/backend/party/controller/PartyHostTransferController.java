package pbl2.sub119.backend.party.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.party.controller.docs.PartyHostTransferDocs;
import pbl2.sub119.backend.party.dto.request.HostTransferRequestCreateRequest;
import pbl2.sub119.backend.party.dto.response.HostTransferResponse;
import pbl2.sub119.backend.party.service.PartyHostTransferService;

// 파티장 탈퇴 예약 및 승계
@RestController
@RequestMapping("/api/v1/party-host-transfer")
@RequiredArgsConstructor
public class PartyHostTransferController implements PartyHostTransferDocs {

    private final PartyHostTransferService partyHostTransferService;

    // 파티원에게 승계 요청
    @Override
    public ResponseEntity<HostTransferResponse> requestTransfer(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId,
            @RequestBody @Valid final HostTransferRequestCreateRequest request
    ) {
        return ResponseEntity.ok(
                partyHostTransferService.requestTransfer(partyId, accessor.getUserId(), request.targetUserId())
        );
    }

    // 승계 수락
    @Override
    public ResponseEntity<Void> acceptTransfer(
            @Auth final Accessor accessor,
            @PathVariable final Long requestId
    ) {
        partyHostTransferService.acceptTransfer(requestId, accessor.getUserId());
        return ResponseEntity.ok().build();
    }

    // 승계 거절
    @Override
    public ResponseEntity<Void> rejectTransfer(
            @Auth final Accessor accessor,
            @PathVariable final Long requestId
    ) {
        partyHostTransferService.rejectTransfer(requestId, accessor.getUserId());
        return ResponseEntity.ok().build();
    }

    // 승계 진행 상태
    @Override
    public ResponseEntity<List<HostTransferResponse>> getTransferRequests(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId
    ) {
        return ResponseEntity.ok(
                partyHostTransferService.getTransferRequests(partyId, accessor.getUserId())
        );
    }
}