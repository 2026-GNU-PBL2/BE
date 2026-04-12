package pbl2.sub119.backend.party.join.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.party.join.controller.docs.PartyJoinDocs;
import pbl2.sub119.backend.party.join.dto.request.PartyJoinApplyRequest;
import pbl2.sub119.backend.party.join.dto.request.PartyJoinPreviewRequest;
import pbl2.sub119.backend.party.join.dto.response.PartyJoinApplyResponse;
import pbl2.sub119.backend.party.join.dto.response.PartyJoinCancelResponse;
import pbl2.sub119.backend.party.join.dto.response.PartyJoinMeResponse;
import pbl2.sub119.backend.party.join.dto.response.PartyJoinPreviewResponse;
import pbl2.sub119.backend.party.join.service.PartyJoinCommandService;
import pbl2.sub119.backend.party.join.service.PartyJoinPreviewService;
import pbl2.sub119.backend.party.join.service.PartyJoinQueryService;

@RestController
@RequestMapping("/api/v1/party-join")
@RequiredArgsConstructor
public class PartyJoinController implements PartyJoinDocs {

    private final PartyJoinPreviewService partyJoinPreviewService;
    private final PartyJoinCommandService partyJoinCommandService;
    private final PartyJoinQueryService partyJoinQueryService;

    // 파티 참여 전 결제 안내 조회
    @Override
    public ResponseEntity<PartyJoinPreviewResponse> getJoinPreview(
            @Auth final Accessor accessor,
            @RequestBody @Valid final PartyJoinPreviewRequest request
    ) {
        return ResponseEntity.ok(
                partyJoinPreviewService.getJoinPreview(request.productId())
        );
    }

    // 파티 자동 매칭 신청
    @Override
    public ResponseEntity<PartyJoinApplyResponse> applyJoin(
            @Auth final Accessor accessor,
            @RequestBody @Valid final PartyJoinApplyRequest request
    ) {
        return ResponseEntity.ok(
                partyJoinCommandService.applyJoin(request.productId(), accessor.getUserId())
        );
    }

    // 내 자동 매칭 신청 상태 조회
    @Override
    public ResponseEntity<List<PartyJoinMeResponse>> getMyJoinRequests(
            @Auth final Accessor accessor
    ) {
        return ResponseEntity.ok(
                partyJoinQueryService.getMyJoinRequests(accessor.getUserId())
        );
    }

    // 자동 매칭 신청 취소
    @Override
    public ResponseEntity<PartyJoinCancelResponse> cancelJoin(
            @Auth final Accessor accessor,
            @PathVariable final Long joinRequestId
    ) {
        return ResponseEntity.ok(
                partyJoinCommandService.cancelJoin(joinRequestId, accessor.getUserId())
        );
    }
}