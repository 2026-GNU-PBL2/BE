package pbl2.sub119.backend.admin.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pbl2.sub119.backend.admin.dashboard.dto.AdminDashboardResponse;
import pbl2.sub119.backend.admin.party.dto.AdminPartyDetailResponse;
import pbl2.sub119.backend.admin.party.dto.AdminPartyResponse;
import pbl2.sub119.backend.admin.user.dto.AdminUserDetailResponse;
import pbl2.sub119.backend.admin.user.dto.AdminUserResponse;
import pbl2.sub119.backend.auth.aop.Auth;
import pbl2.sub119.backend.auth.entity.Accessor;

import java.util.List;

public interface AdminDocs {

    @Tag(
            name = "Admin API",
            description = "관리자 전용 API"
    )
    interface Check {

        @Operation(
                summary = "관리자 권한 확인",
                description = """
                        현재 로그인한 사용자가 관리자(ADMIN)인지 확인하는 API입니다.

                        이 API는 아래 화면에서 사용합니다.
                        - 관리자 페이지 진입 전 권한 확인
                        - 관리자 로그인 성공 후 대시보드 이동 전 검증

                        처리 기준:
                        - ADMIN 권한 사용자는 접근할 수 있습니다.
                        - 일반 사용자는 접근할 수 없습니다.
                        - 토큰이 없거나 만료된 경우 접근할 수 없습니다.
                        """,
                responses = {
                        @ApiResponse(
                                responseCode = "200",
                                description = "관리자 접근 성공",
                                content = @Content(schema = @Schema(implementation = String.class))
                        ),
                        @ApiResponse(
                                responseCode = "401",
                                description = "토큰 없음 또는 유효하지 않은 토큰"
                        ),
                        @ApiResponse(
                                responseCode = "403",
                                description = "관리자 권한 없음"
                        )
                }
        )
        @GetMapping("/check")
        ResponseEntity<String> checkAdmin(
                @Parameter(hidden = true) @Auth Accessor accessor
        );
    }

    @Tag(
            name = "Admin API",
            description = "관리자 전용 API"
    )
    interface Dashboard {

        @Operation(
                summary = "관리자 대시보드 조회",
                description = """
                        관리자 대시보드 화면에 필요한 운영 현황 데이터를 조회합니다.

                        이 API는 아래 화면에서 사용합니다.
                        - 관리자 운영 대시보드

                        포함 데이터:
                        - operatingProductCount : 운영 중 상품 수
                        - activeMemberCount : 활성 회원 수
                        - recruitingPartyCount : 모집 중 파티 수
                        - failedPaymentCount : 결제 실패 건수

                        주의 필요 항목 안내:
                        - failedPaymentPartyCount : 결제 실패가 발생한 파티 수
                        - waitingMatchUserCount : 자동 매칭 대기 중인 회원 수
                        - recruitingPartyCount : 현재 모집 중인 파티 수

                        자동 매칭 상태값 안내:
                        - WAITING : 파티 매칭 대기 중
                        - MATCHED : 파티 매칭 완료
                        - CANCELED : 매칭 신청 취소
                        """,
                responses = {
                        @ApiResponse(
                                responseCode = "200",
                                description = "관리자 대시보드 조회 성공",
                                content = @Content(schema = @Schema(implementation = AdminDashboardResponse.class))
                        ),
                        @ApiResponse(
                                responseCode = "401",
                                description = "토큰 없음 또는 유효하지 않은 토큰"
                        ),
                        @ApiResponse(
                                responseCode = "403",
                                description = "관리자 권한 없음"
                        )
                }
        )
        @GetMapping
        ResponseEntity<AdminDashboardResponse> getDashboard(
                @Parameter(hidden = true) @Auth Accessor accessor
        );
    }

    @Tag(
            name = "Admin API",
            description = "관리자 전용 API"
    )
    interface User {

        @Operation(
                summary = "관리자 회원 목록 조회",
                description = """
                    관리자 회원 관리 페이지에서 전체 회원 목록을 조회합니다.

                    이 API는 아래 화면에서 사용합니다.
                    - 관리자 회원 관리 페이지

                    응답 데이터:
                    - userId : 회원 PK
                    - displayUserId : 관리자 화면 표시용 회원 ID
                    - nickname : 회원 닉네임
                    - email : 회원 이메일
                    - phoneNumber : 회원 전화번호
                    - role : 회원 역할
                    - status : 회원 상태
                    - createdAt : 가입일

                    회원 역할(role) 안내:
                    - USER : 일반 회원
                    - ADMIN : 관리자

                    회원 상태(status) 안내:
                    - PENDING_SIGNUP : 회원가입 진행 중
                    - ACTIVE : 이용중
                    - INACTIVE : 비활성
                    - DELETED : 탈퇴
                    """,
                responses = {
                        @ApiResponse(
                                responseCode = "200",
                                description = "관리자 회원 목록 조회 성공",
                                content = @Content(
                                        array = @ArraySchema(schema = @Schema(implementation = AdminUserResponse.class))
                                )
                        ),
                        @ApiResponse(responseCode = "401", description = "토큰 없음 또는 유효하지 않은 토큰"),
                        @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
                }
        )
        @GetMapping
        ResponseEntity<List<AdminUserResponse>> getUsers(
                @Parameter(hidden = true) @Auth Accessor accessor
        );

