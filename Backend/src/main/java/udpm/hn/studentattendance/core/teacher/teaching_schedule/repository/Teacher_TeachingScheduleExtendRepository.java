package udpm.hn.studentattendance.core.teacher.teaching_schedule.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.model.request.Teacher_TeachingScheduleRequest;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.model.response.Teacher_TSDetailPlanDateResponse;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.model.response.Teacher_TeachingScheduleResponse;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.repositories.PlanDateRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Teacher_TeachingScheduleExtendRepository extends PlanDateRepository {

    @Query(
            value = """
                    SELECT
                    ROW_NUMBER() OVER (ORDER BY pd.start_date ASC) AS indexs,
                        pd.id AS idPlanDate,
                        pd.start_date AS teachingDay,
                        pd.shift AS shift,
                        sb.code AS subjectCode,
                        ft.name AS factoryName,
                        p.name AS projectName,
                        pd.late_arrival AS lateArrival,
                        pd.description AS description
                    FROM
                        plan_date pd
                        LEFT JOIN plan_factory pf ON pf.id = pd.id_plan_factory
                        LEFT JOIN factory ft ON ft.id = pf.id_factory
                        LEFT JOIN user_staff us ON us.id = ft.id_user_staff
                        LEFT JOIN project p ON p.id = ft.id_project
                        LEFT JOIN subject_facility sf ON sf.id = p.id_subject_facility
                        LEFT JOIN subject sb ON sb.id = sf.id_subject
                    WHERE
                        us.id = :userId
                        AND us.status = 1
                        AND pd.status = 1
                        AND ft.status = 1
                        AND p.status = 1
                        AND sf.status = 1
                        AND sb.status = 1
                        AND pf.status = 1
                    AND pd.start_date BETWEEN :#{#teachingScheduleRequest.startDate} AND :#{#teachingScheduleRequest.endDate}
                        AND (:#{#teachingScheduleRequest.idSubject} IS NULL OR sb.id = :#{#teachingScheduleRequest.idSubject})
                        AND (:#{#teachingScheduleRequest.idFactory} IS NULL OR ft.id = :#{#teachingScheduleRequest.idFactory})
                        AND (:#{#teachingScheduleRequest.idProject} IS NULL OR p.id = :#{#teachingScheduleRequest.idProject})
                        AND (:#{#teachingScheduleRequest.shift} IS NULL OR pd.shift = :#{#teachingScheduleRequest.shift})
                    ORDER BY pd.start_date ASC 
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM
                        plan_date pd
                        LEFT JOIN plan_factory pf ON pf.id = pd.id_plan_factory
                        LEFT JOIN factory ft ON ft.id = pf.id_factory
                        LEFT JOIN user_staff us ON us.id = ft.id_user_staff
                        LEFT JOIN project p ON p.id = ft.id_project
                        LEFT JOIN subject_facility sf ON sf.id = p.id_subject_facility
                        LEFT JOIN subject sb ON sb.id = sf.id_subject
                    WHERE
                        us.id = :userId
                        AND us.status = 1
                        AND pd.status = 1
                        AND ft.status = 1
                        AND p.status = 1
                        AND sf.status = 1
                        AND sb.status = 1
                        AND pf.status = 1
                        AND pd.start_date BETWEEN :#{#teachingScheduleRequest.startDate} AND :#{#teachingScheduleRequest.endDate}
                        AND (:#{#teachingScheduleRequest.idSubject} IS NULL OR sb.id = :#{#teachingScheduleRequest.idSubject})
                        AND (:#{#teachingScheduleRequest.idFactory} IS NULL OR ft.id = :#{#teachingScheduleRequest.idFactory})
                        AND (:#{#teachingScheduleRequest.idProject} IS NULL OR p.id = :#{#teachingScheduleRequest.idProject})
                        AND (:#{#teachingScheduleRequest.shift} IS NULL OR pd.shift = :#{#teachingScheduleRequest.shift})
                    """,
            nativeQuery = true
    )
    Page<Teacher_TeachingScheduleResponse> getAllTeachingScheduleByStaff(
            String userId, Pageable pageable, Teacher_TeachingScheduleRequest teachingScheduleRequest);

    @Query
            (value = """
                    SELECT 
                    DISTINCT 
                    pd.shift
                    FROM plan_date pd  
                    WHERE pd.status = 1
                          """, nativeQuery = true)
    List<PlanDate> getAllShift();

    @Query
            (value = """
                        SELECT
                        pd.late_arrival as lateArrival,
                        pd.description as description,
                        pd.id as planDateId
                        FROM plan_date pd
                        WHERE pd.id = :planDateId
                    """, nativeQuery = true)
    Optional<Teacher_TSDetailPlanDateResponse> getPlanDateById(String planDateId);
}