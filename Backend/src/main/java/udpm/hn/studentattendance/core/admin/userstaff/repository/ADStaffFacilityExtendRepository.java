package udpm.hn.studentattendance.core.admin.userstaff.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.FacilityRepository;

import java.util.List;

@Repository
public interface ADStaffFacilityExtendRepository extends FacilityRepository {
    @Query(value = """
            SELECT
            f
            FROM Facility f
            WHERE f.status = :status
            """)
    List<Facility> getFacility(EntityStatus status);

    Facility getFacilityByCodeAndStatus(String code, EntityStatus status);
}
