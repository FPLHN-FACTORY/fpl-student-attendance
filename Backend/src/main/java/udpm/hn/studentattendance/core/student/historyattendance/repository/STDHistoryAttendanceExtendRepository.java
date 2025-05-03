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
                ROW_NUMBER() OVER (PARTITION BY ft.id ORDER BY pd.start_date ASC) AS rowNumber,
                usf.id_user_student AS userStudentFactoryId,
                usf.id_factory AS factoryId,
                ft.name AS factoryName,
                p.name AS projectName,
                pd.start_date AS planDateStartDate,
                pd.shift AS planDateShift,
                s.id AS semesterId,
                CASE
                    WHEN UNIX_TIMESTAMP(NOW()) * 1000 < pd.start_date THEN 'CHUA_DIEN_RA'
                    WHEN
                    (
                    SELECT
                    COUNT(*) > 0
                    FROM attendance at
                    LEFT JOIN plan_date pdt ON at.id_plan_date = pdt.id
                    WHERE at.id_user_student = us.id
                    AND DATE(FROM_UNIXTIME((pd.start_date + (pd.late_arrival * 60)) / 1000))
                    >= DATE(FROM_UNIXTIME(at.updated_at / 1000))
                    AND pdt.shift = pd.shift
                    AND at.id_plan_date = pd.id
                    AND at.status = 3
                    )
                    THEN 'CO_MAT'
                    ELSE 'VANG_MAT'
                END AS statusAttendance,
                pl.name AS planDateName,
                pd.description AS planDateDescription,
                pd.late_arrival AS lateArrival
            FROM
                user_student us
                LEFT JOIN user_student_factory usf ON usf.id_user_student = us.id
                LEFT JOIN factory ft ON usf.id_factory = ft.id
                LEFT JOIN project p ON ft.id_project = p.id
                LEFT JOIN semester s ON p.id_semester = s.id
                LEFT JOIN plan pl ON pl.id_project = p.id
                LEFT JOIN plan_factory pf ON pf.id_factory = ft.id
                LEFT JOIN plan_date pd ON pd.id_plan_factory = pf.id
                LEFT JOIN attendance ad ON ad.id_plan_date = pd.id
            WHERE
                us.id = :userStudentId
                AND ft.status = 1
                AND pd.status = 1
                AND pf.status = 1
                AND pl.status = 1
                AND p.status = 1
                AND s.status = 1
                AND (usf.status = 1 OR (usf.status = 0 AND pd.start_date <= usf.updated_at))
                AND (:#{#attendanceRequest.semesterId} IS NULL OR s.id = :#{#attendanceRequest.semesterId})
                AND (:#{#attendanceRequest.factoryId} IS NULL OR ft.id = :#{#attendanceRequest.factoryId})
            ORDER BY ft.id ASC, pd.start_date ASC
            """, countQuery = """
            SELECT COUNT(*)
            FROM
                user_student us
                LEFT JOIN user_student_factory usf ON usf.id_user_student = us.id
                LEFT JOIN factory ft ON usf.id_factory = ft.id
                LEFT JOIN project p ON ft.id_project = p.id
                LEFT JOIN semester s ON p.id_semester = s.id
                LEFT JOIN plan pl ON pl.id_project = p.id
                LEFT JOIN plan_factory pf ON pf.id_factory = ft.id
                LEFT JOIN plan_date pd ON pd.id_plan_factory = pf.id
                LEFT JOIN attendance ad ON ad.id_plan_date = pd.id
            WHERE
                us.id = :userStudentId
                AND ft.status = 1
                AND pd.status = 1
                AND pf.status = 1
                AND pl.status = 1
                AND p.status = 1
                AND s.status = 1
                AND (usf.status = 1 OR (usf.status = 0 AND pd.start_date <= usf.updated_at))
                AND (:#{#attendanceRequest.semesterId} IS NULL OR s.id = :#{#attendanceRequest.semesterId})
                AND (:#{#attendanceRequest.factoryId} IS NULL OR ft.id = :#{#attendanceRequest.factoryId})
            """, nativeQuery = true)
    Page<STDHistoryAttendanceResponse> getAllFactoryAttendance(
            String userStudentId,
            Pageable pageable,
            STDHistoryAttendanceRequest attendanceRequest);

    @Query(value = """
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
                    WHEN UNIX_TIMESTAMP(NOW()) * 1000 < pd.end_date THEN 'CHUA_DIEN_RA'
                    WHEN
                    (
                    SELECT
                    COUNT(*) > 0
                    FROM attendance at
                    LEFT JOIN plan_date pdt ON at.id_plan_date = pdt.id
                    WHERE at.id_user_student = us.id
                    AND DATE(FROM_UNIXTIME((pd.start_date + (pd.late_arrival * 60000)) / 1000))
                    >= DATE(FROM_UNIXTIME(at.updated_at / 1000))
                    AND pdt.shift = pd.shift
                    AND at.id_plan_date = pd.id
                    )
                    THEN 'CO_MAT'
                    ELSE 'VANG_MAT'
                END AS statusAttendance,
                pl.name AS planDateName,
                pd.description AS planDateDescription,
                pd.late_arrival AS lateArrival
            FROM
                user_student us
                LEFT JOIN user_student_factory usf ON usf.id_user_student = us.id
                LEFT JOIN factory ft ON usf.id_factory = ft.id
                LEFT JOIN project p ON ft.id_project = p.id
                LEFT JOIN semester s ON p.id_semester = s.id
                LEFT JOIN plan pl ON pl.id_project = p.id
                LEFT JOIN plan_factory pf ON pf.id_factory = ft.id
                LEFT JOIN plan_date pd ON pd.id_plan_factory = pf.id
                LEFT JOIN attendance ad ON ad.id_plan_date = pd.id
            WHERE
                us.id = :userStudentId
                AND ft.status = 1
                AND pd.status = 1
                AND pf.status = 1
                AND pl.status = 1
                AND p.status = 1
                AND s.status = 1
                AND (usf.status = 1 OR (usf.status = 0 AND pd.start_date <= usf.updated_at))
            	AND ft.id = :factoryId
            ORDER BY pd.start_date ASC
            """, nativeQuery = true)
    List<STDHistoryAttendanceResponse> getAllHistoryAttendanceByFactory(String userStudentId, String factoryId);
}
