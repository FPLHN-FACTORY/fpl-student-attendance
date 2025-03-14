package udpm.hn.studentattendance.core.staff.plan.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDSubjectResponse;
import udpm.hn.studentattendance.repositories.SubjectRepository;

import java.util.List;

@Repository
public interface SPDSubjectRepository extends SubjectRepository {

    @Query(value = """
        SELECT 
            s.id,
            s.name,
            s.code
        FROM subject s 
        JOIN subject_facility sf ON sf.id_subject = s.id
        WHERE
            s.status = 1 AND
            sf.status = 1 AND
            sf.id_facility = :idFacility
        ORDER BY s.name ASC
    """, nativeQuery = true)
    List<SPDSubjectResponse> getAllByFacility(String idFacility);

}
