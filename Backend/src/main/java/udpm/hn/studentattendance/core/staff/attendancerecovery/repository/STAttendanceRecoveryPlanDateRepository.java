package udpm.hn.studentattendance.core.staff.attendancerecovery.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Plan;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.PlanDateRepository;

import java.util.List;

@Repository
public interface STAttendanceRecoveryPlanDateRepository extends PlanDateRepository {
    @Query(value = """
            SELECT 
            pd
            FROM
            PlanDate pd
            WHERE 
            pd.planFactory.id = :planFactoryId
            AND pd.status = :status
            AND pd.planFactory.status = :planFactoryStatus
            """)
    List<PlanDate> getAllPlanDateByPlanFactoryId(String planFactoryId, EntityStatus status, EntityStatus planFactoryStatus);

    @Query("""
        SELECT a.planDate.id
        FROM Attendance a
        WHERE a.userStudent.id = :studentId
        AND a.planDate.id IN :planDateIds
        AND a.status = :status
        """)
    List<String> findExistingAttendance( String studentId,
             List<String> planDateIds,
             EntityStatus status
    );

    default List<String> findExistingAttendance(String studentId, List<String> planDateIds) {
        return findExistingAttendance(studentId, planDateIds, EntityStatus.ACTIVE);
    }

    @Query(
            value = """
    SELECT EXISTS(
      SELECT 1
      FROM plan_date pd
      JOIN plan_factory pf ON pd.plan_factory_id = pf.id
      WHERE pf.id = :planFactoryId
        AND pd.status = :status
        AND pf.status = :planFactoryStatus
        AND DATE(FROM_UNIXTIME(pd.end_date/1000)) = DATE(FROM_UNIXTIME(:targetDate/1000))
    )
  """,
            nativeQuery = true)
    boolean existsPlanDateInDay(
            String planFactoryId,
            Long targetDate,
            String status,
            String planFactoryStatus
    );


    @Query("""
        SELECT pd
        FROM PlanDate pd
        JOIN FETCH pd.planFactory pf
        WHERE pf.id = :planFactoryId
        AND pd.status = :status
        AND pf.status = :planFactoryStatus
        AND DATE(FROM_UNIXTIME(pd.endDate/1000)) = DATE(FROM_UNIXTIME(:targetDate/1000))
        ORDER BY pd.startDate ASC
        """)
    List<PlanDate> getPlanDatesByFactoryIdAndDate(
            String planFactoryId,
            Long targetDate,
            EntityStatus status,
            EntityStatus planFactoryStatus
    );
}
