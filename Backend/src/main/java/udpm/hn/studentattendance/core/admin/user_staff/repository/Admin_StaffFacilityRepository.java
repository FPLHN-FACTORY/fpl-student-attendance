package udpm.hn.studentattendance.core.admin.user_staff.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.FacilityRepository;

import java.util.List;

@Repository
public interface Admin_StaffFacilityRepository extends FacilityRepository {
    @Query
            (
                    value = """
                            SELECT 
                            f
                            FROM Facility f
                            WHERE f.status = :status
                                                        """
            )
    List<Facility> getFacility(EntityStatus status);
}
