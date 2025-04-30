package udpm.hn.studentattendance.core.teacher.factory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCStudentFactoryRequest;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCStudentFactoryResponse;
import udpm.hn.studentattendance.repositories.UserStudentFactoryRepository;

@Repository
public interface TCStudentFactoryExtendRepository extends UserStudentFactoryRepository {
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

}
