package udpm.hn.studentattendance.core.admin.role.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.role.model.response.AdRoleFacilityResponse;
import udpm.hn.studentattendance.repositories.FacilityRepository;

import java.util.List;

@Repository
public interface AdRoleFacilityRepository extends FacilityRepository {
    @Query(
            value =
                    """
                            SELECT 
                            f.id as facilityId,
                            f.code as facilityCode,
                            f.name as facilityName
                            FROM Facility f
                            WHERE f.status = 0     
                    """
    )
    List<AdRoleFacilityResponse> getFacilities();
}
