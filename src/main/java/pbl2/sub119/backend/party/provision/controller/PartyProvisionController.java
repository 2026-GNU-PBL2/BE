package pbl2.sub119.backend.party.provision.controller;

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
import pbl2.sub119.backend.party.provision.controller.docs.PartyProvisionDocs;
import pbl2.sub119.backend.party.provision.dto.request.PartyProvisionResetRequest;
import pbl2.sub119.backend.party.provision.dto.request.PartyProvisionSetupRequest;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionConfirmResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionDashboardResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionMeResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionMemberResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionPasswordRevealResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionSetupResponse;
import pbl2.sub119.backend.party.provision.service.PartyProvisionCommandService;
import pbl2.sub119.backend.party.provision.service.PartyProvisionQueryService;

@RestController
@RequestMapping("/api/v1/parties")
@RequiredArgsConstructor
public class PartyProvisionController implements PartyProvisionDocs {

    private final PartyProvisionCommandService partyProvisionCommandService;
    private final PartyProvisionQueryService partyProvisionQueryService;

    // 파티장이 provision 정보를 최초 등록하거나 다시 저장
    @Override
    public ResponseEntity<PartyProvisionSetupResponse> setupProvision(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId,
            @RequestBody @Valid final PartyProvisionSetupRequest request
    ) {
        return ResponseEntity.ok(
                partyProvisionCommandService.setupProvision(accessor.getUserId(), partyId, request)
        );
    }

    // 파티 provision 전체 현황 조회
    @Override
    public ResponseEntity<PartyProvisionDashboardResponse> getProvisionDashboard(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId
    ) {
        return ResponseEntity.ok(
                partyProvisionQueryService.getProvisionDashboard(accessor.getUserId(), partyId)
        );
    }

    // 파티장이 provision 대상 멤버 상태 목록 조회
    @Override
    public ResponseEntity<List<PartyProvisionMemberResponse>> getProvisionMembers(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId
    ) {
        return ResponseEntity.ok(
                partyProvisionQueryService.getProvisionMembers(accessor.getUserId(), partyId)
        );
    }

    // 파티원이 provision 완료 확인
    @Override
    public ResponseEntity<PartyProvisionConfirmResponse> confirmProvision(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId
    ) {
        return ResponseEntity.ok(
                partyProvisionCommandService.confirmProvision(accessor.getUserId(), partyId)
        );
    }

    // 파티장이 provision 재설정
    @Override
    public ResponseEntity<Void> resetProvision(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId,
            @RequestBody @Valid final PartyProvisionResetRequest request
    ) {
        partyProvisionCommandService.resetProvision(accessor.getUserId(), partyId, request);
        return ResponseEntity.ok().build();
    }

    // 본인에게 필요한 provision 정보 조회
    @Override
    public ResponseEntity<PartyProvisionMeResponse> getMyProvisionInfo(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId
    ) {
        return ResponseEntity.ok(
                partyProvisionQueryService.getMyProvisionInfo(accessor.getUserId(), partyId)
        );
    }

    // 보기 버튼 클릭 시 평문 비밀번호 조회
    @Override
    public ResponseEntity<PartyProvisionPasswordRevealResponse> getMyProvisionPassword(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId
    ) {
        return ResponseEntity.ok(
                partyProvisionQueryService.getMyProvisionPassword(accessor.getUserId(), partyId)
        );
    }
}