package pbl2.sub119.backend.party.join.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.payment.policy.FeePolicy;
import pbl2.sub119.backend.subproduct.dto.SubProductResponse;
import pbl2.sub119.backend.subproduct.service.SubProductService;
import pbl2.sub119.backend.party.join.dto.response.PartyJoinPreviewResponse;

@Service
@RequiredArgsConstructor
public class PartyJoinPreviewService {

    private static final long DEPOSIT_AMOUNT = 0L;

    private final SubProductService subProductService;

    // 파티 참여 전 결제 안내 계산
    @Transactional(readOnly = true)
    public PartyJoinPreviewResponse getJoinPreview(final String productId) {
        final SubProductResponse product = subProductService.getProduct(productId);

        final long productPricePerMember = product.getPricePerMember();
        final long firstPaymentAmount = productPricePerMember + FeePolicy.MEMBER_FEE + DEPOSIT_AMOUNT;
        final long recurringPaymentAmount = productPricePerMember + FeePolicy.MEMBER_FEE;

        return new PartyJoinPreviewResponse(
                product.getId(),
                product.getServiceName(),
                product.getThumbnailUrl(),
                productPricePerMember,
                FeePolicy.MEMBER_FEE,
                DEPOSIT_AMOUNT,
                firstPaymentAmount,
                recurringPaymentAmount,
                "매칭 완료 시 첫 결제가 진행되며, 다음 정산일부터 동일 금액이 자동 결제됩니다."
        );
    }
}