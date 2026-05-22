package pbl2.sub119.backend.concurrent.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OttServicePlanResponse {

    private String serviceName;
    private String planName;
    private int concurrentLimit;
}
