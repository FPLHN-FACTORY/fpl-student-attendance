package udpm.hn.studentattendance.core.staff.factory.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Project;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.ProjectRepository;

import java.util.List;

@Repository
public interface Staff_ProjectFactoryExtendRepository extends ProjectRepository {
    @Query
            (value =
                    """
                                SELECT 
                                p
                                FROM 
                                Project p
                                LEFT JOIN 
                                SubjectFacility sf on p.subjectFacility.id = sf.id
                                LEFT JOIN 
                                Facility f on f.id = sf.facility.id
                                WHERE 
                                p.status = :projectStatus
                                AND sf.status = :subjecFacilityStatus
                                AND f.status = :facilityStatus
                                AND f.id = :facilityId
                                
                            """)
    List<Project> getAllProject
            (EntityStatus projectStatus, EntityStatus subjecFacilityStatus, EntityStatus facilityStatus, String facilityId);
}
