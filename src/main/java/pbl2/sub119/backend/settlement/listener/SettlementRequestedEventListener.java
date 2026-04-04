package pbl2.sub119.backend.settlement.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pbl2.sub119.backend.settlement.event.SettlementRequestedEvent;
import pbl2.sub119.backend.settlement.service.SettlementService;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementRequestedEventListener {

    private final SettlementService settlementService;

    @EventListener
    public void handle(SettlementRequestedEvent event) {
        log.info("정산 처리 시작. partyId={}, partyCycleId={}",
                event.partyId(), event.partyCycleId());

        settlementService.process(event.partyId(), event.partyCycleId());
    }
}