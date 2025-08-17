package udpm.hn.studentattendance.infrastructure.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.entities.UserStudentFactory;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import java.util.List;

@Repository
public interface CommonScheduleReminderRepository extends JpaRepository<PlanDate, String> {

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

    @Query(value = """
                SELECT
                us.email_fpt
                FROM user_staff us
                JOIN student_attendance.role r on us.id = r.id_user_staff
                JOIN student_attendance.factory f on us.id = f.id_user_staff
                WHERE
                r.code = 3
                AND f.id = :factoryId
                AND us.status = 1
                AND r.status = 1
                AND f.status = 1
            """, nativeQuery = true)
    List<String> findTeachersByFactoryId(String factoryId);
}