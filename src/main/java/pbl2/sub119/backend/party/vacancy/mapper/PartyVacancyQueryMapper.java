package pbl2.sub119.backend.party.vacancy.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.party.vacancy.dto.response.HostVacancyPartyResponse;
import pbl2.sub119.backend.party.vacancy.dto.response.MemberVacancyPartyResponse;
import pbl2.sub119.backend.party.vacancy.dto.response.PartyVacancyDetailResponse;

@Mapper
public interface PartyVacancyQueryMapper {

    List<MemberVacancyPartyResponse> findMemberVacancyParties(
            @Param("productId") String productId
    );

    PartyVacancyDetailResponse findMemberVacancyDetail(
            @Param("partyId") Long partyId
    );

    List<HostVacancyPartyResponse> findHostVacancyParties(
            @Param("productId") String productId
    );

    PartyVacancyDetailResponse findHostVacancyDetail(
            @Param("partyId") Long partyId
    );
}