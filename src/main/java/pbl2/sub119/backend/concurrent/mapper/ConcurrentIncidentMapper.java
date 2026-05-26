package pbl2.sub119.backend.concurrent.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.concurrent.entity.ConcurrentIncident;
import pbl2.sub119.backend.concurrent.enumerated.IncidentStatus;

@Mapper
public interface ConcurrentIncidentMapper {

    void insert(ConcurrentIncident incident);

    ConcurrentIncident findById(@Param("id") Long id);

    List<ConcurrentIncident> findByPartyId(@Param("partyId") Long partyId);

    boolean existsByPartyIdAndStatus(
            @Param("partyId") Long partyId,
            @Param("status") IncidentStatus status
    );

    // 1차 경고를 받은 이력이 있는 파티인지 확인 (FIRST_WARNING_SENT 또는 RESOLVED 포함)
    boolean hasAnyPriorWarning(@Param("partyId") Long partyId);

    void updateStatus(
            @Param("id") Long id,
            @Param("status") IncidentStatus status
    );

    void updateFirstWarned(
            @Param("id") Long id,
            @Param("firstWarnedAt") java.time.LocalDateTime firstWarnedAt,
            @Param("hostDeadline") java.time.LocalDateTime hostDeadline
    );

    void updateDissolutionDate(
            @Param("id") Long id,
            @Param("dissolutionDate") java.time.LocalDate dissolutionDate
    );

    void updateResolved(@Param("id") Long id);

    void updateAdminEscalated(@Param("id") Long id);

    void updateWebNotified(@Param("id") Long id);

    void updateSmsNotified(@Param("id") Long id);

    // 에스컬레이션 스케줄러용 조회
    List<ConcurrentIncident> findForWebRenotification(@Param("threshold") java.time.LocalDateTime threshold);

    List<ConcurrentIncident> findForSmsRenotification(@Param("threshold") java.time.LocalDateTime threshold);

    List<ConcurrentIncident> findForAdminEscalation(@Param("now") java.time.LocalDateTime now);
}
