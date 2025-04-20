package udpm.hn.studentattendance.core.staff.student.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import udpm.hn.studentattendance.core.staff.student.model.request.Staff_StudentRequest;
import udpm.hn.studentattendance.core.staff.student.model.response.Staff_StudentResponse;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.repositories.UserStudentRepository;

import java.util.Optional;

@Repository
public interface Staff_StudentExtendRepository extends UserStudentRepository {
    @Query(
            value = """
                    SELECT
                         ROW_NUMBER() OVER (ORDER BY us.createdAt DESC) AS rowNumber,
                         us.id as studentId,
                         us.name as studentName,
                         us.code as studentCode,
                         us.email as studentEmail,
                         us.status as studentStatus
                    FROM UserStudent us
                    LEFT JOIN us.facility f ON us.facility.id = f.id
                    WHERE 
                         (
                           (:#{#studentRequest.searchQuery} IS NULL OR trim(:#{#studentRequest.searchQuery}) = '')
                           OR us.id LIKE concat('%', trim(:#{#studentRequest.searchQuery}), '%')
                           OR us.name LIKE concat('%', trim(:#{#studentRequest.searchQuery}), '%')
                           OR us.code LIKE concat('%', trim(:#{#studentRequest.searchQuery}), '%')
                           OR us.email LIKE concat('%', trim(:#{#studentRequest.searchQuery}), '%')
                         )
                         AND f.id = :facilityId
                         AND (:#{#studentRequest.studentStatus} IS NULL OR us.status = :#{#studentRequest.studentStatus})
                    ORDER BY us.createdAt DESC
                    """,
            countQuery = """
                    SELECT COUNT(us)
                    FROM UserStudent us
                    LEFT JOIN us.facility f ON us.facility.id = f.id        
                    WHERE 
                         (
                           (:#{#studentRequest.searchQuery} IS NULL OR trim(:#{#studentRequest.searchQuery}) = '')
                           OR us.id LIKE concat('%', trim(:#{#studentRequest.searchQuery}), '%')
                           OR us.name LIKE concat('%', trim(:#{#studentRequest.searchQuery}), '%')
                           OR us.code LIKE concat('%', trim(:#{#studentRequest.searchQuery}), '%')
                           OR us.email LIKE concat('%', trim(:#{#studentRequest.searchQuery}), '%')
                         )
                         AND f.id = :facilityId
                         AND (:#{#studentRequest.studentStatus} IS NULL OR us.status = :#{#studentRequest.studentStatus})
                    """
    )
    Page<Staff_StudentResponse> getAllStudentByFacility(Pageable pageable, Staff_StudentRequest studentRequest, String facilityId);

    @Query(value =
            """
                    SELECT 
                    us
                    FROM 
                    UserStudent us
                    WHERE 
                    us.id = :studentId
                     """)
    Optional<UserStudent> getStudentById(String studentId);

    Optional<UserStudent> getUserStudentByCode(String studentCode);

    Optional<UserStudent> getUserStudentByEmail(String email);

}
