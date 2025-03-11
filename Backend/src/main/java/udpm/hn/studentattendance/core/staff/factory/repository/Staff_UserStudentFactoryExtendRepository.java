package udpm.hn.studentattendance.core.staff.factory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.factory.model.request.Staff_UserStudentRequest;
import udpm.hn.studentattendance.core.staff.factory.model.response.Staff_UserStudentResponse;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.UserStudentRepository;

import java.util.List;

@Repository
public interface Staff_UserStudentFactoryExtendRepository extends UserStudentRepository {
    @Query(value =
            """
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
                    """, nativeQuery = true
    )
    List<Staff_UserStudentResponse> getAllUserStudentExistFactory(
            String facilityId,
            String factoryId);

    @Query(
            value = """
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
                    """,
            countQuery = """
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
                    """
    )
    Page<UserStudent> getAllUserStudent(Pageable pageable,
                                        String facilityId,
                                        EntityStatus userStatus,
                                        Staff_UserStudentRequest userStudentRequest);
}
