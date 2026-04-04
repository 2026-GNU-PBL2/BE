package pbl2.sub119.backend.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.enumerated.BillingKeyStatus;
import pbl2.sub119.backend.common.enumerated.PartyCycleStatus;
import pbl2.sub119.backend.party.enumerated.OperationStatus;
import pbl2.sub119.backend.party.enumerated.PartyHistoryEventType;
import pbl2.sub119.backend.party.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.enumerated.PartyRole;
import pbl2.sub119.backend.party.mapper.PartyMapper;
import pbl2.sub119.backend.party.mapper.PartyMemberMapper;
import pbl2.sub119.backend.party.service.PartyHistoryService;
import pbl2.sub119.backend.payment.dto.PaymentChargeTarget;
import pbl2.sub119.backend.payment.entity.PartyCycle;
import pbl2.sub119.backend.payment.mapper.PartyCycleMapper;
import pbl2.sub119.backend.payment.mapper.PaymentExecutionQueryMapper;
import pbl2.sub119.backend.toss.client.TossPaymentClient;
import pbl2.sub119.backend.toss.dto.request.TossBillingPaymentRequest;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoPaymentService {

    private final PartyCycleMapper partyCycleMapper;
    private final PaymentExecutionQueryMapper paymentExecutionQueryMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final PartyMapper partyMapper;
    private final PartyHistoryService partyHistoryService;
    private final TossPaymentClient tossPaymentClient;

    @Transactional
    public void execute(Long partyId, Long partyCycleId) {
        PartyCycle cycle = partyCycleMapper.findById(partyCycleId);
        if (cycle == null) {
            return;
        }

        if (cycle.getStatus() != PartyCycleStatus.PAYMENT_PENDING) {
            return;
        }

        partyMapper.updateOperationStatus(partyId, OperationStatus.ACTIVE);

        List<PaymentChargeTarget> targets = paymentExecutionQueryMapper.findChargeTargets(
                partyId,
                partyCycleId,
                PartyRole.MEMBER,
                PartyMemberStatus.PENDING,
                BillingKeyStatus.ACTIVE
        );

        if (targets.isEmpty()) {
            partyCycleMapper.updateStatus(partyCycleId, PartyCycleStatus.FAILED);
            partyMapper.updateOperationStatus(partyId, OperationStatus.WAITING_START);

            partyHistoryService.saveHistory(
                    partyId,
                    null,
                    PartyHistoryEventType.PAYMENT_EXECUTION_FAILED,
                    "{\"reason\":\"NO_CHARGE_TARGET\"}",
                    0L
            );
            return;
        }

        for (PaymentChargeTarget target : targets) {
            try {
                tossPaymentClient.executeBillingPayment(
                        target.getBillingKey(),
                        new TossBillingPaymentRequest(
                                target.getCustomerKey(),
                                target.getAmount(),
                                createOrderId(target),
                                "Submate 파티 이용료"
                        )
                );
            } catch (Exception e) {
                log.error("자동결제 실패. partyId={}, partyCycleId={}, memberId={}, userId={}",
                        partyId, partyCycleId, target.getMemberId(), target.getUserId(), e);

                partyCycleMapper.updateStatus(partyCycleId, PartyCycleStatus.FAILED);
                partyMapper.updateOperationStatus(partyId, OperationStatus.WAITING_START);

                partyHistoryService.saveHistory(
                        partyId,
                        target.getMemberId(),
                        PartyHistoryEventType.PAYMENT_EXECUTION_FAILED,
                        "{\"userId\":" + target.getUserId() + ",\"reason\":\"PAYMENT_API_FAILED\"}",
                        target.getUserId()
                );
                return;
            }
        }

        for (PaymentChargeTarget target : targets) {
            partyMemberMapper.updateStatusAndActivatedAt(
                    target.getMemberId(),
                    PartyMemberStatus.ACTIVE
            );

            partyHistoryService.saveHistory(
                    partyId,
                    target.getMemberId(),
                    PartyHistoryEventType.PAYMENT_EXECUTION_SUCCEEDED,
                    "{\"userId\":" + target.getUserId() + "}",
                    target.getUserId()
            );
        }

        partyCycleMapper.updateStatus(partyCycleId, PartyCycleStatus.RUNNING);
        partyMapper.updateOperationStatus(partyId, OperationStatus.ACTIVE);

        log.info("자동결제 전원 성공. partyId={}, partyCycleId={}", partyId, partyCycleId);
    }

    private String createOrderId(PaymentChargeTarget target) {
        return "party-" + target.getPartyId()
                + "-cycle-" + target.getPartyCycleId()
                + "-user-" + target.getUserId()
                + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}