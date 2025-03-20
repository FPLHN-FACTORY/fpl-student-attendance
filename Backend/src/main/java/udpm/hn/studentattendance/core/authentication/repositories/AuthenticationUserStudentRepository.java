package udpm.hn.studentattendance.core.authentication.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.repositories.UserStudentRepository;

import java.util.Optional;

@Repository
public interface AuthenticationUserStudentRepository extends UserStudentRepository {

    Optional<UserStudent> findByEmail(String email);

    Optional<UserStudent> findByEmailAndFacility_Id(String email, String idFacility);

    @Query(value = """
        SELECT 
            CASE WHEN COUNT(*) > 0 THEN 'TRUE' ELSE 'FALSE' END 
        FROM user_student us
        LEFT JOIN facility f ON us.id_facility = f.id
        WHERE 
            us.status = 1 AND
            us.id != :idUser AND 
            us.code LIKE :code AND
            f.id = :idFacility
    """, nativeQuery = true)
    boolean isExistsCode(String code, String idUser, String idFacility);
}
