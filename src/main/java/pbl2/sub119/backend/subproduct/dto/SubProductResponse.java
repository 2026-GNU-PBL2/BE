package pbl2.sub119.backend.subproduct.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pbl2.sub119.backend.subproduct.entity.SubProduct;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubProductResponse {
    private String id;
    private String serviceName;
    private String description;
    private String thumbnailUrl;
    private String operationType;
    private Integer maxMemberCount;
    private Long basePrice;
    private Long pricePerMember;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SubProductResponse from(SubProduct entity) {
        return SubProductResponse.builder()
                .id(entity.getId())
                .serviceName(entity.getServiceName())
                .description(entity.getDescription())
                .thumbnailUrl(entity.getThumbnailUrl())
                .operationType(entity.getOperationType())
                .maxMemberCount(entity.getMaxMemberCount())
                .basePrice(entity.getBasePrice())
                .pricePerMember(entity.getPricePerMember())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
