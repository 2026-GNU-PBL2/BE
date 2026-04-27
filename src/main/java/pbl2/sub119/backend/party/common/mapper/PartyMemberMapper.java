package pbl2.sub119.backend.party.common.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.common.enumerated.PartyMemberStatus;
import pbl2.sub119.backend.party.common.entity.PartyMember;

@Mapper
public interface PartyMemberMapper {

    int insertPartyMember(PartyMember partyMember);

    PartyMember findByPartyIdAndUserId(
            @Param("partyId") Long partyId,
            @Param("userId") Long userId
    );

    PartyMember findByPartyIdAndUserIdForUpdate(
            @Param("partyId") Long partyId,
            @Param("userId") Long userId
    );

    PartyMember findHostMemberByPartyIdForUpdate(@Param("partyId") Long partyId);

    PartyMember findById(@Param("memberId") Long memberId);

    List<PartyMember> findMembersByPartyId(@Param("partyId") Long partyId);

    List<PartyMember> findLeaveReservedMembers(@Param("partyId") Long partyId);

    List<PartyMember> findSwitchWaitingMembers(@Param("partyId") Long partyId);

    List<PartyMember> findProvisionTargetMembersByPartyId(@Param("partyId") Long partyId);

    int countOccupiedMembers(@Param("partyId") Long partyId);

    int countRecurringBillableMembers(@Param("partyId") Long partyId);

    int updateLeaveReserved(
            @Param("partyId") Long partyId,
            @Param("userId") Long userId
    );

    int clearLeaveReserved(
            @Param("partyId") Long partyId,
            @Param("userId") Long userId
    );

    int updateStatus(
            @Param("memberId") Long memberId,
            @Param("status") PartyMemberStatus status
    );

    int updateStatusAndLeftAt(
            @Param("memberId") Long memberId,
            @Param("status") PartyMemberStatus status
    );

    int updateStatusAndActivatedAt(
            @Param("memberId") Long memberId,
            @Param("status") PartyMemberStatus status
    );
}