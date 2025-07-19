package udpm.hn.studentattendance.core.staff.useractivity.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.UserActivityLogRepository;

import java.util.List;

@Repository
public interface STUserActivityFilterExtendRepository extends UserActivityLogRepository {



    @Query(value = """
            SELECT
            us
            FROM
            UserStaff us
            JOIN Role r
            ON r.userStaff.id = us.id
            JOIN Facility f ON f.id = r.facility.id
            WHERE us.status = :status
            AND f.status = :facilityStatus
            AND r.status = :roleStatus
            AND f.id = :idFacility
            ORDER BY us.createdAt DESC
            """)
    List<UserStaff> getAllUserStaff(EntityStatus status, EntityStatus facilityStatus, EntityStatus roleStatus, String idFacility);




}
