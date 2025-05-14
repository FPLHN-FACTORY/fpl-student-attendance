package udpm.hn.studentattendance.core.staff.plan.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateAttendanceRequest;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateAttendanceResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateStudentResponse;
import udpm.hn.studentattendance.entities.Attendance;
import udpm.hn.studentattendance.repositories.AttendanceRepository;

import java.util.Optional;

@Repository
public interface SPDAttendanceRepository extends AttendanceRepository {

    Optional<Attendance> findByPlanDate_IdAndUserStudent_Id(String idPlanDate, String idUserStudent);

    @Query(value = """
        SELECT 
            ROW_NUMBER() OVER (ORDER BY us.name ASC) as orderNumber,
            us.id,
            us.code,
            us.name,
            COALESCE(a.attendance_status, 0) AS status
        FROM user_student_factory usf
        JOIN factory f ON usf.id_factory = f.id
        JOIN plan_factory pf ON f.id = pf.id_factory
        JOIN plan_date pd ON pd.id_plan_factory = pf.id
        JOIN user_student us ON usf.id_user_student = us.id
        LEFT JOIN attendance a ON pd.id = a.id_plan_date AND a.id_user_student = usf.id_user_student
        WHERE
            pd.status = 1 AND
            pf.status = 1 AND
            f.status = 1 AND
            us.status = 1 AND
            usf.status = 1 AND
            EXISTS(
                SELECT 1
                FROM plan p
                JOIN project pj ON f.id_project = pj.id
                JOIN subject_facility sf ON sf.id = pj.id_subject_facility
                JOIN subject s2 ON s2.id = sf.id_subject
                JOIN facility f2 ON sf.id_facility = f2.id
                JOIN semester s ON pj.id_semester = s.id
                WHERE 
                     pf.id_plan = p.id AND
                     p.status = 1 AND
                     pj.status = 1 AND
                     s.status = 1 AND
                     s2.status = 1 AND
                     f2.status = 1 AND
                     sf.status = 1 AND
                     f2.id = :#{#request.idFacility}
            ) AND
            (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR (
                BINARY us.code LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%') OR
                BINARY us.name LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')
            )) AND
            (COALESCE(:#{#request.status}, 0) = 0 OR a.status = :#{#request.status}) AND
            pd.start_date <= UNIX_TIMESTAMP(NOW()) * 1000 AND
            pd.id = :#{#request.idPlanDate}
        ORDER BY
            us.name ASC 
    """, countQuery = """
       SELECT 
            COUNT(*)
        FROM user_student_factory usf
        JOIN factory f ON usf.id_factory = f.id
        JOIN plan_factory pf ON f.id = pf.id_factory
        JOIN plan_date pd ON pd.id_plan_factory = pf.id
        JOIN user_student us ON usf.id_user_student = us.id
        LEFT JOIN attendance a ON pd.id = a.id_plan_date AND a.id_user_student = usf.id_user_student
        WHERE
            pd.status = 1 AND
            pf.status = 1 AND
            f.status = 1 AND
            us.status = 1 AND
            usf.status = 1 AND
            EXISTS(
                SELECT 1
                FROM plan p
                JOIN project pj ON f.id_project = pj.id
                JOIN subject_facility sf ON sf.id = pj.id_subject_facility
                JOIN subject s2 ON s2.id = sf.id_subject
                JOIN facility f2 ON sf.id_facility = f2.id
                JOIN semester s ON pj.id_semester = s.id
                WHERE 
                     pf.id_plan = p.id AND
                     p.status = 1 AND
                     pj.status = 1 AND
                     s.status = 1 AND
                     s2.status = 1 AND
                     f2.status = 1 AND
                     sf.status = 1 AND
                     f2.id = :#{#request.idFacility}
            ) AND
            (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR (
                BINARY us.code LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%') OR
                BINARY us.name LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')
            )) AND
            (COALESCE(:#{#request.status}, 0) = 0 OR a.status = :#{#request.status}) AND
            pd.start_date <= UNIX_TIMESTAMP(NOW()) * 1000 AND
            pd.id = :#{#request.idPlanDate}
    """, nativeQuery = true)
    Page<SPDPlanDateStudentResponse> getAllByFilter(Pageable pageable, SPDFilterPlanDateAttendanceRequest request);

    @Query(value = """
                SELECT
                    1 as orderNumber,
                    pd.id,
                    pl.id AS planId,
                    pl.name AS planName,
                    pf.id AS factoryId,
                    f.name AS factoryName
                FROM plan_date pd
                JOIN plan_factory pf ON pd.id_plan_factory = pf.id
                JOIN plan pl ON pf.id_plan = pl.id
                JOIN factory f ON pf.id_factory = f.id
                WHERE
                    pf.status = 1 AND
                    pl.status = 1 AND
                    f.status = 1 AND
                    EXISTS(
                        SELECT 1
                        FROM project p
                        JOIN level_project lp ON lp.id = p.id_level_project
                        JOIN semester s ON s.id = p.id_semester
                        JOIN subject_facility sf ON sf.id = p.id_subject_facility
                        JOIN subject s2 ON s2.id = sf.id_subject
                        JOIN facility f2 ON sf.id_facility = f2.id
                        WHERE
                            p.id = pl.id_project AND
                            p.status = 1 AND
                            lp.status = 1 AND
                            s.status = 1 AND
                            sf.status = 1 AND
                            s2.status = 1 AND
                            f2.status = 1 AND
                            f2.id = :idFacility
                    ) AND
                    pd.id = :idPlanDate
            """, nativeQuery = true)
    Optional<SPDPlanDateAttendanceResponse> getDetailPlanDate(String idPlanDate, String idFacility);

}
