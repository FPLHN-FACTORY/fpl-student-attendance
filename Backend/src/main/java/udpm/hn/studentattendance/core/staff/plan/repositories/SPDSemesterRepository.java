package udpm.hn.studentattendance.core.staff.plan.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.repositories.SemesterRepository;

import java.util.List;

@Repository
public interface SPDSemesterRepository extends SemesterRepository {

    @Query(value = """
        SELECT 
            year
        FROM semester
        WHERE
            status = 1
        ORDER BY year DESC
    """, nativeQuery = true)
    List<Integer> getAllYear();

}
