package pbl2.sub119.backend.subproduct.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.subproduct.dto.SubProductRequest;
import pbl2.sub119.backend.subproduct.dto.SubProductResponse;
import pbl2.sub119.backend.subproduct.dto.SubProductUpdateRequest;

import java.util.List;

@Tag(
        name = "SubProduct Admin API",
        description = "구독 상품 관리자 API"
)
public interface SubProductAdminDocs {

    @Operation(
            summary = "[관리자] 구독 상품 목록 조회",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = SubProductResponse.class)))
                    ),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content)
            }
    )
    @GetMapping
    ResponseEntity<List<SubProductResponse>> getProducts(
            @Parameter(hidden = true) @Auth Accessor accessor
    );

    @Operation(
            summary = "[관리자] 구독 상품 단건 조회",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = SubProductResponse.class))
                    ),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "상품 없음", content = @Content)
            }
    )
    @GetMapping("/{id}")
    ResponseEntity<SubProductResponse> getProduct(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @Parameter(description = "상품 ID", required = true) @PathVariable String id
    );

    @Operation(
            summary = "[관리자] 구독 상품 등록",
            description = "multipart/form-data: data(JSON) + image(File, optional)",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "등록 성공",
                            content = @Content(schema = @Schema(implementation = SubProductResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "검증 실패/중복명/파일 오류", content = @Content),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content)
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<SubProductResponse> createProduct(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @Parameter(description = "상품 등록 정보(JSON part)", required = true, schema = @Schema(implementation = SubProductRequest.class))
            @RequestPart("data") @Valid SubProductRequest request,
            @Parameter(description = "상품 이미지(File part, optional)")
            @RequestPart(value = "image", required = false) MultipartFile image
    );

    @Operation(
            summary = "[관리자] 구독 상품 수정",
            description = "multipart/form-data: data(JSON) + image(File, optional)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "수정 성공",
                            content = @Content(schema = @Schema(implementation = SubProductResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "검증 실패/중복명/파일 오류", content = @Content),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "상품 없음", content = @Content)
            }
    )
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<SubProductResponse> updateProduct(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @Parameter(description = "상품 ID", required = true) @PathVariable String id,
            @Parameter(description = "상품 수정 정보(JSON part)", required = true, schema = @Schema(implementation = SubProductUpdateRequest.class))
            @RequestPart("data") @Valid SubProductUpdateRequest request,
            @Parameter(description = "상품 이미지(File part, optional)")
            @RequestPart(value = "image", required = false) MultipartFile image
    );
}
