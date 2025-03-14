package udpm.hn.studentattendance.core.staff.factory.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.SubjectFacility;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.SubjectFacilityRepository;

import java.util.List;

@Repository
public interface Staff_SubjectFacilityFactoryExtendRepository extends SubjectFacilityRepository {
    @Query(
            """
                            SELECT 
                            sf
                            FROM 
                            SubjectFacility sf
                            LEFT JOIN 
                            Facility f on f.id = sf.facility.id
                            WHERE 
                            sf.status = :subjectFacilityStatus
                            AND 
                            f.status = :facilityStatus
                            AND 
                            f.id = :facilityId
                    """
    )
    List<SubjectFacility> getAllSubjectFacility
            (EntityStatus subjectFacilityStatus, EntityStatus facilityStatus, String facilityId);
}
