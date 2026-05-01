package pbl2.sub119.backend.party.vacancy.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.party.vacancy.dto.response.HostVacancyPartyResponse;
import pbl2.sub119.backend.party.vacancy.dto.response.MemberVacancyPartyResponse;
import pbl2.sub119.backend.party.vacancy.dto.response.PartyVacancyDetailResponse;

@Mapper
public interface PartyVacancyQueryMapper {

    // 파티원 결원 예정/결원 파티 목록 조회
    List<MemberVacancyPartyResponse> findMemberVacancyParties(
            @Param("productId") String productId
    );

    // 파티원 결원 예정/결원 파티 상세 조회
    PartyVacancyDetailResponse findMemberVacancyDetail(
            @Param("partyId") Long partyId
    );

    // 파티장 결원 예정/결원 파티 목록 조회
    List<HostVacancyPartyResponse> findHostVacancyParties(
            @Param("productId") String productId
    );

    // 파티장 결원 예정/결원 파티 상세 조회
    PartyVacancyDetailResponse findHostVacancyDetail(
            @Param("partyId") Long partyId
    );
}