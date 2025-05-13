package udpm.hn.studentattendance.core.student.schedule.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.student.schedule.model.request.STDScheduleAttendanceSearchRequest;
import udpm.hn.studentattendance.core.student.schedule.model.response.STDScheduleAttendanceResponse;
import udpm.hn.studentattendance.repositories.FacilityRepository;

import java.util.List;

@Repository
public interface STDScheduleAttendanceRepository extends FacilityRepository {

    @Query(value = """
            SELECT
                       ROW_NUMBER() OVER (ORDER BY pd.start_date ASC) AS indexs,
                       pd.id AS id,
                       pd.start_date AS attendanceDayStart,
                       pd.end_date AS attendanceDayEnd,
                       s.name AS subjectName,
                       us.name AS staffName,
                       pd.type,
                       pd.link,
                       pd.shift AS shift,
                       pd.description AS description,
                       pd.room as location,
                       ft.name as factoryName,
                       CONCAT(p.name, ' - ', lp.name) as projectName
                   FROM
                   plan_date pd
            	   LEFT JOIN plan_factory pdf ON pdf.id = pd.id_plan_factory
                   LEFT JOIN plan pl ON pl.id = pdf.id_plan
                   LEFT JOIN factory ft ON pdf.id_factory = ft.id
                   LEFT JOIN project p ON ft.id_project = p.id
                   LEFT JOIN user_staff us ON ft.id_user_staff = us.id
                   LEFT JOIN subject_facility sf ON p.id_subject_facility = sf.id
                   LEFT JOIN subject s ON sf.id_subject = s.id
                   LEFT JOIN level_project lp ON lp.id = p.id_level_project
                   LEFT JOIN facility f ON f.id = sf.id_facility
                   WHERE
                       ft.id IN (
                            SELECT id_factory
                            FROM user_student_factory
                            WHERE id_user_student = :#{#request.idStudent}
                        )
                    AND pd.start_date BETWEEN :#{#request.now} AND :#{#request.max}
            ORDER BY pd.start_date
            """, countQuery = """
            SELECT COUNT(*) FROM plan_date pd
            LEFT JOIN plan_factory pdf ON pdf.id = pd.id_plan_factory
                   LEFT JOIN plan pl ON pl.id = pdf.id_plan
                   LEFT JOIN factory ft ON pdf.id_factory = ft.id
                   LEFT JOIN project p ON ft.id_project = p.id
                   LEFT JOIN user_staff us ON ft.id_user_staff = us.id
                   LEFT JOIN subject_facility sf ON p.id_subject_facility = sf.id
                   LEFT JOIN subject s ON sf.id_subject = s.id
                   LEFT JOIN facility f ON f.id = sf.id_facility
            WHERE
                ft.id IN (
                    SELECT id_factory
                    FROM user_student_factory
                    WHERE id_user_student = :#{#request.idStudent}
                )
            AND pd.start_date BETWEEN :#{#request.now} AND :#{#request.max}
            """, nativeQuery = true)
    Page<STDScheduleAttendanceResponse> getAllListAttendanceByUser(Pageable pageable,
                                                                   @Param("request") STDScheduleAttendanceSearchRequest request);

    @Query(value = """
            SELECT
                       ROW_NUMBER() OVER (ORDER BY pd.start_date ASC) AS indexs,
                       pd.id AS id,
                       pd.start_date AS attendanceDayStart,
                       pd.end_date AS attendanceDayEnd,
                       s.name AS subjectName,
                       us.name AS staffName,
                       pd.type,
                       pd.link,
                       pd.shift AS shift,
                       pd.description AS description,
                       pd.room as location,
                       ft.name as factoryName,
                       CONCAT(p.name, ' - ', lp.name) as projectName
                   FROM
                   plan_date pd
            	   LEFT JOIN plan_factory pdf ON pdf.id = pd.id_plan_factory
                   LEFT JOIN plan pl ON pl.id = pdf.id_plan
                   LEFT JOIN factory ft ON pdf.id_factory = ft.id
                   LEFT JOIN project p ON ft.id_project = p.id
                   LEFT JOIN user_staff us ON ft.id_user_staff = us.id
                   LEFT JOIN subject_facility sf ON p.id_subject_facility = sf.id
                   LEFT JOIN subject s ON sf.id_subject = s.id
                   LEFT JOIN level_project lp ON lp.id = p.id_level_project
                   LEFT JOIN facility f ON f.id = sf.id_facility
                   WHERE
                       ft.id IN (
                            SELECT id_factory
                            FROM user_student_factory
                            WHERE id_user_student = :#{#request.idStudent}
                        )
                    AND pd.start_date BETWEEN :#{#request.now} AND :#{#request.max}
            ORDER BY pd.start_date
            """, nativeQuery = true)
    List<STDScheduleAttendanceResponse> getAllListAttendanceByUserList(@Param("request") STDScheduleAttendanceSearchRequest request);
}
