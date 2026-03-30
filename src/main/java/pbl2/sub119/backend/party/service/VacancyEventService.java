package pbl2.sub119.backend.party.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.party.enumerated.PartyHistoryEventType;

@Service
@RequiredArgsConstructor
public class VacancyEventService {

    private final AutoMatchService autoMatchService;
    private final PartyHistoryService partyHistoryService;

    @Transactional
    public void handleVacancy(String productId, Long partyId, Long triggeredBy) {
        partyHistoryService.saveHistory(
                partyId,
                null,
                PartyHistoryEventType.VACANCY_OPENED,
                "{\"productId\":\"" + productId + "\",\"partyId\":" + partyId + "}",
                triggeredBy
        );

        autoMatchService.matchWaitingUserToVacancy(productId, partyId);
    }
}