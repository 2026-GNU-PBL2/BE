package pbl2.sub119.backend.concurrent.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.concurrent.controller.docs.ConcurrentDocs;
import pbl2.sub119.backend.concurrent.dto.response.OttServicePlanResponse;

@RestController
@RequestMapping("/api/v1/ott-service-plans")
@RequiredArgsConstructor
public class OttServicePlanController implements ConcurrentDocs.OttServicePlan {

    @GetMapping
    public ResponseEntity<List<OttServicePlanResponse>> getPlans() {
        return ResponseEntity.ok(List.of(
                new OttServicePlanResponse("NETFLIX", "프리미엄(4K)", 4),
                new OttServicePlanResponse("TVING", "프리미엄(FHD)", 4),
                new OttServicePlanResponse("WATCHA", "프리미엄(FHD)", 4),
                new OttServicePlanResponse("DISNEY_PLUS", "프리미엄(4K)", 4),
                new OttServicePlanResponse("APPLE_TV", "Apple TV+", 6),
                new OttServicePlanResponse("WAVVE", "프리미엄(FHD)", 4),
                new OttServicePlanResponse("LAFTEL", "프리미엄", 4)
        ));
    }
}
