package pbl2.sub119.backend.party.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.party.controller.docs.PartyLeaveDocs;
import pbl2.sub119.backend.party.dto.response.PartyLeaveReservationMemberResponse;
import pbl2.sub119.backend.party.dto.response.PartyLeaveReserveResponse;
import pbl2.sub119.backend.party.service.PartyLeaveService;

// 파티원 탈퇴 예약
@RestController
@RequestMapping("/api/v1/party-leave")
@RequiredArgsConstructor
public class PartyLeaveController implements PartyLeaveDocs {

    private final PartyLeaveService partyLeaveService;

    // 파티원 탈퇴 예약
    @Override
    public ResponseEntity<PartyLeaveReserveResponse> reserveLeave(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId
    ) {
        return ResponseEntity.ok(
                partyLeaveService.reserveLeave(partyId, accessor.getUserId())
        );
    }

    // 탈퇴 예약 취소
    @Override
    public ResponseEntity<Void> cancelLeave(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId
    ) {
        partyLeaveService.cancelLeave(partyId, accessor.getUserId());
        return ResponseEntity.ok().build();
    }

    // 파티장이 탈퇴 예정 멤버 목록 조회
    @Override
    public ResponseEntity<List<PartyLeaveReservationMemberResponse>> getLeaveReservations(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId
    ) {
        return ResponseEntity.ok(
                partyLeaveService.getLeaveReservations(partyId, accessor.getUserId())
        );
    }
}