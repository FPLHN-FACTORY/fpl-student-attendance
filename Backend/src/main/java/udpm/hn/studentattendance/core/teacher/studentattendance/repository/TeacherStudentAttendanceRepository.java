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

    /**
     * Lấy danh sách sinh viên kèm trạng thái điểm danh (mặc định 1 nếu chưa có bản ghi)
     */
    @Query(value = """
        SELECT
            at.id                  AS attendanceId,
            us.code                AS userStudentCode,
            us.name                AS userStudentName,
            us.id                  AS userStudentId,
            CASE
                WHEN at.id IS NULL THEN 1
                ELSE at.attendance_status
            END                   AS attendanceStatus
        FROM factory ft
        LEFT JOIN user_student_factory usf
            ON usf.id_factory = ft.id
        LEFT JOIN user_student us
            ON us.id = usf.id_user_student
        LEFT JOIN plan_factory pf
            ON pf.id_factory = ft.id
        LEFT JOIN plan_date pd
            ON pd.id_plan_factory = pf.id
        LEFT JOIN attendance at
            ON at.id_plan_date = pd.id
           AND at.id_user_student = us.id
        WHERE pd.id = :planDateId
    """, nativeQuery = true)
    List<TeacherStudentAttendanceResponse> getAllByPlanDate(
            String planDateId
    );

    /**
     * Lấy danh sách userStudentId theo planDate
     */
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

    /**
     * Lấy attendanceId theo planDate và userStudent
     */
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
