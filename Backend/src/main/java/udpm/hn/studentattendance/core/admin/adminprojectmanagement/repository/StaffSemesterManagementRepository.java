package udpm.hn.studentattendance.core.admin.adminprojectmanagement.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.adminprojectmanagement.model.response.SemesterResponse;
import udpm.hn.studentattendance.repositories.ProjectRepository;

import java.util.List;

@Repository
public interface StaffSemesterManagementRepository extends ProjectRepository {

    @Query(value = """
                SELECT 
                    ROW_NUMBER() OVER (ORDER BY s.created_at DESC) AS indexs,
                    s.id AS id,
                    s.name AS name
                FROM semester s
                LEFT JOIN facility f ON s.id_facility = f.id
                WHERE (
                    :facilityId IS NULL OR f.id = :facilityId
                ) AND s.status = 1
                ORDER BY s.created_at DESC
            """, nativeQuery = true)
    List<SemesterResponse> getSemesters(@Param("facilityId") String facilityId);
}
