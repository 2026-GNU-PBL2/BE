package pbl2.sub119.backend.party.settings.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.bankaccounts.entity.BankAccount;
import pbl2.sub119.backend.bankaccounts.enums.AccountType;
import pbl2.sub119.backend.bankaccounts.mapper.BankMapper;
import pbl2.sub119.backend.common.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.common.enumerated.PartyCycleStatus;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.entity.PartyMember;
import pbl2.sub119.backend.party.common.enumerated.PartyRole;
import pbl2.sub119.backend.party.common.exception.PartyException;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMemberMapper;
import pbl2.sub119.backend.party.settings.dto.response.PartyFeeDetailResponse;
import pbl2.sub119.backend.party.settings.dto.response.PartySettingsResponse;
import pbl2.sub119.backend.payment.entity.PartyCycle;
import pbl2.sub119.backend.payment.mapper.PartyCycleMapper;
import pbl2.sub119.backend.payment.policy.FeePolicy;
import pbl2.sub119.backend.subproduct.entity.SubProduct;
import pbl2.sub119.backend.subproduct.mapper.SubProductMapper;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartySettingsQueryService {

    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final SubProductMapper subProductMapper;
    private final PartyCycleMapper partyCycleMapper;
    private final BankMapper bankMapper;

    public PartySettingsResponse getSettings(final Long userId, final Long partyId) {
        final Party party = getParty(partyId);
        final PartyMember member = getActiveMember(userId, partyId);
        final SubProduct subProduct = getSubProduct(party.getProductId());
        final PartyCycle cycle = getRunningCycle(partyId);

        if (member.getRole() == PartyRole.HOST) {
            return buildHostSettings(party, subProduct, cycle);
        }
        return buildMemberSettings(party, subProduct, cycle);
    }

    public PartyFeeDetailResponse getFeeDetail(final Long userId, final Long partyId) {
        getParty(partyId);
        final PartyMember member = getActiveMember(userId, partyId);
        final PartyCycle cycle = getRunningCycle(partyId);

        if (member.getRole() == PartyRole.HOST) {
            return buildHostFeeDetail(cycle);
        }
        return buildMemberFeeDetail(cycle);
    }

    private PartySettingsResponse buildHostSettings(
            final Party party,
            final SubProduct subProduct,
            final PartyCycle cycle
    ) {
        final BankAccount settlementAccount = findSettlementAccount(party.getHostUserId());
        final long membersShareAmount =
                (long) cycle.getMemberCountSnapshot() * (cycle.getPricePerMemberSnapshot() - FeePolicy.MEMBER_FEE);
        final long monthlySettlementAmount = membersShareAmount - FeePolicy.HOST_FEE;

        return new PartySettingsResponse(
                "HOST",
                subProduct.getServiceName(),
                party.getCreatedAt().toLocalDate(),
                cycle.getBillingDueAt().getDayOfMonth(),
                monthlySettlementAmount,
                settlementAccount != null ? settlementAccount.getBankName() : null,
                settlementAccount != null ? settlementAccount.getAccountNumMasked() : null,
                null,
                null
        );
    }

    private PartySettingsResponse buildMemberSettings(
            final Party party,
            final SubProduct subProduct,
            final PartyCycle cycle
    ) {
        return new PartySettingsResponse(
                "MEMBER",
                subProduct.getServiceName(),
                party.getCreatedAt().toLocalDate(),
                null,
                null,
                null,
                null,
                (long) cycle.getPricePerMemberSnapshot(),
                cycle.getBillingDueAt().getDayOfMonth()
        );
    }

    private PartyFeeDetailResponse buildHostFeeDetail(final PartyCycle cycle) {
        final long membersShareAmount =
                (long) cycle.getMemberCountSnapshot() * (cycle.getPricePerMemberSnapshot() - FeePolicy.MEMBER_FEE);
        final long monthlySettlementAmount = membersShareAmount - FeePolicy.HOST_FEE;

        return new PartyFeeDetailResponse(
                "HOST",
                cycle.getMemberCountSnapshot(),
                membersShareAmount,
                FeePolicy.HOST_FEE,
                monthlySettlementAmount,
                cycle.getBillingDueAt().plusMonths(1).toLocalDate(),
                true,
                null,
                null,
                null
        );
    }

    private PartyFeeDetailResponse buildMemberFeeDetail(final PartyCycle cycle) {
        final long monthlyPaymentAmount = cycle.getPricePerMemberSnapshot();
        final long ottUsageFee = monthlyPaymentAmount - FeePolicy.MEMBER_FEE;

        return new PartyFeeDetailResponse(
                "MEMBER",
                null,
                null,
                FeePolicy.MEMBER_FEE,
                null,
                null,
                null,
                monthlyPaymentAmount,
                ottUsageFee,
                cycle.getBillingDueAt().plusMonths(1).toLocalDate()
        );
    }

    private Party getParty(final Long partyId) {
        final Party party = partyMapper.findById(partyId);
        if (party == null) {
            throw new PartyException(ErrorCode.PARTY_NOT_FOUND);
        }
        return party;
    }

    private PartyMember getActiveMember(final Long userId, final Long partyId) {
        final PartyMember member = partyMemberMapper.findByPartyIdAndUserId(partyId, userId);
        if (member == null
                || member.getStatus() == PartyMemberStatus.LEFT
                || member.getStatus() == PartyMemberStatus.REMOVED) {
            throw new PartyException(ErrorCode.PARTY_LEAVE_FORBIDDEN);
        }
        return member;
    }

    private SubProduct getSubProduct(final String productId) {
        return subProductMapper.findById(productId)
                .orElseThrow(() -> new PartyException(ErrorCode.SUB_PRODUCT_NOT_FOUND));
    }

    private PartyCycle getRunningCycle(final Long partyId) {
        final PartyCycle cycle = partyCycleMapper.findLatestPendingOrRunningCycle(
                partyId, PartyCycleStatus.PAYMENT_PENDING, PartyCycleStatus.RUNNING
        );
        if (cycle == null) {
            throw new PartyException(ErrorCode.PAYMENT_CYCLE_NOT_FOUND);
        }
        return cycle;
    }

    private BankAccount findSettlementAccount(final Long hostUserId) {
        final List<BankAccount> accounts = bankMapper.findAllByUserId(hostUserId);
        return accounts.stream()
                .filter(a -> a.getAccountType() == AccountType.SETTLEMENT)
                .findFirst()
                .orElse(null);
    }
}
