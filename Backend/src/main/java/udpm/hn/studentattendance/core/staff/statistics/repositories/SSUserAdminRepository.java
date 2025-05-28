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
            id,
            code,
            name,
            email
        FROM user_admin
        WHERE
            status = 1 AND
            email <> :email
        ORDER BY name ASC
    """, nativeQuery = true)
    List<SSUserResponse> getAllList(String email);

}
