package udpm.hn.studentattendance.core.staff.studentfactory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.studentfactory.model.request.USPDDetailShiftByStudentRequest;
import udpm.hn.studentattendance.core.staff.studentfactory.model.request.USStudentFactoryRequest;
import udpm.hn.studentattendance.core.staff.studentfactory.model.response.STDetailUserStudentFactory;
import udpm.hn.studentattendance.core.staff.studentfactory.model.response.STPDDetailShiftByStudentResponse;
import udpm.hn.studentattendance.core.staff.studentfactory.model.response.STStudentFactoryResponse;
import udpm.hn.studentattendance.entities.UserStudentFactory;
import udpm.hn.studentattendance.repositories.UserStudentFactoryRepository;

import java.util.Optional;

@Repository
public interface USStudentFactoryExtendRepository extends UserStudentFactoryRepository {

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
                    pd.start_date <= UNIX_TIMESTAMP(NOW()) * 1000
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
                us.name AS studentName,
                us.email AS studentEmail,
                usf.status AS statusStudentFactory,
                ROW_NUMBER() OVER (ORDER BY usf.created_at DESC) AS rowNumber
            FROM user_student_factory usf
            LEFT JOIN user_student us ON us.id = usf.id_user_student
            LEFT JOIN factory ft ON ft.id = usf.id_factory
            CROSS JOIN cte_total_current_shift cte_tcs
            CROSS JOIN cte_total_shift cte_ts
            WHERE
                ft.id = :factoryId
                AND ft.status = 1
                AND us.status = 1
                AND (:#{#studentFactoryRequest.status} IS NULL OR usf.status = :#{#studentFactoryRequest.status})
                AND (
                    :#{#studentFactoryRequest.searchQuery} IS NULL OR :#{#studentFactoryRequest.searchQuery} = ''
                    OR us.code LIKE CONCAT('%', :#{#studentFactoryRequest.searchQuery}, '%')
                    OR us.name LIKE CONCAT('%', :#{#studentFactoryRequest.searchQuery}, '%')
                    OR us.email LIKE CONCAT('%', :#{#studentFactoryRequest.searchQuery}, '%')
                )
            ORDER BY usf.created_at DESC, usf.status DESC 
            """, countQuery = """
            SELECT COUNT(DISTINCT usf.id)
            FROM user_student_factory usf
            LEFT JOIN user_student us ON us.id = usf.id_user_student
            LEFT JOIN factory ft ON ft.id = usf.id_factory
            WHERE
                ft.id = :factoryId
                AND ft.status = 1
                AND us.status = 1
                AND (:#{#studentFactoryRequest.status} IS NULL OR usf.status = :#{#studentFactoryRequest.status})
                AND (
                    :#{#studentFactoryRequest.searchQuery} IS NULL OR :#{#studentFactoryRequest.searchQuery} = ''
                    OR us.code LIKE CONCAT('%', :#{#studentFactoryRequest.searchQuery}, '%')
                    OR us.name LIKE CONCAT('%', :#{#studentFactoryRequest.searchQuery}, '%')
                    OR us.email LIKE CONCAT('%', :#{#studentFactoryRequest.searchQuery}, '%')
                )

            """, nativeQuery = true)
    Page<STStudentFactoryResponse> getUserStudentInFactory(Pageable pageable, String factoryId,
                                                           USStudentFactoryRequest studentFactoryRequest);

    Optional<UserStudentFactory> getUserStudentFactoriesByUserStudentIdAndFactoryId(String userStudentId,
                                                                                    String factoryId);

    @Query(value = """
            SELECT
            us.id,
            us.code as userStudentCode,
            us.name as userStudentName,
            usf.status as userStudentStatus,
            s.from_date as startDate,
            s.to_date as endDate,
            s.code as semesterCode 
            FROM user_student_factory usf
            LEFT JOIN factory ft ON ft.id = usf.id_factory
            LEFT JOIN user_student us ON us.id = usf.id_user_student
            LEFT JOIN plan_factory pf ON pf.id_factory = ft.id
            LEFT JOIN plan_date pd ON pd.id_plan_factory = pf.id
            LEFT JOIN facility f ON f.id = us.id_facility
            LEFT JOIN project p ON ft.id_project = p.id
            LEFT JOIN semester s ON s.id = p.id_semester
            WHERE us.id = :userStudentId
            AND s.id = p.id_semester
            LIMIT 1
            """
            , nativeQuery = true
    )
    Optional<STDetailUserStudentFactory> getDetailUserStudent(String userStudentId);

    @Query(value = """
                SELECT 
                    ROW_NUMBER() OVER (ORDER BY pd.start_date ASC) as orderNumber,
                    pd.id,
                    pd.start_date,
                    pd.end_date,
                    pd.shift,
                    pd.type,
                    pd.late_arrival,
                    pd.description,
                    pd.link,
                    pd.required_location,
                    pd.required_ip,
                    CASE
                            WHEN UNIX_TIMESTAMP(NOW()) * 1000 > pd.start_date + pd.late_arrival
                            THEN COALESCE(a.attendance_status, 0)
                            ELSE NULL
                    END AS statusAttendance,
                    CASE
                        WHEN UNIX_TIMESTAMP(NOW()) * 1000 > pd.start_date AND UNIX_TIMESTAMP(NOW()) * 1000 < pd.end_date
                        THEN 'DANG_DIEN_RA'
                        WHEN UNIX_TIMESTAMP(NOW()) * 1000 > pd.end_date 
                        THEN 'DA_DIEN_RA'
                        ELSE 'CHUA_DIEN_RA'
                    END AS status
                FROM plan_date pd
                JOIN plan_factory pf ON pd.id_plan_factory = pf.id
                JOIN factory f ON f.id = pf.id_factory
                JOIN project p ON p.id = f.id_project
                JOIN subject_facility sf ON sf.id = p.id_subject_facility
                JOIN user_student_factory usf ON usf.id_factory = f.id
                JOIN user_student us ON us.id = usf.id_user_student
                LEFT JOIN attendance a ON pd.id = a.id_plan_date AND a.id_user_student = usf.id_user_student
                WHERE 
                    (:#{#request.startDate} IS NULL OR (
                        DAY(FROM_UNIXTIME(pd.start_date / 1000)) = DAY(FROM_UNIXTIME(:#{#request.startDate} / 1000)) AND
                        MONTH(FROM_UNIXTIME(pd.start_date / 1000)) = MONTH(FROM_UNIXTIME(:#{#request.startDate} / 1000)) AND
                        YEAR(FROM_UNIXTIME(pd.start_date / 1000)) = YEAR(FROM_UNIXTIME(:#{#request.startDate} / 1000))
                    )) AND
                    (:#{#request.status} IS NULL OR (
                        CASE
                            WHEN UNIX_TIMESTAMP(NOW()) * 1000 > pd.start_date
                            THEN 'DA_DIEN_RA'
                            ELSE 'CHUA_DIEN_RA'
                        END
                    ) = :#{#request.status})
                    AND us.id = :userStudentId
                ORDER BY pd.start_date ASC
            """, countQuery = """
                SELECT 
                    COUNT(pd.id)
                FROM plan_date pd
                JOIN plan_factory pf ON pd.id_plan_factory = pf.id
                JOIN factory f ON f.id = pf.id_factory
                JOIN project p ON p.id = f.id_project
                JOIN subject_facility sf ON sf.id = p.id_subject_facility
                JOIN user_student_factory usf ON usf.id_factory = f.id
                JOIN user_student us ON us.id = usf.id_user_student
                LEFT JOIN attendance a ON pd.id = a.id_plan_date AND a.id_user_student = usf.id_user_student
                WHERE 
                    (:#{#request.startDate} IS NULL OR (
                        DAY(FROM_UNIXTIME(pd.start_date / 1000)) = DAY(FROM_UNIXTIME(:#{#request.startDate} / 1000)) AND
                        MONTH(FROM_UNIXTIME(pd.start_date / 1000)) = MONTH(FROM_UNIXTIME(:#{#request.startDate} / 1000)) AND
                        YEAR(FROM_UNIXTIME(pd.start_date / 1000)) = YEAR(FROM_UNIXTIME(:#{#request.startDate} / 1000))
                    )) AND
                    (:#{#request.status} IS NULL OR (
                        CASE
                            WHEN UNIX_TIMESTAMP(NOW()) * 1000 > pd.start_date 
                            THEN 'DA_DIEN_RA'
                            ELSE 'CHUA_DIEN_RA'
                        END
                    ) = :#{#request.status})
                    AND us.id = :userStudentId
            """, nativeQuery = true)
    Page<STPDDetailShiftByStudentResponse> getAllPlanDateByStudent(Pageable pageable, USPDDetailShiftByStudentRequest request, String userStudentId);
}
