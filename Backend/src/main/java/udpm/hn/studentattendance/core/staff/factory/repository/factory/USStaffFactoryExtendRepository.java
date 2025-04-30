package udpm.hn.studentattendance.core.staff.factory.repository.factory;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.UserStaffRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface USStaffFactoryExtendRepository extends UserStaffRepository {

    @Query(value = """
                        SELECT
                        us
                        FROM
                        UserStaff us
                        LEFT JOIN
                        Role r ON r.userStaff.id = us.id
                        LEFT JOIN
                        Facility f ON f.id = r.facility.id
                        WHERE
                        us.status = :userStaffStatus
                        AND f.status = :facilityStatus
                        AND f.id = :facilityId
            """)
    List<UserStaff> getListUserStaff(EntityStatus userStaffStatus, EntityStatus facilityStatus, String facilityId);

    Optional<UserStaff> findUserStaffByCode(String code);
}
