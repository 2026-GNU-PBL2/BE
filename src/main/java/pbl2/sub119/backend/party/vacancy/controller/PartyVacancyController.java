package pbl2.sub119.backend.party.vacancy.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    // 파티원 결원 예정/결원 파티 목록 조회
    @Override
    public ResponseEntity<List<MemberVacancyPartyResponse>> getMemberVacancyParties(
            final String productId
    ) {
        return ResponseEntity.ok(
                partyVacancyQueryService.getMemberVacancyParties(productId)
        );
    }

    // 파티장 결원 예정/결원 파티 목록 조회
    @Override
    public ResponseEntity<List<HostVacancyPartyResponse>> getHostVacancyParties(
            final String productId
    ) {
        return ResponseEntity.ok(
                partyVacancyQueryService.getHostVacancyParties(productId)
        );
    }

    // 파티원 결원 예정/결원 파티 상세 조회
    @Override
    public ResponseEntity<PartyVacancyDetailResponse> getMemberVacancyDetail(
            final Long partyId
    ) {
        return ResponseEntity.ok(
                partyVacancyQueryService.getMemberVacancyDetail(partyId)
        );
    }

    // 파티장 결원 예정/결원 파티 상세 조회
    @Override
    public ResponseEntity<PartyVacancyDetailResponse> getHostVacancyDetail(
            final Long partyId
    ) {
        return ResponseEntity.ok(
                partyVacancyQueryService.getHostVacancyDetail(partyId)
        );
    }

    // 결원 파티 직접 참여
    @Override
    public ResponseEntity<PartyVacancyJoinResponse> joinMemberVacancyParty(
            final Accessor accessor,
            final Long partyId
    ) {
        return ResponseEntity.ok(
                partyVacancyCommandService.joinMemberVacancyParty(
                        partyId,
                        accessor.getUserId()
                )
        );
    }
}