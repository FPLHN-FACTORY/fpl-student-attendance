package udpm.hn.studentattendance.core.teacher.statistics.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.teacher.statistics.model.response.TSUserResponse;
import udpm.hn.studentattendance.repositories.UserAdminRepository;

import java.util.List;

@Repository
public interface TSUserAdminRepository extends UserAdminRepository {

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
    List<TSUserResponse> getAllList(String email);

}
