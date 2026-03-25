
package pbl2.sub119.backend.subproduct.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubProductUpdateRequest {

    @NotBlank(message = "서비스명은 필수입니다.")
    private String serviceName;

    private String description;
    private String thumbnailUrl;

    @NotNull(message = "최대 인원은 필수입니다.")
    @Positive(message = "최대 인원은 1 이상이어야 합니다.")
    private Integer maxMemberCount;

    @NotNull(message = "전체 구독료는 필수입니다.")
    @Positive(message = "전체 구독료는 0보다 커야 합니다.")
    private Long basePrice;

    @NotNull(message = "1인당 결제 금액은 필수입니다.")
    @Positive(message = "1인당 결제 금액은 0보다 커야 합니다.")
    private Long pricePerMember;
}