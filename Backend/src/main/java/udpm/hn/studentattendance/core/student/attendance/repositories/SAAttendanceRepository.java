package udpm.hn.studentattendance.core.student.attendance.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.student.attendance.model.request.SAFilterAttendanceRequest;
import udpm.hn.studentattendance.core.student.attendance.model.response.SAAttendanceResponse;
import udpm.hn.studentattendance.core.student.attendance.model.response.SAAttendanceRecoveryResponse;
import udpm.hn.studentattendance.entities.Attendance;
import udpm.hn.studentattendance.repositories.AttendanceRepository;

import java.util.Optional;

@Repository
public interface SAAttendanceRepository extends AttendanceRepository {

    Optional<Attendance> findByUserStudent_IdAndPlanDate_Id(String idUser, String idPlanDate);

    @Query(value = """
        SELECT
            ROW_NUMBER() OVER (ORDER BY pd.start_date ASC) as orderNumber,
            a.id AS id,
            pd.id AS idPlanDate,
            pd.start_date AS startDate,
            pd.end_date AS endDate,
            a.created_at AS timeCheckin,
            pd.late_arrival AS lateArrival,
            pd.shift AS shift,
            pd.type AS type,
            pd.link AS link,
            f.name AS factoryName,
            pd.required_checkin AS requiredCheckin,
            pd.required_checkout AS requiredCheckout,
            COALESCE((SELECT COUNT(*) FROM plan_date WHERE id_plan_factory = pf.id) / 100 * COALESCE(p.max_late_arrival, 0), 0)  AS totalLateAttendance,
            COALESCE((
              SELECT
                  SUM(
                      CASE
                        WHEN a2.late_checkin IS NOT NULL AND a2.late_checkout IS NOT NULL THEN 2
                        WHEN a2.late_checkin IS NOT NULL OR a2.late_checkout IS NOT NULL THEN 1
                        ELSE 0
                      END
                  )
              FROM attendance a2
              JOIN plan_date pd2 ON pd2.id = a2.id_plan_date
              WHERE
                pd2.status = 1 AND
                a2.id_user_student = usf.id_user_student AND
                pd2.id_plan_factory = pf.id
            ), 0) AS currentLateAttendance,
            CONCAT(us.code, ' - ', us.name) AS teacherName,
            COALESCE(a.attendance_status, 0) AS status
        FROM user_student_factory usf
        JOIN factory f ON usf.id_factory = f.id
        JOIN plan_factory pf ON f.id = pf.id_factory
        JOIN plan p ON p.id = pf.id_plan
        JOIN plan_date pd ON pd.id_plan_factory = pf.id
        LEFT JOIN user_staff us ON f.id_user_staff = us.id
        LEFT JOIN attendance a ON pd.id = a.id_plan_date AND a.id_user_student = usf.id_user_student
        WHERE
            pd.status = 1 AND
            pf.status = 1 AND
            p.status = 1 AND
            f.status = 1 AND
            usf.status = 1 AND
            EXISTS(
                SELECT 1
                FROM project pj
                JOIN subject_facility sf ON sf.id = pj.id_subject_facility
                JOIN subject s2 ON s2.id = sf.id_subject
                JOIN facility f2 ON sf.id_facility = f2.id
                JOIN semester s ON pj.id_semester = s.id
                WHERE
                     f.id_project = pj.id AND
                     f2.id = :#{#request.idFacility} AND
                     pj.status = 1 AND
                     sf.status = 1 AND
                     s.status = 1 AND
                     s2.status = 1 AND
                     f2.status = 1
            ) AND
            (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR (
                f.name LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%') OR
                us.code LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%') OR
                us.name LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')
            )) AND
            (COALESCE(:#{#request.status}, 0) = 0 OR a.attendance_status = :#{#request.status}) AND
            (:#{#request.type} IS NULL OR pd.type = :#{#request.type}) AND
            DATE(FROM_UNIXTIME(pd.start_date / 1000)) = CURDATE() AND
            usf.id_user_student = :#{#request.idUserStudent}
        ORDER BY
            pd.start_date ASC 
    """, countQuery = """
       SELECT 
            COUNT(*)
        FROM user_student_factory usf
        JOIN factory f ON usf.id_factory = f.id
        JOIN plan_factory pf ON f.id = pf.id_factory
        JOIN plan_date pd ON pd.id_plan_factory = pf.id
        LEFT JOIN user_staff us ON f.id_user_staff = us.id
        LEFT JOIN attendance a ON pd.id = a.id_plan_date AND a.id_user_student = usf.id_user_student
        WHERE
            pd.status = 1 AND
            pf.status = 1 AND
            f.status = 1 AND
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
                     f2.id = :#{#request.idFacility} AND
                     p.status = 1 AND
                     pj.status = 1 AND
                     sf.status = 1 AND
                     s.status = 1 AND
                     s2.status = 1 AND
                     f2.status = 1
            ) AND
            (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR (
                f.name LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%') OR
                us.code LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%') OR
                us.name LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')
            )) AND
            (COALESCE(:#{#request.status}, 0) = 0 OR a.attendance_status = :#{#request.status}) AND
            (:#{#request.type} IS NULL OR pd.type = :#{#request.type}) AND
            DATE(FROM_UNIXTIME(pd.start_date / 1000)) = CURDATE() AND
            usf.id_user_student = :#{#request.idUserStudent}
    """, nativeQuery = true)
    Page<SAAttendanceResponse> getAllByFilter(Pageable pageable, SAFilterAttendanceRequest request);


    @Query(value = """
        SELECT
            COALESCE((SELECT COUNT(*) FROM plan_date WHERE id_plan_factory = pf.id) / 100 * COALESCE(p.max_late_arrival, 0), 0)  AS totalLateAttendance,
            COALESCE((
              SELECT
                  SUM(
                      CASE
                        WHEN a.late_checkin IS NOT NULL AND a.late_checkout IS NOT NULL THEN 2
                        WHEN a.late_checkin IS NOT NULL OR a.late_checkout IS NOT NULL THEN 1
                        ELSE 0
                      END
                  )
              FROM attendance a
              JOIN plan_date pd2 ON pd2.id = a.id_plan_date
              WHERE
                pd2.status = 1 AND
                a.id_user_student = :idUserStudent AND
                pd2.id_plan_factory = pf.id
            ), 0) AS currentLateAttendance
        FROM plan_date pd
        JOIN plan_factory pf ON pd.id_plan_factory = pf.id
        JOIN plan p ON p.id = pf.id_plan
        WHERE
            p.status = 1 AND
            pf.status = 1 AND
            pd.status = 1 AND
            pd.id = :idPlanDate
    """, nativeQuery = true)
    Optional<SAAttendanceRecoveryResponse> getAttendanceRecovery(String idPlanDate, String idUserStudent);

}
