package udpm.hn.studentattendance.core.staff.plan.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.repositories.UserStudentRepository;

import java.util.List;


@Repository
public interface SPDUserStudentRepository extends UserStudentRepository {

    @Query(value = """
        SELECT us.email
        FROM user_student us
        JOIN user_student_factory usf ON us.id = usf.id_user_student
        JOIN factory f ON usf.id_factory = f.id
        WHERE
            us.status = 1 AND
            usf.status = 1 AND
            f.status = 1 AND
            f.id = :idFactory
    """, nativeQuery = true)
    List<String> getAllEmail(String idFactory);

}
