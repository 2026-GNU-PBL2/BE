package pbl2.sub119.backend.party.history.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.party.history.controller.docs.PartyHistoryDocs;
import pbl2.sub119.backend.party.history.dto.PartyHistoryResponse;
import pbl2.sub119.backend.party.history.service.PartyHistoryQueryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/me/party-history")
public class PartyHistoryController implements PartyHistoryDocs {

    private final PartyHistoryQueryService partyHistoryQueryService;

    @Override
    public ResponseEntity<List<PartyHistoryResponse>> getMyPartyHistories(
            @Auth final Accessor accessor
    ) {
        return ResponseEntity.ok(
                partyHistoryQueryService.getMyPartyHistories(accessor.getUserId())
        );
    }
}