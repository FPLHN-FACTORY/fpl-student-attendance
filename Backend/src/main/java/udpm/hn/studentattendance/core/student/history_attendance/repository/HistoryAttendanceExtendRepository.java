package udpm.hn.studentattendance.core.student.history_attendance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.student.history_attendance.model.request.HistoryAttendanceRequest;
import udpm.hn.studentattendance.core.student.history_attendance.model.response.HistoryAttendanceResponse;
import udpm.hn.studentattendance.repositories.FactoryRepository;

@Repository
public interface HistoryAttendanceExtendRepository extends FactoryRepository {

    @Query(
            value = """
                    SELECT
                        ROW_NUMBER() OVER (PARTITION BY ft.id ORDER BY pd.start_date ASC) AS rowNumber,
                        usf.id_user_student AS userStudentFactoryId,
                        usf.id_factory AS factoryId,
                        ft.name AS factoryName,
                        p.name AS projectName,
                        pd.start_date AS planDateStartDate,
                        pd.shift AS planDateShift,
                        s.id AS semesterId,
                        CASE
                            WHEN UNIX_TIMESTAMP(NOW()) * 1000 < (pd.start_date + 7200) THEN 'CHUA_DIEN_RA'
                            WHEN ad.id_user_student IS NOT NULL THEN 'CO_MAT'
                            ELSE 'VANG_MAT'
                        END AS statusAttendance,
                        pl.name AS planDateName,
                        pd.description AS planDateDescription,
                        pd.late_arrival AS lateArrival
                    FROM
                        user_student us 
                        LEFT JOIN user_student_factory usf ON usf.id_user_student = us.id
                        LEFT JOIN factory ft ON usf.id_factory = ft.id
                        LEFT JOIN attendance ad ON ad.id_user_student = us.id 
                        LEFT JOIN project p ON ft.id_project = p.id
                        LEFT JOIN semester s ON p.id_semester = s.id
                        LEFT JOIN plan pl ON pl.id_project = p.id
                        LEFT JOIN plan_factory pf ON pf.id_factory = ft.id
                        LEFT JOIN plan_date pd ON pd.id_plan_factory = pf.id
                    WHERE
                        us.id = :userStudentId
                        AND ft.status = 1
                        AND pd.status = 1
                        AND pf.status = 1
                        AND pl.status = 1
                        AND p.status = 1
                        AND usf.status = 1
                        AND s.status = 1
                        AND (:#{#attendanceRequest.semesterId} IS NULL OR s.id = :#{#attendanceRequest.semesterId})
                        AND (:#{#attendanceRequest.factoryId} IS NULL OR ft.id = :#{#attendanceRequest.factoryId})
                    ORDER BY ft.id ASC, pd.start_date ASC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM
                        user_student us 
                        LEFT JOIN user_student_factory usf ON usf.id_user_student = us.id
                        LEFT JOIN factory ft ON usf.id_factory = ft.id
                        LEFT JOIN attendance ad ON ad.id_user_student = us.id 
                        LEFT JOIN project p ON ft.id_project = p.id
                        LEFT JOIN semester s ON p.id_semester = s.id
                        LEFT JOIN plan pl ON pl.id_project = p.id
                        LEFT JOIN plan_factory pf ON pf.id_factory = ft.id
                        LEFT JOIN plan_date pd ON pd.id_plan_factory = pf.id
                    WHERE
                        us.id = :userStudentId
                        AND ft.status = 1
                        AND pd.status = 1
                        AND pf.status = 1
                        AND pl.status = 1
                        AND p.status = 1
                        AND s.status = 1
                        AND usf.status = 1
                        AND (:#{#attendanceRequest.semesterId} IS NULL OR s.id = :#{#attendanceRequest.semesterId})
                        AND (:#{#attendanceRequest.factoryId} IS NULL OR ft.id = :#{#attendanceRequest.factoryId})
                    """,
            nativeQuery = true
    )
    Page<HistoryAttendanceResponse> getAllFactoryAttendance(
            String userStudentId,
            Pageable pageable,
            HistoryAttendanceRequest attendanceRequest
    );
}
