package udpm.hn.studentattendance.core.staff.factory.repository.userstudentfactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.factory.model.request.userstudentfactory.USUserStudentRequest;
import udpm.hn.studentattendance.core.staff.factory.model.response.userstudentfactory.Staff_UserStudentResponse;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.UserStudentRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface USUSFUserStudentExtendRepository extends UserStudentRepository {
        @Query(value = """
                        SELECT
                            usf.id AS studentFactoryId,
                            us.id AS studentId,
                            ft.id AS factoryId,
                            us.code AS studentCode,
                            us.name AS studentName,
                            us.email AS studentEmail,
                            usf.status AS statusStudentFactory,
                            ROW_NUMBER() OVER (ORDER BY us.created_at DESC) AS rowNumber,
                            (CASE WHEN usf.id IS NOT NULL THEN 'TRUE' ELSE 'FALSE' END) AS checked
                        FROM user_student us
                        LEFT JOIN user_student_factory usf ON usf.id_user_student = us.id
                        LEFT JOIN facility f ON f.id = us.id_facility
                        LEFT JOIN factory ft ON ft.id = usf.id_factory
                        WHERE
                            f.id = :facilityId
                            AND f.status = 1
                            AND ft.id = :factoryId
                            AND ft.status = 1
                            AND us.status = 1
                        ORDER BY us.created_at DESC
                        """, nativeQuery = true)
        List<Staff_UserStudentResponse> getAllUserStudentExistFactory(
                        String facilityId,
                        String factoryId);

        @Query(value = """
                        SELECT us
                        FROM UserStudent us
                        LEFT JOIN Facility f ON f.id = us.facility.id
                        WHERE f.id = :facilityId
                          AND us.status = :userStatus
                          AND (
                              :#{#userStudentRequest.searchQuery} IS NULL
                              OR :#{#userStudentRequest.searchQuery} = ''
                              OR us.id LIKE CONCAT('%', :#{#userStudentRequest.searchQuery}, '%')
                              OR us.code LIKE CONCAT('%', :#{#userStudentRequest.searchQuery}, '%')
                              OR us.name LIKE CONCAT('%', :#{#userStudentRequest.searchQuery}, '%')
                              OR us.email LIKE CONCAT('%', :#{#userStudentRequest.searchQuery}, '%')
                          )
                        ORDER BY us.createdAt DESC
                        """, countQuery = """
                        SELECT COUNT(us)
                        FROM UserStudent us
                        LEFT JOIN Facility f ON f.id = us.facility.id
                        WHERE f.id = :facilityId
                          AND us.status = :userStatus
                          AND (
                              :#{#userStudentRequest.searchQuery} IS NULL
                              OR :#{#userStudentRequest.searchQuery} = ''
                              OR us.id LIKE CONCAT('%', :#{#userStudentRequest.searchQuery}, '%')
                              OR us.code LIKE CONCAT('%', :#{#userStudentRequest.searchQuery}, '%')
                              OR us.name LIKE CONCAT('%', :#{#userStudentRequest.searchQuery}, '%')
                              OR us.email LIKE CONCAT('%', :#{#userStudentRequest.searchQuery}, '%')
                          )
                        """)
        Page<UserStudent> getAllUserStudent(Pageable pageable,
                        String facilityId,
                        EntityStatus userStatus,
                        USUserStudentRequest userStudentRequest);

        @Query(value = """
                        SELECT
                        CASE WHEN COUNT(*) > 20 THEN 'FALSE' ELSE 'TRUE' END
                        FROM
                        user_student_factory usf
                        WHERE
                        usf.id_factory = :factoryId
                        """, nativeQuery = true)
        boolean isStudentGreaterThanTwenty(String factoryId);


    @Query(value = """
                SELECT
                CASE WHEN COUNT(distinct pd2.id) > 0 THEN 'FALSE' ELSE 'TRUE' END
                FROM plan_date pd
                JOIN plan_factory pf ON pf.id = pd.id_plan_factory
                JOIN factory f ON pf.id_factory = f.id
                JOIN user_student_factory usf ON usf.id_factory = f.id
                JOIN plan p ON pf.id_plan = p.id
                JOIN project pj ON f.id_project = pj.id
                JOIN subject_facility sf ON sf.id = pj.id_subject_facility
                JOIN facility f2 ON sf.id_facility = f2.id
                JOIN user_student us ON usf.id_user_student = us.id
                JOIN user_student_factory usf2 ON usf2.id_user_student = us.id
                JOIN plan_factory pf2 ON pf2.id_factory = usf2.id_factory
                JOIN plan_date pd2 ON pd2.id_plan_factory = pf2.id
                WHERE
                    pd.status = 1 AND
                    pf.status = 1 AND
                    p.status = 1 AND
                    f.status = 1 AND
                    f2.status = 1 AND
                    usf.status = 1 AND
                    usf2.status = 1 AND
                    (pd2.start_date < pd.start_date OR pd2.start_date > pd.end_date) AND
                    pd.start_date >= (UNIX_TIMESTAMP(CURRENT_DATE) * 1000) AND
                    f.id <> :idFactory AND
                    usf.id_user_student = :idUserStudent AND
                    f2.id = :idFacility
            """, nativeQuery = true)
    boolean isStudentExistsShift(String idFacility, String idFactory, String idUserStudent);

    Optional<UserStudent> getUserStudentByCode(String code);
}
