package pbl2.sub119.backend.party.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.party.entity.Party;
import pbl2.sub119.backend.party.enumerated.OperationStatus;
import pbl2.sub119.backend.party.enumerated.RecruitStatus;
import pbl2.sub119.backend.party.enumerated.VacancyType;

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

    int updateOperationStatus(
            @Param("partyId") Long partyId,
            @Param("operationStatus") OperationStatus operationStatus
    );

    int updateVacancyType(
            @Param("partyId") Long partyId,
            @Param("vacancyType") VacancyType vacancyType
    );

    int updateHostUserId(
            @Param("partyId") Long partyId,
            @Param("hostUserId") Long hostUserId
    );

    List<Party> findJoinablePartiesByProductId(@Param("productId") String productId);
}