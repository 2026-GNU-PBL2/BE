package pbl2.sub119.backend.subproduct.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.subproduct.dto.SubProductRequest;
import pbl2.sub119.backend.subproduct.dto.SubProductResponse;

import java.util.List;

@Tag(
        name = "SubProduct Admin API",
        description = "구독 상품 관리 API (어드민 전용)"
)
public interface SubProductAdminDocs {

    @Operation(
            summary = "[어드민] 구독 상품 전체 목록 조회",
            description = "등록된 구독 상품 전체 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = SubProductResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "어드민 권한 없음",
                            content = @Content
                    )
            }
    )
    @GetMapping
    ResponseEntity<List<SubProductResponse>> getProducts(
            @Parameter(hidden = true) @Auth Accessor accessor
    );

    @Operation(
            summary = "[어드민] 구독 상품 단건 조회",
            description = "상품 ID로 구독 상품 단건을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = SubProductResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "어드민 권한 없음",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "존재하지 않는 상품",
                            content = @Content
                    )
            }
    )
    @GetMapping("/{id}")
    ResponseEntity<SubProductResponse> getProduct(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @Parameter(description = "상품 ID", required = true) @PathVariable String id
    );

    @Operation(
            summary = "[어드민] 구독 상품 등록",
            description = """
            구독 상품을 등록합니다.
            - operationType 등록 후 변경 불가 (INVITE_CODE / ACCOUNT_SHARE)
            - serviceName 중복 불가
            - basePrice: 서비스 전체 구독료
            - pricePerMember: 파티원 1인당 결제 금액 (어드민 직접 입력)
            """,
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "등록 성공",
                            content = @Content(schema = @Schema(implementation = SubProductResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "중복된 서비스명 또는 유효성 검사 실패",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "어드민 권한 없음",
                            content = @Content
                    )
            }
    )
    @PostMapping
    ResponseEntity<SubProductResponse> createProduct(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @Parameter(description = "상품 등록 정보", required = true)
            @RequestBody SubProductRequest request
    );

    @Operation(
            summary = "[어드민] 구독 상품 수정",
            description = """
            구독 상품 정보를 수정합니다.
            - operationType 변경 불가, 등록 시 확정된 값 유지
            - basePrice / pricePerMember 변경 시 기존 파티에는 영향 없음 (스냅샷 기준)
            """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "수정 성공",
                            content = @Content(schema = @Schema(implementation = SubProductResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "중복된 서비스명 또는 유효성 검사 실패",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "어드민 권한 없음",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "존재하지 않는 상품",
                            content = @Content
                    )
            }
    )
    @PutMapping("/{id}")
    ResponseEntity<SubProductResponse> updateProduct(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @Parameter(description = "상품 ID", required = true) @PathVariable String id,
            @Parameter(description = "상품 수정 정보", required = true)
            @RequestBody SubProductRequest request
    );
}