package udpm.hn.studentattendance.infrastructure.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.entities.UserStudentFactory;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import java.util.List;

/**
 * Repository interface for queries related to schedule reminders
 */
@Repository
public interface CommonScheduleReminderRepository extends JpaRepository<PlanDate, String> {

    /**
     * Find plan dates that will start within the given time range
     * 
     * @param startTimeRange    Lower bound of start time
     * @param endTimeRange      Upper bound of start time
     * @param status            Status of plan dates to find (usually ACTIVE)
     * @param planFactoryStatus Status of plan factory (usually ACTIVE)
     * @return List of plan dates starting within the specified time range
     */
    @Query(value = """
                SELECT pd
                FROM PlanDate pd
                JOIN FETCH pd.planFactory pf
                JOIN FETCH pf.factory f
                JOIN FETCH f.project p
                JOIN FETCH p.subjectFacility sf
                JOIN FETCH sf.subject s
                JOIN FETCH f.userStaff us
                WHERE pd.status = :status
                AND pf.status = :planFactoryStatus
                AND pd.startDate >= :startTimeRange
                AND pd.startDate <= :endTimeRange
                ORDER BY pd.startDate ASC
            """)
    List<PlanDate> findUpcomingPlanDates(
            Long startTimeRange,
            Long endTimeRange,
            EntityStatus status,
            EntityStatus planFactoryStatus);

    /**
     * Find all active students in a factory
     * 
     * @param factoryId The factory ID to search for
     * @param status    The status of student factory relationships to return
     * @return List of UserStudentFactory objects
     */
    @Query("""
                SELECT usf
                FROM UserStudentFactory usf
                JOIN FETCH usf.userStudent us
                WHERE usf.factory.id = :factoryId
                AND usf.status = :status
                AND us.status = :status
                AND usf.factory.status = :status
            """)
    List<UserStudentFactory> findStudentsByFactoryIdAndStatus(String factoryId, EntityStatus status);
}