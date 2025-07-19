package udpm.hn.studentattendance.core.admin.statistics.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADSSubjectFacilityChartResponse;
import udpm.hn.studentattendance.repositories.SubjectFacilityRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ADSSubjectFacilityExtendRepository extends SubjectFacilityRepository {

    @Query(value = """
           SELECT
               f.id,
               f.name as facilityName,
               COUNT(sf.id) as totalSubjectFacility
           FROM
               facility f
               LEFT JOIN subject_facility sf ON f.id = sf.id_facility AND sf.status = 1
           WHERE
               f.status = 1
           GROUP BY
               f.id, f.name
           ORDER BY
               f.name
            """, nativeQuery = true)
    List<ADSSubjectFacilityChartResponse> getSubjectByFacility();
}
