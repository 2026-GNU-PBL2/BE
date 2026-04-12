package pbl2.sub119.backend.party.common.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.party.common.entity.HostTransferRequest;
import pbl2.sub119.backend.party.common.enumerated.HostTransferStatus;

@Mapper
public interface HostTransferRequestMapper {

    int insertHostTransferRequest(HostTransferRequest request);

    HostTransferRequest findById(@Param("id") Long id);

    HostTransferRequest findByIdForUpdate(@Param("id") Long id);

    HostTransferRequest findActiveRequestByPartyId(@Param("partyId") Long partyId);

    List<HostTransferRequest> findByPartyId(@Param("partyId") Long partyId);

    int updateStatus(
            @Param("id") Long id,
            @Param("status") HostTransferStatus status
    );

    int updateStatusWithRespondedAt(
            @Param("id") Long id,
            @Param("status") HostTransferStatus status
    );

    int updateStatusWithCompletedAt(
            @Param("id") Long id,
            @Param("status") HostTransferStatus status
    );
}