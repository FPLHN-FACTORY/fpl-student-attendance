package udpm.hn.studentattendance.core.student.attendance.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.student.attendance.model.request.SAFilterAttendanceRequest;
import udpm.hn.studentattendance.core.student.attendance.model.response.SAAttendanceResponse;
import udpm.hn.studentattendance.entities.Attendance;
import udpm.hn.studentattendance.repositories.AttendanceRepository;

import java.util.Optional;

@Repository
public interface SAAttendanceRepository extends AttendanceRepository {

    Optional<Attendance> findByUserStudent_IdAndPlanDate_Id(String idUser, String idPlanDate);

    @Query(value = """
        SELECT 
            ROW_NUMBER() OVER (ORDER BY a.created_at DESC) as orderNumber,
            a.id AS id,
            pd.id AS idPlanDate,
            pd.start_date AS date,
            a.created_at AS timeCheckin,
            pd.late_arrival AS lateArrival,
            pd.shift AS shift,
            f.name AS factoryName,
            CONCAT(us.code, ' - ', us.name) AS teacherName,
            COALESCE(a.attendance_status, 0) AS status
        FROM user_student_factory usf
        JOIN factory f ON usf.id_factory = f.id
        JOIN plan_factory pf ON f.id = pf.id_factory
        JOIN plan_date pd ON pd.id_plan_factory = pf.id
        JOIN plan p ON pf.id_plan = p.id
        LEFT JOIN user_staff us ON f.id_user_staff = us.id
        JOIN project pj ON f.id_project = pj.id
        JOIN subject_facility sf ON sf.id = pj.id_subject_facility
        JOIN facility f2 ON sf.id_facility = f2.id
        JOIN semester s ON pj.id_semester = s.id
        LEFT JOIN attendance a ON pd.id = a.id_plan_date
        WHERE
            pd.status = 1 AND
            pf.status = 1 AND
            p.status = 1 AND
            f.status = 1 AND
            pj.status = 1 AND
            s.status = 1 AND
            f2.status = 1 AND
            (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR BINARY f.name LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')) AND
            (:#{#request.status} IS NULL OR a.status = :#{#request.status}) AND
            DATE(FROM_UNIXTIME(pd.start_date / 1000)) = CURDATE() AND
            usf.id_user_student = :#{#request.idUserStudent} AND
            f2.id = :#{#request.idFacility}
        ORDER BY
            pd.start_date DESC 
    """, countQuery = """
       SELECT 
            COUNT(*)
        FROM user_student_factory usf
        JOIN factory f ON usf.id_factory = f.id
        JOIN plan_factory pf ON f.id = pf.id_factory
        JOIN plan_date pd ON pd.id_plan_factory = pf.id
        JOIN plan p ON pf.id_plan = p.id
        LEFT JOIN user_staff us ON f.id_user_staff = us.id
        JOIN project pj ON f.id_project = pj.id
        JOIN subject_facility sf ON sf.id = pj.id_subject_facility
        JOIN facility f2 ON sf.id_facility = f2.id
        JOIN semester s ON pj.id_semester = s.id
        LEFT JOIN attendance a ON pd.id = a.id_plan_date
        WHERE
            pd.status = 1 AND
            pf.status = 1 AND
            p.status = 1 AND
            f.status = 1 AND
            pj.status = 1 AND
            s.status = 1 AND
            f2.status = 1 AND
            (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR BINARY f.name LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')) AND
            (:#{#request.status} IS NULL OR a.status = :#{#request.status}) AND
            DATE(FROM_UNIXTIME(pd.start_date / 1000)) = CURDATE() AND
            usf.id_user_student = :#{#request.idUserStudent} AND
            f2.id = :#{#request.idFacility}
    """, nativeQuery = true)
    Page<SAAttendanceResponse> getAllByFilter(Pageable pageable, SAFilterAttendanceRequest request);

}
