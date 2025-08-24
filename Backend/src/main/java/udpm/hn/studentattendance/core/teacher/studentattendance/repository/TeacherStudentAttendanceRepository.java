package udpm.hn.studentattendance.core.teacher.studentattendance.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.teacher.studentattendance.model.response.TeacherStudentAttendanceResponse;
import udpm.hn.studentattendance.repositories.AttendanceRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherStudentAttendanceRepository extends AttendanceRepository {


    @Query(value = """
    SELECT 
        ROW_NUMBER() OVER (ORDER BY us.name ASC) as orderNumber,
        us.id,
        us.code,
        us.name,
        pd.required_checkin,
        pd.required_checkout,
        a.id as idAttendance,
        COALESCE(a.attendance_status, 0) AS status,
        COALESCE(a.created_at, 0) AS createdAt,
        COALESCE(a.updated_at, 0) AS updatedAt
    FROM user_student_factory usf
    JOIN factory f ON usf.id_factory = f.id
    JOIN plan_factory pf ON f.id = pf.id_factory
    JOIN plan_date pd ON pd.id_plan_factory = pf.id
    JOIN user_student us ON usf.id_user_student = us.id
    LEFT JOIN attendance a ON (pd.id = a.id_plan_date AND a.id_user_student = us.id)
    WHERE
        pd.status = 1 AND
        pf.status = 1 AND
        f.status = 1 AND
        us.status = 1 AND
        usf.status = 1 AND
        pd.id = :idPlanDate AND
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
                 f2.id = :idFacility
        )
    ORDER BY
        us.name ASC 
""", nativeQuery = true)
    List<TeacherStudentAttendanceResponse> getAllByPlanDate(
            String idPlanDate, String idFacility
    );


    @Query(value = """
        SELECT usf.id_user_student
        FROM plan_date pd
        JOIN plan_factory pf  ON pd.id_plan_factory = pf.id
        JOIN factory f        ON pf.id_factory = f.id
        JOIN user_student_factory usf ON usf.id_factory = f.id
        WHERE pd.id = :planDateId
    """, nativeQuery = true)
    List<String> getUserStudentIdsByPlanDate(
            String planDateId
    );

    @Query(value = """
        SELECT a.id
        FROM attendance a
        WHERE a.id_plan_date = :planDateId
          AND a.id_user_student = :userStudentId
    """, nativeQuery = true)
    Optional<String> findAttendanceIdByPlanDateAndStudent(
            String planDateId,
            String userStudentId
    );
}
