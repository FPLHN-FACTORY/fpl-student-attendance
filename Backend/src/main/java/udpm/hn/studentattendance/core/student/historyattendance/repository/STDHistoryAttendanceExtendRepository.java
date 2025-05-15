package udpm.hn.studentattendance.core.student.historyattendance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.student.historyattendance.model.request.STDHistoryAttendanceRequest;
import udpm.hn.studentattendance.core.student.historyattendance.model.response.STDHistoryAttendanceResponse;
import udpm.hn.studentattendance.repositories.FactoryRepository;

import java.util.List;

@Repository
public interface STDHistoryAttendanceExtendRepository extends FactoryRepository {

    @Query(value = """
            SELECT
                ROW_NUMBER() OVER (
                    PARTITION BY ft.id 
                    ORDER BY pd.start_date
                ) AS rowNumber,
                ft.name AS factoryName,
                p.name  AS projectName,
                pd.start_date AS planDateStartDate,
                pd.end_date AS planDateEndDate,
                pd.shift AS planDateShift,
                ft.id AS factoryId,
                CASE
                    WHEN :nowTs < pd.start_date THEN 'CHUA_DIEN_RA'
                    WHEN att.max_status = 2 THEN 'CHECK_IN'
                    WHEN att.max_status = 3 THEN 'CO_MAT'
                    ELSE 'VANG_MAT'
                END AS statusAttendance,
                pl.name AS planDateName,
                pd.description AS planDateDescription,
                pd.late_arrival AS lateArrival
            FROM user_student us
            JOIN user_student_factory usf 
              ON usf.id_user_student = us.id
            JOIN factory ft 
              ON ft.id = usf.id_factory 
             AND ft.status = 1
            JOIN project p 
              ON p.id = ft.id_project 
             AND p.status = 1
            JOIN semester s 
              ON s.id = p.id_semester 
             AND s.status = 1
            JOIN plan pl 
              ON pl.id_project = p.id 
             AND pl.status = 1
            JOIN plan_factory pf 
              ON pf.id_factory = ft.id 
             AND pf.status = 1
            JOIN plan_date pd 
              ON pd.id_plan_factory = pf.id 
             AND pd.status = 1
            LEFT JOIN (
                SELECT
                    at.id_plan_date,
                    MAX(at.attendance_status) AS max_status
                FROM attendance at
                WHERE at.id_user_student = :userStudentId
                GROUP BY at.id_plan_date
            ) att 
              ON att.id_plan_date = pd.id

            WHERE us.id = :userStudentId
              AND (usf.status = 1 OR (usf.status = 0 AND pd.start_date <= usf.updated_at))
              AND (:#{#attendanceRequest.semesterId} IS NULL OR s.id = :#{#attendanceRequest.semesterId})
              AND (:#{#attendanceRequest.factoryId}  IS NULL OR ft.id = :#{#attendanceRequest.factoryId})

            ORDER BY ft.id, pd.start_date
            """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM user_student us
                    JOIN user_student_factory usf 
                      ON usf.id_user_student = us.id
                    JOIN factory ft 
                      ON ft.id = usf.id_factory 
                     AND ft.status = 1
                    JOIN project p 
                      ON p.id = ft.id_project 
                     AND p.status = 1
                    JOIN semester s 
                      ON s.id = p.id_semester 
                     AND s.status = 1
                    JOIN plan pl 
                      ON pl.id_project = p.id 
                     AND pl.status = 1
                    JOIN plan_factory pf 
                      ON pf.id_factory = ft.id 
                     AND pf.status = 1
                    JOIN plan_date pd 
                      ON pd.id_plan_factory = pf.id 
                     AND pd.status = 1
                    WHERE us.id = :userStudentId
                      AND (usf.status = 1 OR (usf.status = 0 AND pd.start_date <= usf.updated_at))
                      AND (:#{#attendanceRequest.semesterId} IS NULL OR s.id = :#{#attendanceRequest.semesterId})
                      AND (:#{#attendanceRequest.factoryId}  IS NULL OR ft.id = :#{#attendanceRequest.factoryId})
                    """,
            nativeQuery = true
    )
    Page<STDHistoryAttendanceResponse> getAllFactoryAttendance(
            String userStudentId,
            Pageable pageable,
            STDHistoryAttendanceRequest attendanceRequest,
            Long nowTs
    );


    @Query(value = """
            SELECT
                ROW_NUMBER() OVER (
                    PARTITION BY ft.id 
                    ORDER BY pd.start_date
                ) AS rowNumber,
                ft.name AS factoryName,
                p.name  AS projectName,
                pd.start_date AS planDateStartDate,
                pd.end_date AS planDateEndDate,
                pd.shift AS planDateShift,
                ft.id AS factoryId,
                CASE
                    WHEN :nowTs < pd.start_date THEN 'CHUA_DIEN_RA'
                    WHEN att.max_status = 2 THEN 'CHECK_IN'
                    WHEN att.max_status = 3 THEN 'CO_MAT'
                    ELSE 'VANG_MAT'
                END AS statusAttendance,
                pl.name AS planDateName,
                pd.description AS planDateDescription,
                pd.late_arrival AS lateArrival
            FROM user_student us
            JOIN user_student_factory usf 
              ON usf.id_user_student = us.id
            JOIN factory ft 
              ON ft.id = usf.id_factory 
             AND ft.status = 1
            JOIN project p 
              ON p.id = ft.id_project 
             AND p.status = 1
            JOIN semester s 
              ON s.id = p.id_semester 
             AND s.status = 1
            JOIN plan pl 
              ON pl.id_project = p.id 
             AND pl.status = 1
            JOIN plan_factory pf 
              ON pf.id_factory = ft.id 
             AND pf.status = 1
            JOIN plan_date pd 
              ON pd.id_plan_factory = pf.id 
             AND pd.status = 1
            LEFT JOIN (
                SELECT
                    at.id_plan_date,
                    MAX(at.attendance_status) AS max_status
                FROM attendance at
                WHERE at.id_user_student = :userStudentId
                GROUP BY at.id_plan_date
            ) att 
              ON att.id_plan_date = pd.id
            WHERE us.id = :userStudentId
            ORDER BY ft.id, pd.start_date ASC
            """, nativeQuery = true)
    List<STDHistoryAttendanceResponse> getAllHistoryAttendanceByFactory(String userStudentId, String factoryId);
}
