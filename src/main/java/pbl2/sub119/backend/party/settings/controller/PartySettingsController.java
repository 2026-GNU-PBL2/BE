package pbl2.sub119.backend.party.settings.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.party.settings.controller.docs.PartySettingsDocs;
import pbl2.sub119.backend.party.settings.dto.response.PartyFeeDetailResponse;
import pbl2.sub119.backend.party.settings.dto.response.PartySettingsResponse;
import pbl2.sub119.backend.party.settings.service.PartySettingsQueryService;

@RestController
@RequestMapping("/api/v1/parties")
@RequiredArgsConstructor
public class PartySettingsController implements PartySettingsDocs {

    private final PartySettingsQueryService partySettingsQueryService;

    @Override
    public ResponseEntity<PartySettingsResponse> getPartySettings(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId
    ) {
        return ResponseEntity.ok(
                partySettingsQueryService.getSettings(accessor.getUserId(), partyId)
        );
    }

    @Override
    public ResponseEntity<PartyFeeDetailResponse> getPartyFeeDetail(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId
    ) {
        return ResponseEntity.ok(
                partySettingsQueryService.getFeeDetail(accessor.getUserId(), partyId)
        );
    }
}
