package pbl2.sub119.backend.admin.party.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.admin.party.dto.AdminPartyDetailBaseResponse;
import pbl2.sub119.backend.admin.party.dto.AdminPartyMemberResponse;
import pbl2.sub119.backend.admin.party.dto.AdminPartyResponse;

@Mapper
public interface AdminPartyMapper {

    // 관리자 파티 목록 조회
    List<AdminPartyResponse> findParties();

    // 관리자 파티 상세 기본 정보 조회
    AdminPartyDetailBaseResponse findPartyById(@Param("partyId") Long partyId);

    // 관리자 파티 멤버 목록 조회
    List<AdminPartyMemberResponse> findMembersByPartyId(@Param("partyId") Long partyId);
}