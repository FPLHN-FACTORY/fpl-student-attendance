package udpm.hn.studentattendance.core.staff.plan.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.repositories.FactoryRepository;

@Repository
public interface SPDFactoryRepository extends FactoryRepository {

    @Query(value = """
        SELECT COUNT(DISTINCT us.id)
        FROM factory f
        JOIN user_student_factory usf ON f.id = usf.id_factory
        JOIN user_student us ON usf.id_user_student = us.id
        WHERE
            usf.status = 1 AND
            us.status = 1 AND
            f.id = :idFactory
    """, nativeQuery = true)
    Integer getCountTotalStudentInFactory(String idFactory);

}
