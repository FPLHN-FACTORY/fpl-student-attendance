package udpm.hn.studentattendance.core.staff.student.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import udpm.hn.studentattendance.core.staff.student.model.request.USStudentRequest;
import udpm.hn.studentattendance.core.staff.student.model.response.USStudentResponse;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.repositories.UserStudentRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface USStudentExtendRepository extends UserStudentRepository {
    @Query(value = """
            SELECT
                 us.id as studentId,
                 us.name as studentName,
                 us.code as studentCode,
                 us.email as studentEmail,
                 us.status as studentStatus
            FROM user_student us
            LEFT JOIN facility f ON f.id = us.id_facility
            WHERE
                 (
                   (:#{#studentRequest.searchQuery} IS NULL OR TRIM(:#{#studentRequest.searchQuery}) = '')
                   OR us.name LIKE concat('%', TRIM(:#{#studentRequest.searchQuery}), '%')
                   OR us.code LIKE concat('%', TRIM(:#{#studentRequest.searchQuery}), '%')
                   OR us.email LIKE concat('%', TRIM(:#{#studentRequest.searchQuery}), '%')
                 )
                 AND f.id = :facilityId
                 AND (:#{#studentRequest.studentStatus} IS NULL OR us.status = :#{#studentRequest.studentStatus})
            ORDER BY  us.status DESC, us.created_at DESC
            """, countQuery = """
            SELECT COUNT(*)
            FROM user_student us
            LEFT JOIN facility f ON f.id = us.id_facility
            WHERE
                 (
                   (:#{#studentRequest.searchQuery} IS NULL OR TRIM(:#{#studentRequest.searchQuery}) = '')
                   OR us.name LIKE concat('%', TRIM(:#{#studentRequest.searchQuery}), '%')
                   OR us.code LIKE concat('%', TRIM(:#{#studentRequest.searchQuery}), '%')
                   OR us.email LIKE concat('%', TRIM(:#{#studentRequest.searchQuery}), '%')
                 )
                 AND f.id = :facilityId
                 AND (:#{#studentRequest.studentStatus} IS NULL OR us.status = :#{#studentRequest.studentStatus})
            """, nativeQuery = true)
    Page<USStudentResponse> getAllStudentByFacility(Pageable pageable, USStudentRequest studentRequest,
                                                    String facilityId);
    @Query(value = """
            SELECT
                 us.id as studentId,
                 us.name as studentName,
                 us.code as studentCode,
                 us.email as studentEmail,
                 us.status as studentStatus
            FROM user_student us
            LEFT JOIN facility f ON f.id = us.id_facility
            WHERE
                 f.id = :facilityId
            ORDER BY us.status DESC, us.created_at DESC
            """, nativeQuery = true)
    List<USStudentResponse> exportAllStudent(String facilityId);

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
            CAST(us.id AS CHAR(50)) as studentId,
            CASE
            WHEN us.face_embedding IS NOT NULL THEN 1
            ELSE 0
        END as hasFace
    FROM user_student us
    WHERE us.id_facility = :facilityId
    ORDER BY us.created_at DESC
    """, nativeQuery = true)
    List<Map<String, Object>> existFaceForAllStudents(String facilityId);
}



