package pbl2.sub119.backend.party.provision.controller;

import jakarta.servlet.http.HttpServletRequest;
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
import pbl2.sub119.backend.concurrent.service.DeviceCollectionHelper;
import pbl2.sub119.backend.party.provision.controller.docs.PartyProvisionDocs;
import pbl2.sub119.backend.party.provision.dto.request.PartyProvisionResetRequest;
import pbl2.sub119.backend.party.provision.dto.request.PartyProvisionSetupRequest;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionConfirmResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionDashboardResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionMeResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionMemberResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionPasswordRevealResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyProvisionSetupResponse;
import pbl2.sub119.backend.party.provision.dto.response.PartyRecruitStatusResponse;
import pbl2.sub119.backend.party.provision.service.PartyProvisionCommandService;
import pbl2.sub119.backend.party.provision.service.PartyProvisionQueryService;
import pbl2.sub119.backend.party.provision.service.PartyRecruitStatusQueryService;

@RestController
@RequestMapping("/api/v1/parties")
@RequiredArgsConstructor
public class PartyProvisionController implements PartyProvisionDocs {

    private final PartyProvisionCommandService partyProvisionCommandService;
    private final PartyProvisionQueryService partyProvisionQueryService;
    private final PartyRecruitStatusQueryService partyRecruitStatusQueryService;
    private final DeviceCollectionHelper deviceCollectionHelper;
    private final HttpServletRequest httpServletRequest;

    // 파티 모집 완료 여부 조회
    @Override
    public ResponseEntity<PartyRecruitStatusResponse> getRecruitStatus(
            @PathVariable final Long partyId
    ) {
        return ResponseEntity.ok(
                partyRecruitStatusQueryService.getRecruitStatus(partyId)
        );
    }

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

    // 파티장이 이용 대상 멤버 상태 목록 조회
    @Override
    public ResponseEntity<List<PartyProvisionMemberResponse>> getProvisionMembers(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId
    ) {
        return ResponseEntity.ok(
                partyProvisionQueryService.getProvisionMembers(accessor.getUserId(), partyId)
        );
    }

    // 파티원이 이용 완료 확인 — 이 시점에 기기 정보 수집 (해당 기기로 서비스를 이용했음을 명시적으로 확인)
    @Override
    public ResponseEntity<PartyProvisionConfirmResponse> confirmProvision(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId
    ) {
        deviceCollectionHelper.collectSilently(accessor.getUserId(), partyId, httpServletRequest);
        return ResponseEntity.ok(
                partyProvisionCommandService.confirmProvision(accessor.getUserId(), partyId)
        );
    }

    // 파티장이 이용 재설정
    @Override
    public ResponseEntity<Void> resetProvision(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId,
            @RequestBody @Valid final PartyProvisionResetRequest request
    ) {
        partyProvisionCommandService.resetProvision(accessor.getUserId(), partyId, request);
        return ResponseEntity.ok().build();
    }

    // 본인에게 필요한 이용 정보 조회 — 이 시점에 기기 정보 수집 (계정 정보를 이 기기에서 조회 = OTT 접속 전 시점)
    @Override
    public ResponseEntity<PartyProvisionMeResponse> getMyProvisionInfo(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId
    ) {
        deviceCollectionHelper.collectSilently(accessor.getUserId(), partyId, httpServletRequest);
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