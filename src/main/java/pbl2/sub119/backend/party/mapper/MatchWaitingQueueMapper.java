package pbl2.sub119.backend.party.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.party.entity.MatchWaitingQueue;

@Mapper
public interface MatchWaitingQueueMapper {

    int insertMatchWaitingQueue(MatchWaitingQueue queue);

    MatchWaitingQueue findWaitingByProductIdAndUserId(
            @Param("productId") String productId,
            @Param("userId") Long userId
    );

    MatchWaitingQueue findFirstWaitingByProductId(@Param("productId") String productId);

    List<MatchWaitingQueue> findAllWaitingByProductId(@Param("productId") String productId);

    MatchWaitingQueue findById(@Param("id") Long id);

    int updateMatched(
            @Param("id") Long id,
            @Param("targetPartyId") Long targetPartyId
    );

    int updateCanceled(@Param("id") Long id);

    int countWaitingByProductId(@Param("productId") String productId);
}