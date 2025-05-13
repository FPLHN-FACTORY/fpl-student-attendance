package udpm.hn.studentattendance.core.staff.student.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import udpm.hn.studentattendance.core.staff.student.model.request.USStudentRequest;
import udpm.hn.studentattendance.core.staff.student.model.response.USStudentResponse;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.repositories.UserStudentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface USStudentExtendRepository extends UserStudentRepository {
    @Query(value = """
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
            """, countQuery = """
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
            """)
    Page<USStudentResponse> getAllStudentByFacility(Pageable pageable, USStudentRequest studentRequest,
                                                    String facilityId);

    @Query(value = """
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

    @Query(value = """
            SELECT CASE WHEN EXISTS (
                SELECT 1
                FROM user_student
                WHERE code = :newCode
                AND code != :currentCode
            ) THEN 'TRUE' ELSE 'FALSE' END
            """, nativeQuery = true)
    boolean isExistCodeUpdate(String newCode, String currentCode);

    @Query(value = """
            SELECT CASE WHEN EXISTS (
                SELECT 1
                FROM user_student us
                WHERE us.email = :newEmail
                AND us.email != :currentEmail
                    ) THEN 'TRUE' ELSE 'FALSE' END
            """, nativeQuery = true)
    boolean isExistEmailFeUpdate(String newEmail, String currentEmail);


    @Query(value = """
                           SELECT
                                CASE
                                  WHEN us.face_embedding IS NOT NULL THEN TRUE
                                  ELSE FALSE
                                END
                              FROM user_student us
                          WHERE us.id_facility = :facilityId
                          ORDER BY us.created_at DESC
            """, nativeQuery = true)
    List<Integer> existFaceForAllStudents(String facilityId);
}