        @Operation(
                summary = "관리자 회원 상세 조회",
                description = """
                    관리자 회원 상세 페이지에서 단일 회원 정보를 조회합니다.

                    이 API는 아래 화면에서 사용합니다.
                    - 관리자 회원 상세 페이지

                    응답 데이터:
                    - userId : 회원 PK
                    - displayUserId : 관리자 화면 표시용 회원 ID
                    - nickname : 회원 닉네임
                    - email : 회원 이메일
                    - phoneNumber : 회원 전화번호
                    - role : 회원 역할
                    - status : 회원 상태
                    - createdAt : 가입일
                    - usingPartyCount : 파티장으로 이용중인 파티 수
                    - usingParties : 파티장으로 이용중인 파티 목록

                    이용중인 파티 상태값 안내:
                    - WAITING_START : 이용 시작 대기
                    - ACTIVE : 이용중
                    - TERMINATION_PENDING : 종료 예정
                    - TERMINATED : 종료 완료

                    모집 상태값 안내:
                    - RECRUITING : 모집중
                    - FULL : 모집 완료
                    - CLOSED : 모집 종료
                    """,
                responses = {
                        @ApiResponse(
                                responseCode = "200",
                                description = "관리자 회원 상세 조회 성공",
                                content = @Content(schema = @Schema(implementation = AdminUserDetailResponse.class))
                        ),
                        @ApiResponse(responseCode = "401", description = "토큰 없음 또는 유효하지 않은 토큰"),
                        @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
                        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
                }
        )
        @GetMapping("/{userId}")
        ResponseEntity<AdminUserDetailResponse> getUser(
                @Parameter(hidden = true) @Auth Accessor accessor,
                @PathVariable Long userId
        );
    }

    @Tag(
            name = "Admin API",
            description = "관리자 전용 API"
    )
    interface Party {

        @Operation(
                summary = "관리자 파티 목록 조회",
                description = """
                    관리자 파티 관리 페이지에서 전체 파티 목록을 조회합니다.

                    이 API는 아래 화면에서 사용합니다.
                    - 관리자 파티 관리 페이지

                    응답 데이터:
                    - partyId : 파티 PK
                    - displayPartyId : 관리자 화면 표시용 파티 ID
                    - productName : 상품명
                    - hostNickname : 파티장 닉네임
                    - currentMemberCount : 현재 인원 수
                    - maxMemberCount : 최대 인원 수
                    - pricePerMember : 1인 월 이용 금액
                    - nextBillingDate : 다음 정산일
                    - recruitStatus : 모집 상태
                    - operationStatus : 운영 상태

                    모집 상태값 안내:
                    - RECRUITING : 모집 중
                    - FULL : 모집 완료
                    - CLOSED : 모집 종료

                    운영 상태값 안내:
                    - WAITING_START : 시작 대기
                    - ACTIVE : 운영 중
                    - TERMINATION_PENDING : 종료 예정
                    - TERMINATED : 종료 완료
                    """,
                responses = {
                        @ApiResponse(
                                responseCode = "200",
                                description = "관리자 파티 목록 조회 성공",
                                content = @Content(
                                        array = @ArraySchema(schema = @Schema(implementation = AdminPartyResponse.class))
                                )
                        ),
                        @ApiResponse(responseCode = "401", description = "토큰 없음 또는 유효하지 않은 토큰"),
                        @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
                }
        )
        @GetMapping
        ResponseEntity<List<AdminPartyResponse>> getParties(
                @Parameter(hidden = true) @Auth Accessor accessor
        );

        @Operation(
                summary = "관리자 파티 상세 조회",
                description = """
                    관리자 파티 상세 페이지에서 단일 파티 정보와 참여 멤버 목록을 조회합니다.

                    이 API는 아래 화면에서 사용합니다.
                    - 관리자 파티 상세 페이지

                    응답 데이터:
                    - partyId : 파티 PK
                    - displayPartyId : 관리자 화면 표시용 파티 ID
                    - productName : 상품명
                    - hostNickname : 파티장 닉네임
                    - createdAt : 파티 생성일
                    - currentMemberCount : 현재 인원 수
                    - maxMemberCount : 최대 인원 수
                    - pricePerMember : 1인 월 이용 금액
                    - nextBillingDate : 다음 정산일
                    - recruitStatus : 모집 상태
                    - operationStatus : 운영 상태
                    - members : 참여 멤버 목록

                    모집 상태값 안내:
                    - RECRUITING : 모집 중
                    - FULL : 모집 완료
                    - CLOSED : 모집 종료

                    운영 상태값 안내:
                    - WAITING_START : 시작 대기
                    - ACTIVE : 운영 중
                    - TERMINATION_PENDING : 종료 예정
                    - TERMINATED : 종료 완료

                    멤버 역할값 안내:
                    - HOST : 파티장
                    - MEMBER : 파티원

                    멤버 상태값 안내:
                    - ACTIVE : 이용 중
                    - LEAVE_RESERVED : 탈퇴 예약
                    - LEFT : 탈퇴 완료

                    안내:
                    - 현재 결제 도메인에 멤버별 결제 상태 테이블이 없으므로 멤버별 결제 상태는 응답하지 않습니다.
                    """,
                responses = {
                        @ApiResponse(
                                responseCode = "200",
                                description = "관리자 파티 상세 조회 성공",
                                content = @Content(schema = @Schema(implementation = AdminPartyDetailResponse.class))
                        ),
                        @ApiResponse(responseCode = "401", description = "토큰 없음 또는 유효하지 않은 토큰"),
                        @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
                        @ApiResponse(responseCode = "404", description = "파티를 찾을 수 없음")
                }
        )
        @GetMapping("/{partyId}")
        ResponseEntity<AdminPartyDetailResponse> getParty(
                @Parameter(hidden = true) @Auth Accessor accessor,
                @PathVariable Long partyId
        );
    }

}