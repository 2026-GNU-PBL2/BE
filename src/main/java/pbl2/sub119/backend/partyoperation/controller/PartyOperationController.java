package pbl2.sub119.backend.partyoperation.controller;

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
import pbl2.sub119.backend.partyoperation.controller.docs.PartyOperationDocs;
import pbl2.sub119.backend.partyoperation.dto.request.PartyOperationResetRequest;
import pbl2.sub119.backend.partyoperation.dto.request.PartyOperationSetupRequest;
import pbl2.sub119.backend.partyoperation.dto.response.PartyOperationConfirmResponse;
import pbl2.sub119.backend.partyoperation.dto.response.PartyOperationDashboardResponse;
import pbl2.sub119.backend.partyoperation.dto.response.PartyOperationMemberResponse;
import pbl2.sub119.backend.partyoperation.dto.response.PartyOperationSetupResponse;
import pbl2.sub119.backend.partyoperation.service.PartyOperationCommandService;
import pbl2.sub119.backend.partyoperation.service.PartyOperationQueryService;

@RestController
@RequestMapping("/api/v1/parties")
@RequiredArgsConstructor
public class PartyOperationController implements PartyOperationDocs {

    private final PartyOperationCommandService partyOperationCommandService;
    private final PartyOperationQueryService partyOperationQueryService;

    // 파티장이 운영 정보 최초 등록 및 수정
    @Override
    public ResponseEntity<PartyOperationSetupResponse> setupOperation(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId,
            @RequestBody @Valid final PartyOperationSetupRequest request
    ) {
        return ResponseEntity.ok(
                partyOperationCommandService.setupOperation(accessor.getUserId(), partyId, request)
        );
    }

    // 운영 전체 현황 조회
    @Override
    public ResponseEntity<PartyOperationDashboardResponse> getOperationDashboard(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId
    ) {
        return ResponseEntity.ok(
                partyOperationQueryService.getOperationDashboard(accessor.getUserId(), partyId)
        );
    }

    // 파티장이 파티 운영 멤버 상태 목록 조회
    @Override
    public ResponseEntity<List<PartyOperationMemberResponse>> getOperationMembers(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId
    ) {
        return ResponseEntity.ok(
                partyOperationQueryService.getOperationMembers(accessor.getUserId(), partyId)
        );
    }

    // 파티원이 운영 완료를 확인하면 멤버 상태 및 파티 전체 운영 상태 갱신
    @Override
    public ResponseEntity<PartyOperationConfirmResponse> confirmOperation(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId
    ) {
        return ResponseEntity.ok(
                partyOperationCommandService.confirmOperation(accessor.getUserId(), partyId)
        );
    }

    // 파티장이 운영 정보를 재설정 상태로 변경
    @Override
    public ResponseEntity<Void> resetOperation(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId,
            @RequestBody @Valid final PartyOperationResetRequest request
    ) {
        partyOperationCommandService.resetOperation(accessor.getUserId(), partyId, request);
        return ResponseEntity.ok().build();
    }
}