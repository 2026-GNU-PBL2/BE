package pbl2.sub119.backend.settlement.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.settlement.entity.Settlement;

@Mapper
public interface SettlementMapper {

    Settlement findByPartyCycleId(@Param("partyCycleId") Long partyCycleId);

    int insertSettlement(Settlement settlement);
}