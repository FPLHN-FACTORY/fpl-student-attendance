package udpm.hn.studentattendance.core.admin.user_admin.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.repositories.UserStaffRepository;

import java.util.List;

@Repository
public interface Admin_UserAdminStaffExtendRepository extends UserStaffRepository {

    @Query(value = """
            SELECT 
            us
            FROM UserStaff us
            """

    )
    List<UserStaff> getAllUserStaff();
}
