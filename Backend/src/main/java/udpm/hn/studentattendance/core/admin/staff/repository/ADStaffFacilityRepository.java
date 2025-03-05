package udpm.hn.studentattendance.core.admin.staff.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.role.model.response.AdRoleFacilityResponse;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.FacilityRepository;

import java.util.List;

@Repository
public interface ADStaffFacilityRepository extends FacilityRepository {
    @Query
            (
                    value = """
                            SELECT 
                            f.id as facilityId,
                            f.name as facilityName
                            FROM Facility f
                            WHERE f.status = :status
                                                        """
            )
    List<AdRoleFacilityResponse> getFacilities(EntityStatus status);
}
