package udpm.hn.studentattendance.core.student.attendance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.student.attendance.model.request.StudentAttendanceSearchRequest;
import udpm.hn.studentattendance.core.student.attendance.model.response.StudentAttendanceResponse;
import udpm.hn.studentattendance.repositories.FacilityRepository;

@Repository
public interface StudentAttendanceRepository extends FacilityRepository {

    @Query(value = """
        SELECT
            ROW_NUMBER() OVER (ORDER BY pd.start_date ASC) AS indexs,
            pd.id AS id,
            pd.start_date AS attendanceDay,
            s.code AS subjectCode,
            s.name AS subjectName,
            us.name AS staffName,
            pd.shift AS shift,
            pd.description AS description
        FROM
            plan_date pd
        LEFT JOIN factory f ON pd.id_factory = f.id
        LEFT JOIN project p ON f.id_project = p.id
        LEFT JOIN user_staff us ON f.id_user_staff = us.id
        LEFT JOIN subject_facility sf ON p.id_subject_facility = sf.id
        LEFT JOIN subject s ON sf.id_subject = s.id
        WHERE
            id_factory IN (
                SELECT id_factory
                FROM user_student_factory
                WHERE id_user_student = :#{#request.idStudent}
            )
            AND pd.start_date BETWEEN :#{#request.now} AND :#{#request.max}
        """,
            countQuery = """
        SELECT COUNT(*) FROM plan_date pd
        LEFT JOIN factory f ON pd.id_factory = f.id
        LEFT JOIN project p ON f.id_project = p.id
        LEFT JOIN user_staff us ON f.id_user_staff = us.id
        LEFT JOIN subject_facility sf ON p.id_subject_facility = sf.id
        LEFT JOIN subject s ON sf.id_subject = s.id
        WHERE
            id_factory IN (
                SELECT id_factory
                FROM user_student_factory
                WHERE id_user_student = :#{#request.idStudent}
            )
            AND pd.start_date BETWEEN :#{#request.now} AND :#{#request.max}
        """,
            nativeQuery = true)
    Page<StudentAttendanceResponse> getList(Pageable pageable, @Param("request") StudentAttendanceSearchRequest request );

}
