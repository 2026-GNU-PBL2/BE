package pbl2.sub119.backend.party.common.mapper;

import org.apache.ibatis.annotations.Mapper;
import pbl2.sub119.backend.party.common.entity.PartyHistory;

@Mapper
public interface PartyHistoryMapper {

    // 파티 이력 저장
    int insertHistory(PartyHistory history);
}