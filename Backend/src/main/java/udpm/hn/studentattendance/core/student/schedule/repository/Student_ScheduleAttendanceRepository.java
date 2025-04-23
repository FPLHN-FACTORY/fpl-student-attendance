package udpm.hn.studentattendance.core.student.schedule.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.student.schedule.model.request.Student_ScheduleAttendanceSearchRequest;
import udpm.hn.studentattendance.core.student.schedule.model.response.Student_ScheduleAttendanceResponse;
import udpm.hn.studentattendance.repositories.FacilityRepository;

@Repository
public interface Student_ScheduleAttendanceRepository extends FacilityRepository {

    @Query(value = """
            SELECT
                       ROW_NUMBER() OVER (ORDER BY pd.start_date ASC) AS indexs,
                       pd.id AS id,
                       pd.start_date AS attendanceDay,
                       s.name AS subjectName,
                       us.name AS staffName,
                       pd.shift AS shift,
                       pd.description AS description,
                       f.name as factoryName,
                       p.name as projectName
                   FROM
                   plan_date pd
            	   LEFT JOIN plan_factory pdf ON pdf.id = pd.id_plan_factory
                   LEFT JOIN plan pl ON pl.id = pdf.id_plan
                   LEFT JOIN factory f ON pdf.id_factory = f.id
                   LEFT JOIN project p ON f.id_project = p.id
                   LEFT JOIN user_staff us ON f.id_user_staff = us.id
                   LEFT JOIN subject_facility sf ON p.id_subject_facility = sf.id
                   LEFT JOIN subject s ON sf.id_subject = s.id
                   WHERE
                       f.id IN (
                            SELECT id_factory
                            FROM user_student_factory
                            WHERE id_user_student = :#{#request.idStudent}
                        )
                    AND pd.start_date BETWEEN UNIX_TIMESTAMP(CURDATE()) * 1000 AND :#{#request.max}
            ORDER BY pd.start_date
            """, countQuery = """
            SELECT COUNT(*) FROM plan_date pd
            LEFT JOIN plan_factory pdf ON pdf.id = pd.id_plan_factory
                   LEFT JOIN plan pl ON pl.id = pdf.id_plan
                   LEFT JOIN factory f ON pdf.id_factory = f.id
                   LEFT JOIN project p ON f.id_project = p.id
                   LEFT JOIN user_staff us ON f.id_user_staff = us.id
                   LEFT JOIN subject_facility sf ON p.id_subject_facility = sf.id
                   LEFT JOIN subject s ON sf.id_subject = s.id
            WHERE
                f.id IN (
                    SELECT id_factory
                    FROM user_student_factory
                    WHERE id_user_student = :#{#request.idStudent}
                )
            AND pd.start_date BETWEEN UNIX_TIMESTAMP(CURDATE()) AND :#{#request.max}
            """, nativeQuery = true)
    Page<Student_ScheduleAttendanceResponse> getAllListAttendanceByUser(Pageable pageable,
            @Param("request") Student_ScheduleAttendanceSearchRequest request);

}
