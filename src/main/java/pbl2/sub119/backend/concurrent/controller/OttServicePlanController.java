package pbl2.sub119.backend.concurrent.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pbl2.sub119.backend.concurrent.controller.docs.OttServicePlanDocs;
import pbl2.sub119.backend.concurrent.dto.response.OttServicePlanResponse;

@RestController
@RequestMapping("/api/v1/ott-service-plans")
@RequiredArgsConstructor
public class OttServicePlanController implements OttServicePlanDocs {

    @GetMapping
    public ResponseEntity<List<OttServicePlanResponse>> getPlans() {
        return ResponseEntity.ok(List.of(
                new OttServicePlanResponse("NETFLIX", "스탠다드", 2, "FHD"),
                new OttServicePlanResponse("NETFLIX", "프리미엄", 4, "4K"),
                new OttServicePlanResponse("TVING", "베이직", 1, "HD"),
                new OttServicePlanResponse("TVING", "스탠다드", 2, "FHD"),
                new OttServicePlanResponse("TVING", "프리미엄", 4, "FHD"),
                new OttServicePlanResponse("WATCHA", "베이직", 1, "FHD"),
                new OttServicePlanResponse("WATCHA", "프리미엄", 4, "FHD"),
                new OttServicePlanResponse("DISNEY_PLUS", "스탠다드", 2, "FHD"),
                new OttServicePlanResponse("DISNEY_PLUS", "프리미엄", 4, "4K"),
                new OttServicePlanResponse("WAVVE", "베이직", 1, "HD"),
                new OttServicePlanResponse("WAVVE", "스탠다드", 2, "FHD"),
                new OttServicePlanResponse("WAVVE", "프리미엄", 4, "FHD")
        ));
    }
}
