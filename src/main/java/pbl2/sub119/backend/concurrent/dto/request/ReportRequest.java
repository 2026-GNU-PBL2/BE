package pbl2.sub119.backend.concurrent.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportRequest {

    @NotBlank
    private String reportType;
}
