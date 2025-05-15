package udpm.hn.studentattendance.core.teacher.factory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCPlanDateStudentFactoryRequest;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCStudentFactoryRequest;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCPlanDateStudentFactoryResponse;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCStudentFactoryResponse;
import udpm.hn.studentattendance.repositories.UserStudentFactoryRepository;

import java.util.List;

@Repository
public interface TCStudentFactoryExtendRepository extends UserStudentFactoryRepository {
    @Query(value = """
            WITH cte_total_current_shift AS (
                SELECT COUNT(DISTINCT pd.id) AS total_shift
                FROM plan_date pd
                JOIN plan_factory pf ON pd.id_plan_factory = pf.id
                WHERE
                    pd.status = 1 AND
                    pf.status = 1 AND
                    pd.id_plan_factory = pf.id AND
                    pf.id_factory = :factoryId AND
                    pd.end_date <= UNIX_TIMESTAMP(NOW()) * 1000
            ),
            cte_total_shift AS (
                SELECT COUNT(DISTINCT pd.id) AS total_shift
                FROM plan_date pd
                JOIN plan_factory pf ON pd.id_plan_factory = pf.id
                WHERE
                    pd.status = 1 AND
                    pf.status = 1 AND
                    pd.id_plan_factory = pf.id AND
                    pf.id_factory = :factoryId
            )
            SELECT
                usf.id AS studentFactoryId,
                us.id AS studentId,
                ft.id AS factoryId,
                us.code AS studentCode,
                us.name AS studentName,
                us.email AS studentEmail,
                usf.status AS statusStudentFactory,
                cte_ts.total_shift AS totalShift,
                (cte_tcs.total_shift - (
                        SELECT COUNT(a.id)
                        FROM attendance a
                        JOIN plan_date pd ON a.id_plan_date = pd.id
                        JOIN plan_factory pf ON pd.id_plan_factory = pf.id
                        WHERE
                            pd.status = 1 AND
                            pf.status = 1 AND
                            a.attendance_status = 3 AND
                            pf.id_factory = ft.id AND
                            a.id_user_student = usf.id_user_student
                    )
                ) AS totalAbsentShift,
                ROW_NUMBER() OVER (ORDER BY usf.created_at DESC) AS rowNumber
            FROM user_student_factory usf
            CROSS JOIN cte_total_current_shift cte_tcs
            CROSS JOIN cte_total_shift cte_ts
            LEFT JOIN user_student us ON us.id = usf.id_user_student
            LEFT JOIN factory ft ON ft.id = usf.id_factory
            WHERE
                ft.id = :factoryId
                AND usf.status = 1
                AND ft.status = 1
                AND us.status = 1
                AND (:#{#studentRequest.status} IS NULL OR usf.status = :#{#studentRequest.status})
                AND (
                    :#{#studentRequest.searchQuery} IS NULL OR :#{#studentRequest.searchQuery} = ''
                    OR us.code LIKE CONCAT('%', :#{#studentRequest.searchQuery}, '%')
                    OR us.name LIKE CONCAT('%', :#{#studentRequest.searchQuery}, '%')
                    OR us.email LIKE CONCAT('%', :#{#studentRequest.searchQuery}, '%')
                )
            ORDER BY usf.created_at DESC
            """, countQuery = """
            SELECT COUNT(DISTINCT usf.id)
            FROM user_student_factory usf
            LEFT JOIN user_student us ON us.id = usf.id_user_student
            LEFT JOIN factory ft ON ft.id = usf.id_factory
            WHERE
                ft.id = :factoryId
                AND usf.status = 1
                AND ft.status = 1
                AND us.status = 1
                AND (:#{#studentRequest.status} IS NULL OR usf.status = :#{#studentRequest.status})
                AND (
                    :#{#studentRequest.searchQuery} IS NULL OR :#{#studentRequest.searchQuery} = ''
                    OR us.code LIKE CONCAT('%', :#{#studentRequest.searchQuery}, '%')
                    OR us.name LIKE CONCAT('%', :#{#studentRequest.searchQuery}, '%')
                    OR us.email LIKE CONCAT('%', :#{#studentRequest.searchQuery}, '%')
                )

            """, nativeQuery = true)
    Page<TCStudentFactoryResponse> getUserStudentInFactory(Pageable pageable, String factoryId,
                                                           TCStudentFactoryRequest studentRequest);

