package pbl2.sub119.backend.party.vacancy.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.party.vacancy.controller.docs.PartyVacancyDocs;
import pbl2.sub119.backend.party.vacancy.dto.response.HostVacancyPartyResponse;
import pbl2.sub119.backend.party.vacancy.dto.response.MemberVacancyPartyResponse;
import pbl2.sub119.backend.party.vacancy.dto.response.PartyVacancyDetailResponse;
import pbl2.sub119.backend.party.vacancy.dto.response.PartyVacancyJoinResponse;
import pbl2.sub119.backend.party.vacancy.service.PartyVacancyCommandService;
import pbl2.sub119.backend.party.vacancy.service.PartyVacancyQueryService;

@RestController
@RequestMapping("/api/v1/party-vacancy")
@RequiredArgsConstructor
public class PartyVacancyController implements PartyVacancyDocs {

    private final PartyVacancyQueryService partyVacancyQueryService;
    private final PartyVacancyCommandService partyVacancyCommandService;

    @Override
    public ResponseEntity<List<MemberVacancyPartyResponse>> getMemberVacancyParties(
            @RequestParam(required = false) final String productId
    ) {
        return ResponseEntity.ok(
                partyVacancyQueryService.getMemberVacancyParties(productId)
        );
    }

    @Override
    public ResponseEntity<List<HostVacancyPartyResponse>> getHostVacancyParties(
            @RequestParam(required = false) final String productId
    ) {
        return ResponseEntity.ok(
                partyVacancyQueryService.getHostVacancyParties(productId)
        );
    }

    @Override
    public ResponseEntity<PartyVacancyDetailResponse> getMemberVacancyDetail(
            @PathVariable final Long partyId
    ) {
        return ResponseEntity.ok(
                partyVacancyQueryService.getMemberVacancyDetail(partyId)
        );
    }

    @Override
    public ResponseEntity<PartyVacancyJoinResponse> joinMemberVacancyParty(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId
    ) {
        return ResponseEntity.ok(
                partyVacancyCommandService.joinMemberVacancyParty(partyId, accessor.getUserId())
        );
    }
}