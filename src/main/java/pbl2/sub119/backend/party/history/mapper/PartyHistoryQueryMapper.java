package pbl2.sub119.backend.party.history.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.party.history.dto.PartyHistoryResponse;

@Mapper
public interface PartyHistoryQueryMapper {

    // 내 파티 히스토리 목록 조회
    List<PartyHistoryResponse> findMyPartyHistories(@Param("userId") Long userId);
}