package pbl2.sub119.backend.party.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.party.entity.PartyMember;

@Mapper
public interface PartyMemberMapper {

    int insertPartyMember(PartyMember partyMember);

    PartyMember findByPartyIdAndUserId(
            @Param("partyId") Long partyId,
            @Param("userId") Long userId
    );

    List<PartyMember> findMembersByPartyId(@Param("partyId") Long partyId);

    int countOccupiedMembers(@Param("partyId") Long partyId);
}