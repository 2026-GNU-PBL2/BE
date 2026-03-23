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
import pbl2.sub119.backend.subproduct.dto.SubProductResponse;

import java.util.List;

@Tag(
        name = "SubProduct API",
        description = "구독 상품 조회 API"
)
public interface SubProductDocs {

    @Operation(
            summary = "구독 상품 전체 목록 조회",
            description = "등록된 구독 상품 전체 목록을 조회합니다. 상품은 ACTIVE 상태만 존재합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = SubProductResponse.class))
                    )
            }
    )
    @GetMapping
    ResponseEntity<List<SubProductResponse>> getProducts(
            @Parameter(hidden = true) @Auth Accessor accessor
    );

    @Operation(
            summary = "구독 상품 단건 조회",
            description = "상품 ID로 구독 상품 단건을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = SubProductResponse.class))
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
}