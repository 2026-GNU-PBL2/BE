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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;
import pbl2.sub119.backend.subproduct.dto.SubProductRequest;
import pbl2.sub119.backend.subproduct.dto.SubProductResponse;
import pbl2.sub119.backend.subproduct.dto.SubProductUpdateRequest;

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
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = SubProductResponse.class))),
                    @ApiResponse(responseCode = "403", description = "어드민 권한 없음", content = @Content)
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
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = SubProductResponse.class)))),
                    @ApiResponse(responseCode = "403", description = "어드민 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 상품", content = @Content)
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
            구독 상품을 등록합니다. (multipart/form-data)
            - `data` part: JSON (SubProductRequest) — operationType은 등록 후 변경 불가 (INVITE_CODE / ACCOUNT_SHARE)
            - `image` part: 상품 이미지 파일 (image/*, 최대 5MB, 선택)
            - image 미첨부 시 data.thumbnailUrl 값 사용
            - category: NETFLIX / TVING / WATCHA / DISNEY_PLUS / APPLE_TV / WAVVE / LAFTEL
            """,
            responses = {
                    @ApiResponse(responseCode = "201", description = "등록 성공",
                            content = @Content(schema = @Schema(implementation = SubProductResponse.class))),
                    @ApiResponse(responseCode = "400", description = "중복된 서비스명 / 유효성 검사 실패 / 잘못된 파일", content = @Content),
                    @ApiResponse(responseCode = "403", description = "어드민 권한 없음", content = @Content)
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<SubProductResponse> createProduct(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @Parameter(description = "상품 등록 정보 (JSON part)", required = true, schema = @Schema(implementation = SubProductRequest.class))
            @RequestPart("data") @Valid SubProductRequest request,
            @Parameter(description = "상품 이미지 (선택, image/*, 5MB 이하)")
            @RequestPart(value = "image", required = false) MultipartFile image
    );

    @Operation(
            summary = "[어드민] 구독 상품 수정",
            description = """
            구독 상품 정보를 수정합니다. (multipart/form-data)
            - `data` part: JSON (SubProductUpdateRequest)
            - `image` part: 상품 이미지 파일 (image/*, 최대 5MB, 선택)
            - image 첨부 시 새 URL로 교체 / 미첨부 시 data.thumbnailUrl → 기존 URL 순으로 유지
            - operationType 변경 불가
            """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공",
                            content = @Content(schema = @Schema(implementation = SubProductResponse.class))),
                    @ApiResponse(responseCode = "400", description = "중복된 서비스명 / 유효성 검사 실패 / 잘못된 파일", content = @Content),
                    @ApiResponse(responseCode = "403", description = "어드민 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 상품", content = @Content)
            }
    )
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<SubProductResponse> updateProduct(
            @Parameter(hidden = true) @Auth Accessor accessor,
            @Parameter(description = "상품 ID", required = true) @PathVariable String id,
            @Parameter(description = "상품 수정 정보 (JSON part)", required = true, schema = @Schema(implementation = SubProductUpdateRequest.class))
            @RequestPart("data") @Valid SubProductUpdateRequest request,
            @Parameter(description = "상품 이미지 (선택, image/*, 5MB 이하)")
            @RequestPart(value = "image", required = false) MultipartFile image
    );
}