    @Query(value = """
        SELECT
            ROW_NUMBER() OVER (ORDER BY pd.start_date ASC) AS orderNumber,
            pd.id,
            pd.start_date,
            pd.end_date,
            pd.shift,
            pd.required_checkin,
            pd.required_checkout,
            COALESCE(a.attendance_status, 0) AS status,
            COALESCE(a.created_at, 0) AS createdAt,
            COALESCE(a.updated_at, 0) AS updatedAt
        FROM plan_date pd
        JOIN plan_factory pf ON pd.id_plan_factory = pf.id
        LEFT JOIN attendance a ON pd.id = a.id_plan_date AND a.id_user_student = :#{#request.idUserStudent}
        WHERE
            pd.status = 1 AND
            pf.status = 1 AND
            pf.id_factory = :#{#request.idFactory} AND
            EXISTS(
                SELECT 1
                FROM plan p
                JOIN factory f ON f.id = pf.id_factory
                JOIN user_student_factory usf ON f.id = usf.id_factory
                JOIN user_student us ON usf.id_user_student = us.id
                JOIN project pj ON f.id_project = pj.id
                JOIN subject_facility sf ON sf.id = pj.id_subject_facility
                JOIN subject s2 ON s2.id = sf.id_subject
                JOIN facility f2 ON sf.id_facility = f2.id
                JOIN semester s ON pj.id_semester = s.id
                WHERE
                     pf.id_plan = p.id AND
                     f.status = 1 AND
                     us.status = 1 AND
                     usf.status = 1 AND
                     p.status = 1 AND
                     pj.status = 1 AND
                     s.status = 1 AND
                     s2.status = 1 AND
                     f2.status = 1 AND
                     sf.status = 1 AND
                     us.id = :#{#request.idUserStudent}
            )
        ORDER BY pd.start_date ASC
    """, nativeQuery = true)
    List<TCPlanDateStudentFactoryResponse> getAllPlanDateAttendanceByIdStudent(TCPlanDateStudentFactoryRequest request);

    @Query(value = """
        SELECT
            ROW_NUMBER() OVER (ORDER BY us.name ASC) AS orderNumber,
            pd.id,
            us.name,
            us.code,
            pd.start_date,
            pd.end_date,
            pd.shift,
            pd.required_checkin,
            pd.required_checkout,
            COALESCE(a.attendance_status, 0) AS status,
            COALESCE(a.created_at, 0) AS createdAt,
            COALESCE(a.updated_at, 0) AS updatedAt
        FROM plan_date pd
        JOIN plan_factory pf ON pd.id_plan_factory = pf.id
        JOIN user_student_factory usf ON pf.id_factory = usf.id_factory
        JOIN user_student us ON usf.id_user_student = us.id
        LEFT JOIN attendance a ON pd.id = a.id_plan_date AND a.id_user_student = us.id
        WHERE
            pd.status = 1 AND
            pf.status = 1 AND
            us.status = 1 AND
            usf.status = 1 AND
            pf.id_factory = :idFactory AND
            EXISTS(
                SELECT 1
                FROM plan p
                JOIN factory f ON f.id = pf.id_factory
                JOIN project pj ON f.id_project = pj.id
                JOIN subject_facility sf ON sf.id = pj.id_subject_facility
                JOIN subject s2 ON s2.id = sf.id_subject
                JOIN facility f2 ON sf.id_facility = f2.id
                JOIN semester s ON pj.id_semester = s.id
                WHERE
                     pf.id_plan = p.id AND
                     f.status = 1 AND
                     p.status = 1 AND
                     pj.status = 1 AND
                     s.status = 1 AND
                     s2.status = 1 AND
                     f2.status = 1 AND
                     sf.status = 1
            )
        ORDER BY us.name ASC
    """, nativeQuery = true)
    List<TCPlanDateStudentFactoryResponse> getAllPlanDateAttendanceByIdFactory(String idFactory);

}
