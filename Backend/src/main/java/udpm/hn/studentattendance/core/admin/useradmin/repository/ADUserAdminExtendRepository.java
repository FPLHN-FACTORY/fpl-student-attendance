package udpm.hn.studentattendance.core.admin.useradmin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.useradmin.model.request.ADUserAdminRequest;
import udpm.hn.studentattendance.core.admin.useradmin.model.response.ADUserAdminResponse;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.repositories.UserAdminRepository;

import java.util.Optional;

@Repository
public interface ADUserAdminExtendRepository extends UserAdminRepository {

    @Query(value = """
            SELECT
            	ROW_NUMBER() OVER (ORDER BY ua.created_at DESC) AS rowNumber,
            	ua.id AS userAdminId,
            	ua.code AS userAdminCode,
            	ua.name AS userAdminName,
            	ua.email AS userAdminEmail,
            	ua.status AS userAdminStatus
            FROM user_admin ua
            WHERE
                (trim(:#{#request.searchQuery}) IS NULL
            	OR trim(:#{#request.searchQuery}) = ''
            	OR ua.name LIKE concat('%', trim(:#{#request.searchQuery}), '%')
            	OR ua.code LIKE concat('%', trim(:#{#request.searchQuery}), '%')
            	OR ua.email LIKE concat('%', trim(:#{#request.searchQuery}), '%')
            	)
            	AND (:#{#request.status} IS NULL OR ua.status = :#{#request.status})
            ORDER BY ua.created_at DESC
            """, countQuery = """
                                SELECT COUNT(*)
                                FROM user_admin ua
                                WHERE
                                    (trim(:#{#request.searchQuery}) IS NULL
                                	OR trim(:#{#request.searchQuery}) = ''
                                	OR ua.name LIKE concat('%', trim(:#{#request.searchQuery}), '%')
                                	OR ua.code LIKE concat('%', trim(:#{#request.searchQuery}), '%')
                                	OR ua.email LIKE concat('%', trim(:#{#request.searchQuery}), '%')
                                	AND (:#{#request.status} IS NULL OR ua.status = :#{#request.status})
            """, nativeQuery = true)
    Page<ADUserAdminResponse> getAllUserAdmin(Pageable pageable, ADUserAdminRequest request);

    Optional<UserAdmin> getUserAdminByCode(String code);

    Optional<UserAdmin> getUserAdminByEmail(String email);

    @Query(value = """
            SELECT CASE WHEN EXISTS (
                SELECT 1
                FROM user_admin ua
                WHERE ua.code = :newCode
                AND ua.code != :currentCode
            ) THEN 'TRUE' ELSE 'FALSE' END
            """, nativeQuery = true)
    boolean isExistCodeUpdate(String newCode, String currentCode);

    @Query(value = """
            SELECT CASE WHEN EXISTS (
                SELECT 1
                FROM user_admin ua
                WHERE ua.email = :newEmailFe
                AND ua.email != :currentEmailFe
            ) THEN 'TRUE' ELSE 'FALSE' END
            """, nativeQuery = true)
    boolean isExistEmailUpdate(String newEmailFe, String currentEmailFe);

    @Query(value = """
        SELECT CASE WHEN COUNT(*) > 0 THEN 'TRUE' ELSE 'FALSE' END 
        FROM user_admin ua
        WHERE
        ua.email = :email
        OR ua.code = :code
""", nativeQuery = true)
    boolean isAdmin(String email, String code);

    @Query(value = """
            SELECT CASE 
            WHEN cnt >= 5 THEN 'TRUE' ELSE 'FALSE'
            END AS result
            FROM
            (
                SELECT COUNT(*) AS cnt FROM user_admin
            ) AS sub
            """, nativeQuery = true)
    boolean isMaxAdmin();

}
