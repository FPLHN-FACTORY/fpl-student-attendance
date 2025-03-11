package udpm.hn.studentattendance.core.staff.factory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.factory.model.request.UserStudentRequest;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.UserStudentRepository;

@Repository
public interface UserStudentFactoryExtendRepository extends UserStudentRepository {
    @Query(
            value = """
                        SELECT 
                            us
                        FROM 
                            UserStudent us
                        LEFT JOIN Facility f ON f.id = us.facility.id
                        WHERE 
                            f.id = :facilityId
                            AND us.status = :userStatus
                            AND (
                                :#{#userStudentRequest.searchQuery} IS NULL OR :#{#userStudentRequest.searchQuery} = ''
                                OR us.id LIKE CONCAT('%', :#{#userStudentRequest.searchQuery}, '%')
                                OR us.code LIKE CONCAT('%', :#{#userStudentRequest.searchQuery}, '%')
                                OR us.name LIKE CONCAT('%', :#{#userStudentRequest.searchQuery}, '%')
                                OR us.email LIKE CONCAT('%', :#{#userStudentRequest.searchQuery}, '%')
                            )
                        ORDER BY us.createdAt desc 
                    """,
            countQuery = """
                        SELECT COUNT(us.id)
                        FROM 
                            UserStudent us
                        LEFT JOIN Facility f ON f.id = us.facility.id
                        WHERE 
                            f.id = :facilityId
                            AND us.status = :userStatus
                            AND (
                                :#{#userStudentRequest.searchQuery} IS NULL OR :#{#userStudentRequest.searchQuery} = ''
                                OR us.id LIKE CONCAT('%', :#{#userStudentRequest.searchQuery}, '%')
                                OR us.code LIKE CONCAT('%', :#{#userStudentRequest.searchQuery}, '%')
                                OR us.name LIKE CONCAT('%', :#{#userStudentRequest.searchQuery}, '%')
                                OR us.email LIKE CONCAT('%', :#{#userStudentRequest.searchQuery}, '%')
                            )
                    """
    )
    Page<UserStudent> getAllUserStudent(Pageable pageable, String facilityId, EntityStatus userStatus, UserStudentRequest userStudentRequest);

}
