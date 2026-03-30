package pbl2.sub119.backend.party.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.party.controller.docs.PartyDocs;
import pbl2.sub119.backend.party.dto.request.MatchWaitingRegisterRequest;
import pbl2.sub119.backend.party.dto.request.PartyCreateRequest;
import pbl2.sub119.backend.party.dto.request.PartyJoinRequest;
import pbl2.sub119.backend.party.dto.response.JoinOrQueueResponse;
import pbl2.sub119.backend.party.dto.response.PartyCreateResponse;
import pbl2.sub119.backend.party.dto.response.PartyDetailResponse;
import pbl2.sub119.backend.party.dto.response.PartyListResponse;
import pbl2.sub119.backend.party.service.*;

@RestController
@RequestMapping("/api/v1/parties")
@RequiredArgsConstructor
public class PartyController implements PartyDocs {

    private final PartyCommandService partyCommandService;
    private final PartyQueryService partyQueryService;
    private final PartyJoinService partyJoinService;
    private final AutoMatchService autoMatchService;
    private final MatchWaitingService matchWaitingService;

    @Override
    public ResponseEntity<PartyCreateResponse> createParty(
            @Auth final Accessor accessor,
            @RequestBody @Valid final PartyCreateRequest request
    ) {
        return ResponseEntity.ok(
                partyCommandService.createParty(accessor.getUserId(), request)
        );
    }

    @Override
    public ResponseEntity<PartyDetailResponse> getPartyDetail(final Long partyId) {
        return ResponseEntity.ok(
                partyQueryService.getPartyDetail(partyId)
        );
    }

    @Override
    public ResponseEntity<List<PartyListResponse>> getPartiesByProduct(final String productId) {
        return ResponseEntity.ok(
                partyQueryService.getPartiesByProduct(productId)
        );
    }

    @Override
    public ResponseEntity<Void> joinParty(
            @Auth final Accessor accessor,
            final Long partyId
    ) {
        partyJoinService.joinParty(partyId, accessor.getUserId());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<JoinOrQueueResponse> joinOrQueue(
            @Auth final Accessor accessor,
            @RequestBody @Valid final MatchWaitingRegisterRequest request
    ) {
        return ResponseEntity.ok(
                autoMatchService.requestJoinOrQueue(request.productId(), accessor.getUserId())
        );
    }

    @Override
    public ResponseEntity<Void> cancelWaiting(
            @Auth final Accessor accessor,
            final Long waitingId
    ) {
        matchWaitingService.cancelWaiting(waitingId, accessor.getUserId());
        return ResponseEntity.ok().build();
    }
}