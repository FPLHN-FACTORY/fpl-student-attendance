package udpm.hn.studentattendance.core.staff.factory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.factory.model.request.Staff_StudentFactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.model.response.Staff_StudentFactoryResponse;
import udpm.hn.studentattendance.entities.UserStudentFactory;
import udpm.hn.studentattendance.repositories.UserStudentFactoryRepository;

import java.util.Optional;

@Repository
public interface Staff_StudentFactoryRepository extends UserStudentFactoryRepository {

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
                    """,
            countQuery =
                    """
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
                                 
                            """,
            nativeQuery = true
    )
    Page<Staff_StudentFactoryResponse> getUserStudentInFactory(Pageable pageable, String factoryId, Staff_StudentFactoryRequest studentFactoryRequest);

    Optional<UserStudentFactory> getUserStudentFactoriesByUserStudentIdAndFactoryId(String userStudentId, String factoryId);




}
