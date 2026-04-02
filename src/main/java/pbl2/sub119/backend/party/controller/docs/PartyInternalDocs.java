package pbl2.sub119.backend.party.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import pbl2.sub119.backend.party.dto.request.PartyCycleStartEventRequest;

@Tag(name = "Party Internal API", description = "내부 연동 API")
public interface PartyInternalDocs {

    @Operation(
            summary = "이용 주기 시작 이벤트 수신",
            description = "Subscription Cycle Service에서 전달하는 다음 주기 시작 이벤트를 수신합니다."
    )
    @PostMapping("/cycle-start")
    ResponseEntity<Void> handleCycleStart(
            @RequestBody PartyCycleStartEventRequest request
    );
}