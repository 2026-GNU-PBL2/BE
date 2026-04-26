package pbl2.sub119.backend.party.create.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.common.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.common.error.ErrorCode;
import pbl2.sub119.backend.party.common.entity.Party;
import pbl2.sub119.backend.party.common.entity.PartyHistory;
import pbl2.sub119.backend.party.common.entity.PartyMember;
import pbl2.sub119.backend.party.common.enumerated.OperationStatus;
import pbl2.sub119.backend.party.common.enumerated.PartyHistoryEventType;
import pbl2.sub119.backend.party.common.enumerated.PartyRole;
import pbl2.sub119.backend.party.common.enumerated.RecruitStatus;
import pbl2.sub119.backend.party.common.enumerated.VacancyType;
import pbl2.sub119.backend.party.common.exception.PartyException;
import pbl2.sub119.backend.party.common.mapper.PartyHistoryMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMapper;
import pbl2.sub119.backend.party.common.mapper.PartyMemberMapper;
import pbl2.sub119.backend.party.create.dto.request.PartyCreateRequest;
import pbl2.sub119.backend.party.create.dto.request.PartyCreateSummaryRequest;
import pbl2.sub119.backend.party.create.dto.response.PartyCreateResponse;
import pbl2.sub119.backend.party.create.dto.response.PartyCreateSummaryResponse;
import pbl2.sub119.backend.payment.policy.FeePolicy;
import pbl2.sub119.backend.subproduct.dto.SubProductResponse;
import pbl2.sub119.backend.subproduct.service.SubProductService;

@Service
@RequiredArgsConstructor
public class PartyCreateService {

    private final PartyMapper partyMapper;
    private final PartyMemberMapper partyMemberMapper;
    private final PartyHistoryMapper partyHistoryMapper;
    private final SubProductService subProductService;
    private final ObjectMapper objectMapper;

    // 파티 생성 전에 화면에서 보여줄 예상 금액과 안내 문구를 계산
    @Transactional(readOnly = true)
    public PartyCreateSummaryResponse getCreateSummary(
            final Long userId,
            final PartyCreateSummaryRequest request
    ) {
        validateSummaryRequest(request);

        final SubProductResponse product = subProductService.getProduct(request.productId());
        validateProductPricing(product);
        validateCapacity(request.capacity(), product.getMaxMemberCount());

        final int totalCapacity = request.capacity(); // 파티 총 인원 (파티장 포함)
        final int recruitMemberCount = Math.max(0, totalCapacity - 1); // 실제 모집할 파티원 수 (파티장 제외)

        final long ottBasePrice = product.getBasePrice(); // OTT 전체 구독 요금
        final long memberPayAmount = product.getPricePerMember(); // 파티원 1명이 부담하는 구독료 (수수료 제외 기준)
        final long memberTotalAmount = memberPayAmount * recruitMemberCount; // 파티원들이 총 부담하는 금액 (파티원 수 * 1인 금액)

        // 파티장이 실제로 부담하는 금액 (전체 구독료 - 파티원들이 낸 금액)
        final long hostPayAmount = Math.max(0L, ottBasePrice - memberTotalAmount);

        // 정산 금액 = 파티원 총 납부액 - 파티장 수수료(490원)
        // 파티원 수수료(990)는 플랫폼이 가져가고 파티장 할인(500) 적용 후 파티장에게 공제되는 금액
        final long expectedSettlementAmount =
                Math.max(0L, memberTotalAmount - FeePolicy.HOST_FEE);

        return new PartyCreateSummaryResponse(
                product.getId(),
                product.getServiceName(),
                product.getThumbnailUrl(),
                product.getOperationType(),
                product.getMaxMemberCount(),
                totalCapacity,
                recruitMemberCount,
                ottBasePrice,
                memberPayAmount,
                memberTotalAmount,
                hostPayAmount,
                FeePolicy.MEMBER_FEE,
                FeePolicy.MEMBER_FEE - FeePolicy.HOST_FEE,
                expectedSettlementAmount,
                "파티 매칭이 완료되면 첫 결제일과 정산일이 확정됩니다.",
                "정산 금액은 실제 매칭 완료 인원과 결제 완료 여부에 따라 달라질 수 있습니다."
        );
    }

