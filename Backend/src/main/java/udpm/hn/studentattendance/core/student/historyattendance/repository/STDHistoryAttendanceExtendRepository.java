package udpm.hn.studentattendance.core.student.historyattendance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.student.historyattendance.model.request.STDHistoryAttendanceRequest;
import udpm.hn.studentattendance.core.student.historyattendance.model.response.STDHistoryAttendanceResponse;
import udpm.hn.studentattendance.core.student.historyattendance.model.response.STDHistoryPlanDateAttendanceResponse;
import udpm.hn.studentattendance.repositories.FactoryRepository;

import java.util.List;
import java.util.Map;

@Repository
public interface STDHistoryAttendanceExtendRepository extends FactoryRepository {

    @Query(value = """
             SELECT
                ROW_NUMBER() OVER (
                  ORDER BY pd.start_date DESC
                ) AS rowNumber,
                ft.id                   AS factoryId,
                ft.name                 AS factoryName,
                pd.start_date           AS planDateStartDate,
                pd.end_date             AS planDateEndDate,
                pd.shift                AS shift,
                ft.id                   AS factoryId,
                pd.id                   AS planDateId,
                a.created_at            AS checkIn,
                a.updated_at            AS checkOut,
                pd.required_checkin    AS requiredCheckIn,
                pd.required_checkout   AS requiredCheckOut,
                pd.type,
                CASE
                  WHEN :nowTs < pd.start_date THEN 'CHUA_DIEN_RA'
                  WHEN :nowTs > pd.start_date AND :nowTs < pd.end_date THEN 'DANG_DIEN_RA'
                  WHEN a.attendance_status = 2 THEN 'CHECK_IN'
                  WHEN a.attendance_status = 3 THEN 'CO_MAT'
                  WHEN a.attendance_status = 1 THEN 'VANG_MAT'
                  ELSE 'CHUA_CHECK_OUT'
                END                      AS statusAttendance,
                pd.description          AS planDateDescription,
                pd.late_arrival         AS lateArrival
            FROM plan_date pd
            JOIN plan_factory pf    ON pd.id_plan_factory = pf.id
            JOIN plan pl            ON pf.id_plan        = pl.id
            JOIN project p          ON pl.id_project     = p.id
            JOIN semester s         ON p.id_semester     = s.id
                                   AND s.status = 1
            JOIN factory ft         ON pf.id_factory     = ft.id
            JOIN user_student_factory usf
                                   ON usf.id_factory    = ft.id
                                   AND usf.status = 1
            LEFT JOIN attendance a  ON a.id_plan_date     = pd.id
                                   AND a.id_user_student = usf.id_user_student
                                   AND a.status = 1
            WHERE usf.id_user_student = :userStudentId
              AND (:#{#attendanceRequest.semesterId} IS NULL
                   OR s.id = :#{#attendanceRequest.semesterId})
              AND (:#{#attendanceRequest.factoryId}  IS NULL
                   OR ft.id = :#{#attendanceRequest.factoryId})
            ORDER  BY pd.start_date DESC
            """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM plan_date pd
                    JOIN plan_factory pf    ON pd.id_plan_factory = pf.id
                    JOIN plan pl            ON pf.id_plan        = pl.id
                    JOIN project p          ON pl.id_project     = p.id
                    JOIN semester s         ON p.id_semester     = s.id
                                           AND s.status = 1
                    JOIN factory ft         ON pf.id_factory     = ft.id
                    JOIN user_student_factory usf
                                           ON usf.id_factory    = ft.id AND usf.status = 1
                    WHERE usf.id_user_student = :userStudentId
                      AND (:#{#attendanceRequest.semesterId} IS NULL
                           OR s.id = :#{#attendanceRequest.semesterId})
                      AND (:#{#attendanceRequest.factoryId}  IS NULL
                           OR ft.id = :#{#attendanceRequest.factoryId})
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
            COUNT(*) AS totalShift,
            SUM(CASE WHEN a.attendance_status = 3 THEN 1 ELSE 0 END) AS totalPresent,
            SUM(CASE WHEN pd.start_date <= UNIX_TIMESTAMP(NOW()) * 1000 AND (a.id IS NULL OR a.attendance_status != 3) THEN 1 ELSE 0 END) AS totalAbsent
        FROM plan_date pd
        JOIN plan_factory pf ON pd.id_plan_factory = pf.id
        JOIN plan pl ON pf.id_plan = pl.id
        JOIN project p ON pl.id_project = p.id
        JOIN semester s ON p.id_semester = s.id AND s.status = 1
        JOIN factory ft ON pf.id_factory = ft.id
        JOIN user_student_factory usf ON usf.id_factory = ft.id AND usf.status = 1
        LEFT JOIN attendance a ON a.id_plan_date = pd.id AND a.id_user_student = usf.id_user_student
        WHERE usf.id_user_student = :userStudentId
          AND (:#{#attendanceRequest.semesterId} IS NULL
               OR s.id = :#{#attendanceRequest.semesterId})
          AND (:#{#attendanceRequest.factoryId} IS NULL
               OR ft.id = :#{#attendanceRequest.factoryId})
    """, nativeQuery = true)
    Map<String, Object> getAttendanceSummary(String userStudentId, STDHistoryAttendanceRequest attendanceRequest);

    @Query(value = """
               SELECT
                ROW_NUMBER() OVER (
                  PARTITION BY f.id 
                ) AS rowNumber,
                    f.id AS factoryId,
                    pd.id,
                    pl.id AS planId,
                    pl.name AS planName,
                    pf.id AS factoryId,
                    f.name AS factoryName,
                    pd.required_checkin,
                    pd.required_checkout
                FROM plan_date pd
                JOIN plan_factory pf ON pd.id_plan_factory = pf.id
                JOIN plan pl ON pf.id_plan = pl.id
                JOIN factory f ON pf.id_factory = f.id
                JOIN user_student_factory usf ON usf.id_factory = f.id
                JOIN user_student us ON usf.id_user_student = us.id
                WHERE
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
                            s.status = 1 AND
                            f2.status = 1 AND
                            f2.id = :idFacility
                    ) AND
                    us.id = :idUserStudent
                    ORDER BY pd.start_date
            """, nativeQuery = true)
    List<STDHistoryPlanDateAttendanceResponse> getDetailPlanDate(String idUserStudent, String idFacility);

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
                pd.shift,
                ft.id AS factoryId,
                pd.type,
                CASE
                    WHEN :nowTs < pd.start_date THEN 'CHUA_DIEN_RA'
                    WHEN :nowTs > pd.start_date AND :nowTs < pd.end_date THEN 'DANG_DIEN_RA'
                    WHEN att.max_status = 3 THEN 'CO_MAT'
                    WHEN att.max_status = 1 THEN 'VANG_MAT'
                    ELSE 'CHUA_CHECK_OUT'
                END AS statusAttendance,
                pl.name AS planDateName,
                pd.description AS planDateDescription,
                pd.late_arrival AS lateArrival
            FROM user_student us
            JOIN user_student_factory usf 
              ON usf.id_user_student = us.id
            JOIN factory ft 
              ON ft.id = usf.id_factory 
            JOIN project p 
              ON p.id = ft.id_project 
            JOIN semester s 
              ON s.id = p.id_semester 
             AND s.status = 1
            JOIN plan pl 
              ON pl.id_project = p.id 
            JOIN plan_factory pf 
              ON pf.id_factory = ft.id 
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
            WHERE 
            us.id = :userStudentId AND 
            ft.id = :factoryId
            ORDER BY ft.id, pd.start_date ASC
            """, nativeQuery = true)
    List<STDHistoryAttendanceResponse> getAllHistoryAttendanceByFactory(String userStudentId, String factoryId, Long nowTs);


}
