package udpm.hn.studentattendance.core.teacher.statistics.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.teacher.statistics.model.response.TSUserResponse;
import udpm.hn.studentattendance.repositories.UserStaffRepository;

import java.util.List;

@Repository
public interface TSUserStaffRepository extends UserStaffRepository {

    @Query(value = """
        SELECT
            us.id,
            us.code,
            us.name,
            us.email_fe AS email
        FROM user_staff us
        WHERE
            us.status = 1 AND
            us.email_fe <> :email AND
            EXISTS(
                SELECT 1
                FROM role r
                JOIN facility f ON r.id_facility = f.id
                WHERE
                    r.id_user_staff = us.id AND
                    r.code = 1 AND
                    f.status = 1 AND
                    r.status = 1 AND
                    f.id = :idFacility
            )
        ORDER BY us.name ASC
    """, nativeQuery = true)
    List<TSUserResponse> getAllListStaff(String idFacility, String email);

}
