package udpm.hn.studentattendance.core.authentication.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.repositories.UserStaffRepository;

import java.util.Optional;

@Repository
public interface AuthenticationUserStaffRepository extends UserStaffRepository {

    @Query(value = """
                SELECT
                    us.*
                FROM user_staff us
                JOIN role r ON r.id_user_staff = us.id
                JOIN facility f ON f.id = r.id_facility
                WHERE
                    us.status = 1
                    AND f.status = 1
                    AND us.email_fpt = :email
                    AND r.code = :roleCode
                    AND f.id = :facilityID
                LIMIT 1
            """, nativeQuery = true)
    Optional<UserStaff> findLoginStaff(String email, RoleConstant roleCode, String facilityID);

    @Query(value = """
                SELECT
                    us.*
                FROM user_staff us
                JOIN role r ON r.id_user_staff = us.id
                JOIN facility f ON f.id = r.id_facility
                WHERE
                    us.status = 1
                    AND f.status = 1
                    AND us.email_fpt = :email
                    AND r.code IN(1, 3)
                    AND f.id = :facilityID
                LIMIT 1
            """, nativeQuery = true)
    Optional<UserStaff> findLogin(String email, String facilityID);

}
