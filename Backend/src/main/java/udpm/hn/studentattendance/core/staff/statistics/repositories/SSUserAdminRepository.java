package udpm.hn.studentattendance.core.staff.statistics.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.statistics.model.response.SSUserResponse;
import udpm.hn.studentattendance.repositories.UserAdminRepository;

import java.util.List;

@Repository
public interface SSUserAdminRepository extends UserAdminRepository {

    @Query(value = """
                SELECT
                    ua.id,
                    ua.code,
                    ua.name,
                    ua.email
                FROM user_admin ua
                WHERE
                    ua.status = 1 AND
                    ua.email <> :email
                ORDER BY ua.name ASC
            """, nativeQuery = true)
    List<SSUserResponse> getAllList(String email);

    @Query(value = """
                SELECT ua.email
                FROM user_admin ua
                WHERE ua.status = 1
                ORDER BY ua.name ASC
            """, nativeQuery = true)
    List<String> getAllAdminEmails();
}
