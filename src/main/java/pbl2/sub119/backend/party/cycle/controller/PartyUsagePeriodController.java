package pbl2.sub119.backend.party.cycle.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.party.cycle.controller.docs.PartyUsagePeriodDocs;
import pbl2.sub119.backend.party.cycle.dto.response.PartyUsagePeriodResponse;
import pbl2.sub119.backend.party.cycle.service.PartyUsagePeriodQueryService;

@RestController
@RequestMapping("/api/v1/parties")
@RequiredArgsConstructor
public class PartyUsagePeriodController implements PartyUsagePeriodDocs {

    private final PartyUsagePeriodQueryService partyUsagePeriodQueryService;

    // 현재 이용 기간 조회
    @Override
    public ResponseEntity<PartyUsagePeriodResponse> getUsagePeriod(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId
    ) {
        return ResponseEntity.ok(
                partyUsagePeriodQueryService.getUsagePeriod(partyId)
        );
    }
}