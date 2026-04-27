package pbl2.sub119.backend.admin.party.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.admin.controller.docs.AdminDocs;
import pbl2.sub119.backend.admin.party.dto.AdminPartyDetailResponse;
import pbl2.sub119.backend.admin.party.dto.AdminPartyResponse;
import pbl2.sub119.backend.admin.party.service.AdminPartyService;
import pbl2.sub119.backend.auth.annotation.AdminOnly;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/parties")
@AdminOnly
public class AdminPartyController implements AdminDocs.Party {

    private final AdminPartyService adminPartyService;

    @Override
    public ResponseEntity<List<AdminPartyResponse>> getParties(
            @Auth final Accessor accessor
    ) {
        return ResponseEntity.ok(adminPartyService.getParties());
    }

    @Override
    public ResponseEntity<AdminPartyDetailResponse> getParty(
            @Auth final Accessor accessor,
            @PathVariable Long partyId
    ) {
        return ResponseEntity.ok(adminPartyService.getParty(partyId));
    }
}