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
import pbl2.sub119.backend.party.controller.docs.PartyWaitingDocs;
import pbl2.sub119.backend.party.dto.request.MatchWaitingRegisterRequest;
import pbl2.sub119.backend.party.dto.response.JoinOrQueueResponse;
import pbl2.sub119.backend.party.dto.response.MatchWaitingResponse;
import pbl2.sub119.backend.party.service.AutoMatchService;
import pbl2.sub119.backend.party.service.MatchWaitingService;

@RestController
@RequestMapping("/api/v1/party-waiting")
@RequiredArgsConstructor
public class PartyWaitingController implements PartyWaitingDocs {

    private final AutoMatchService autoMatchService;
    private final MatchWaitingService matchWaitingService;

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
            @PathVariable final Long waitingId
    ) {
        matchWaitingService.cancelWaiting(waitingId, accessor.getUserId());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<MatchWaitingResponse>> getMyWaitingList(
            @Auth final Accessor accessor
    ) {
        return ResponseEntity.ok(
                matchWaitingService.getMyWaitingList(accessor.getUserId())
        );
    }
}