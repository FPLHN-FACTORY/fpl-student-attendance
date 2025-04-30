package udpm.hn.studentattendance.core.staff.factory.repository.userstudentfactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.factory.model.request.userstudentfactory.USPDDetailShiftByStudentRequest;
import udpm.hn.studentattendance.core.staff.factory.model.request.userstudentfactory.USStudentFactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.model.response.userstudentfactory.Staff_DetailUserStudentFactory;
import udpm.hn.studentattendance.core.staff.factory.model.response.userstudentfactory.Staff_PDDetailShiftByStudentResponse;
import udpm.hn.studentattendance.core.staff.factory.model.response.userstudentfactory.Staff_StudentFactoryResponse;
import udpm.hn.studentattendance.entities.UserStudentFactory;
import udpm.hn.studentattendance.repositories.UserStudentFactoryRepository;

import java.util.Optional;

@Repository
public interface USStudentFactoryRepository extends UserStudentFactoryRepository {

    @Query(value = """
            SELECT
                usf.id AS studentFactoryId,
                us.id AS studentId,
                ft.id AS factoryId,
                us.code AS studentCode,
                us.name AS studentName,
                us.email AS studentEmail,
                usf.status AS statusStudentFactory,
                ROW_NUMBER() OVER (ORDER BY usf.created_at DESC) AS rowNumber
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
            ORDER BY usf.created_at DESC
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
    Page<Staff_StudentFactoryResponse> getUserStudentInFactory(Pageable pageable, String factoryId,
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
    Optional<Staff_DetailUserStudentFactory> getDetailUserStudent(String userStudentId);

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
                WHERE 
                    (:#{#request.startDate} IS NULL OR (
                        DAY(FROM_UNIXTIME(pd.start_date / 1000)) = DAY(FROM_UNIXTIME(:#{#request.startDate} / 1000)) AND
                        MONTH(FROM_UNIXTIME(pd.start_date / 1000)) = MONTH(FROM_UNIXTIME(:#{#request.startDate} / 1000)) AND
                        YEAR(FROM_UNIXTIME(pd.start_date / 1000)) = YEAR(FROM_UNIXTIME(:#{#request.startDate} / 1000))
                    )) AND
                    (:#{#request.status} IS NULL OR (
                        CASE
                            WHEN UNIX_TIMESTAMP(NOW()) * 1000 > pd.end_date
                            THEN 'DA_DIEN_RA'
                            ELSE 'CHUA_DIEN_RA'
                        END
                    ) = :#{#request.status})
                    AND pd.start_date > UNIX_TIMESTAMP(NOW()) * 1000
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
                WHERE 
                    (:#{#request.startDate} IS NULL OR (
                        DAY(FROM_UNIXTIME(pd.start_date / 1000)) = DAY(FROM_UNIXTIME(:#{#request.startDate} / 1000)) AND
                        MONTH(FROM_UNIXTIME(pd.start_date / 1000)) = MONTH(FROM_UNIXTIME(:#{#request.startDate} / 1000)) AND
                        YEAR(FROM_UNIXTIME(pd.start_date / 1000)) = YEAR(FROM_UNIXTIME(:#{#request.startDate} / 1000))
                    )) AND
                    (:#{#request.status} IS NULL OR (
                        CASE
                            WHEN UNIX_TIMESTAMP(NOW()) * 1000 > pd.end_date
                            THEN 'DA_DIEN_RA'
                            ELSE 'CHUA_DIEN_RA'
                        END
                    ) = :#{#request.status})
                    AND pd.start_date > UNIX_TIMESTAMP(NOW()) * 1000 
                    AND us.id = :userStudentId
            """, nativeQuery = true)
    Page<Staff_PDDetailShiftByStudentResponse> getAllPlanDateByStudent(Pageable pageable, USPDDetailShiftByStudentRequest request, String userStudentId);
}
