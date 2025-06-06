package udpm.hn.studentattendance.core.admin.useractivity.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.UserActivityLogRepository;

import java.util.List;

@Repository
public interface ADUserActivityFilterExtendRepository extends UserActivityLogRepository {

    @Query(value = """
            SELECT 
            ua
            FROM 
            UserAdmin ua
            WHERE ua.status = :status
            ORDER BY ua.createdAt DESC
            """)
    List<UserAdmin> getAllUserAdmin(EntityStatus status);

//    @Query(value = """
//            SELECT
//            us
//            FROM
//            UserStaff us
//            JOIN Role r
//            ON r.userStaff.id = us.id
//            JOIN Facility f ON f.id = r.facility.id
//            WHERE us.status = :status
//            AND f.status = :facilityStatus
//            AND r.status = :roleStatus
//            AND f.id = :idFacility
//            ORDER BY us.createdAt DESC
//            """)
//    List<UserStaff> getAllUserStaff(EntityStatus status, EntityStatus facilityStatus, EntityStatus roleStatus, String idFacility);

    @Query(value = """
            SELECT 
            us
            FROM 
            UserStaff us
            WHERE us.status = :status
            ORDER BY us.createdAt DESC
            """)
    List<UserStaff> getAllUserStaff(EntityStatus status);

    @Query(value = """
            SELECT 
            f
            FROM Facility f
            WHERE f.status = :status
            ORDER BY f.createdAt DESC 
            """)
    List<Facility> getAllFacility(EntityStatus status);


}
