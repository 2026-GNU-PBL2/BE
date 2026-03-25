<<<<<<< HEAD
package pbl2.sub119.backend.subproduct.entity;
=======
package pbl2.submate.backend.subproduct.entity;
>>>>>>> 2ee923e (fix: 소프트 삭제 및 재가입 로직 개선, merge 충돌 해결)

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SubProduct {

    private String id;
    private String serviceName;
    private String description;
    private String thumbnailUrl;

    // INVITE_CODE | ACCOUNT_SHARE
    private String operationType;

    private Integer maxMemberCount;
    private Long basePrice;        // 서비스 전체 구독료
    private Long pricePerMember;   // 파티원 1인당 결제 금액
    private String status;         // ACTIVE 고정

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}