    /**
     * 선택한 상품으로 새 파티를 생성한다.
     * 생성자는 파티장(HOST)으로 등록되고 첫 멤버로 함께 저장된다.
     */
    @Transactional
    public PartyCreateResponse createParty(
            final Long hostUserId,
            final PartyCreateRequest request
    ) {
        validateCreateRequest(request);

        final SubProductResponse product = subProductService.getProduct(request.productId());
        validateCapacity(request.capacity(), product.getMaxMemberCount());

        final LocalDateTime now = LocalDateTime.now();

        // 선택한 상품으로 새 파티 생성
        final Party party = Party.builder()
                .productId(product.getId())
                .hostUserId(hostUserId)
                .capacity(request.capacity())
                .currentMemberCount(1)
                .recruitStatus(RecruitStatus.RECRUITING)
                .operationStatus(OperationStatus.WAITING_START)
                .vacancyType(VacancyType.NONE)
                .pricePerMemberSnapshot(Math.toIntExact(product.getPricePerMember()))
                .createdAt(now)
                .updatedAt(now)
                .terminatedAt(null)
                .build();

        partyMapper.insertParty(party);

        // 파티 생성자는 파티장(HOST)로 저장
        final PartyMember hostMember = PartyMember.builder()
                .partyId(party.getId())
                .userId(hostUserId)
                .role(PartyRole.HOST)
                .status(PartyMemberStatus.ACTIVE)
                .joinedAt(now)
                .activatedAt(now)
                .serviceStartAt(null)
                .serviceEndAt(null)
                .leaveReservedAt(null)
                .leftAt(null)
                .replacedTargetMemberId(null)
                .build();

        partyMemberMapper.insertPartyMember(hostMember);

        final PartyHistory history = PartyHistory.builder()
                .partyId(party.getId())
                .memberId(hostMember.getId())
                .eventType(PartyHistoryEventType.PARTY_CREATED)
                .eventPayload(createPartyCreatedPayload(hostUserId, product.getId(), request.capacity()))
                .createdAt(now)
                .createdBy(hostUserId)
                .build();

        partyHistoryMapper.insertHistory(history);

        return new PartyCreateResponse(
                party.getId(),
                party.getProductId(),
                product.getServiceName(),
                party.getHostUserId(),
                party.getCapacity(),
                party.getCurrentMemberCount(),
                party.getRecruitStatus(),
                party.getOperationStatus(),
                party.getVacancyType(),
                party.getPricePerMemberSnapshot(),
                party.getCreatedAt(),
                "SETUP_PROVISION"
        );
    }

    private String createPartyCreatedPayload(
            final Long hostUserId,
            final String productId,
            final Integer capacity
    ) {
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("hostUserId", hostUserId);
        payload.put("productId", productId);
        payload.put("capacity", capacity);

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("파티 생성 이력 payload 직렬화에 실패했습니다.", e);
        }
    }

    // 생성 전 요약 조회 요청 최소 입력값 검증
    private void validateSummaryRequest(final PartyCreateSummaryRequest request) {
        if (request.productId() == null || request.productId().isBlank()) {
            throw new PartyException(ErrorCode.PARTY_INVALID_PRODUCT_ID);
        }

        if (request.capacity() == null || request.capacity() < 2) {
            throw new PartyException(ErrorCode.PARTY_INVALID_CAPACITY);
        }
    }

    // 파티 생성 요청의 최소 입력값 검증
    private void validateCreateRequest(final PartyCreateRequest request) {
        if (request.productId() == null || request.productId().isBlank()) {
            throw new PartyException(ErrorCode.PARTY_INVALID_PRODUCT_ID);
        }

        if (request.capacity() == null || request.capacity() < 2) {
            throw new PartyException(ErrorCode.PARTY_INVALID_CAPACITY);
        }
    }

    // 선택한 인원이 상품의 최대 허용 인원을 넘지 않는지 확인
    private void validateCapacity(final Integer capacity, final Integer maxMemberCount) {
        if (maxMemberCount != null && capacity > maxMemberCount) {
            throw new PartyException(ErrorCode.PARTY_INVALID_CAPACITY);
        }
    }

    private void validateProductPricing(final SubProductResponse product) {
        final Long pricePerMember = product.getPricePerMember();
        if (pricePerMember != null && pricePerMember > Integer.MAX_VALUE) {
            throw new IllegalStateException("pricePerMember가 저장 가능한 범위를 초과했습니다.");
        }
    }
}