package pbl2.sub119.backend.party.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.party.entity.Party;
import pbl2.sub119.backend.party.enumerated.RecruitStatus;

@Mapper
public interface PartyMapper {

    int insertParty(Party party);

    Party findById(@Param("partyId") Long partyId);

    Party findByIdForUpdate(@Param("partyId") Long partyId);

    List<Party> findByProductId(@Param("productId") String productId);

    int updateCurrentMemberCount(
            @Param("partyId") Long partyId,
            @Param("currentMemberCount") int currentMemberCount
    );

    int updateRecruitStatus(
            @Param("partyId") Long partyId,
            @Param("recruitStatus") RecruitStatus recruitStatus
    );
}