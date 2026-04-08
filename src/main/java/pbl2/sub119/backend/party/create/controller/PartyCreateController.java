package pbl2.sub119.backend.party.create.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.party.create.controller.docs.PartyCreateDocs;
import pbl2.sub119.backend.party.create.dto.request.PartyCreateRequest;
import pbl2.sub119.backend.party.create.dto.request.PartyCreateSummaryRequest;
import pbl2.sub119.backend.party.create.dto.response.PartyCreateResponse;
import pbl2.sub119.backend.party.create.dto.response.PartyCreateSummaryResponse;
import pbl2.sub119.backend.party.create.service.PartyCreateService;

@RestController
@RequestMapping("/api/v1/parties")
@RequiredArgsConstructor
public class PartyCreateController implements PartyCreateDocs {

    private final PartyCreateService partyCreateService;

    // 파티 생성 전에 화면에서 보여줄 요약 정보를 조회
    @Override
    public ResponseEntity<PartyCreateSummaryResponse> getCreateSummary(
            @Auth final Accessor accessor,
            @RequestBody @Valid final PartyCreateSummaryRequest request
    ) {
        return ResponseEntity.ok(
                partyCreateService.getCreateSummary(accessor.getUserId(), request)
        );
    }

    // 선택한 상품으로 실제 파티를 생성
    @Override
    public ResponseEntity<PartyCreateResponse> createParty(
            @Auth final Accessor accessor,
            @RequestBody @Valid final PartyCreateRequest request
    ) {
        return ResponseEntity.ok(
                partyCreateService.createParty(accessor.getUserId(), request)
        );
    }
}