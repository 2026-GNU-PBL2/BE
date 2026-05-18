package pbl2.sub119.backend.payment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.common.enumerated.PartyCycleStatus;
import pbl2.sub119.backend.party.common.enumerated.OperationStatus;
import pbl2.sub119.backend.payment.dto.RecurringPaymentTarget;

import java.util.List;

@Mapper
public interface RecurringPaymentQueryMapper {

    List<RecurringPaymentTarget> findRunningCycles(
            @Param("runningStatus") PartyCycleStatus runningStatus,
            @Param("activeOperationStatus") OperationStatus activeOperationStatus
    );
}
