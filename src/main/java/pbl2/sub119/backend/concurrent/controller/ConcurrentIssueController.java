package pbl2.sub119.backend.concurrent.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.concurrent.dto.request.LeaderActionRequest;
import pbl2.sub119.backend.concurrent.dto.request.ReportRequest;
import pbl2.sub119.backend.concurrent.controller.docs.ConcurrentDocs;
import pbl2.sub119.backend.concurrent.dto.response.IncidentHistoryResponse;
import pbl2.sub119.backend.concurrent.dto.response.IncidentResult;
import pbl2.sub119.backend.concurrent.dto.response.ResolveResult;
import pbl2.sub119.backend.concurrent.mapper.ConcurrentIncidentMapper;
import pbl2.sub119.backend.concurrent.service.IncidentService;

@RestController
@RequestMapping("/api/v1/concurrent-issues")
@RequiredArgsConstructor
public class ConcurrentIssueController implements ConcurrentDocs.Issue {

    private final IncidentService incidentService;
    private final ConcurrentIncidentMapper incidentMapper;

    // 동시접속 위반 신고
    @PostMapping("/{partyId}")
    public ResponseEntity<IncidentResult> report(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId,
            @RequestBody @Valid final ReportRequest request
    ) {
        return ResponseEntity.ok(
                incidentService.processReport(partyId, accessor.getUserId(), request.getReportType())
        );
    }

    // 파티장 조치 완료 처리
    @PostMapping("/{partyId}/resolve")
    public ResponseEntity<ResolveResult> resolve(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId,
            @RequestBody @Valid final LeaderActionRequest request
    ) {
        return ResponseEntity.ok(
                incidentService.resolveIncident(request.getIncidentId(), accessor.getUserId(), partyId)
        );
    }

    // 파티 인시던트 이력 조회
    @GetMapping("/{partyId}/history")
    public ResponseEntity<List<IncidentHistoryResponse>> getHistory(
            @Auth final Accessor accessor,
            @PathVariable final Long partyId
    ) {
        return ResponseEntity.ok(
                IncidentHistoryResponse.fromList(incidentMapper.findByPartyId(partyId))
        );
    }
}
