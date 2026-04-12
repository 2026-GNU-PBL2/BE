package pbl2.sub119.backend.party.cycle.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// 내부 이용 주기 시작 이벤트 요청
public record PartyCycleStartEventRequest(
        @Positive
        @NotNull
        Long partyId
) {
}