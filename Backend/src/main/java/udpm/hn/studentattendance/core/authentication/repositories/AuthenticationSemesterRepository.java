package udpm.hn.studentattendance.core.authentication.repositories;

import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.FacilityRepository;
import udpm.hn.studentattendance.repositories.SemesterRepository;

import java.util.List;

@Repository
public interface AuthenticationSemesterRepository extends SemesterRepository {

    List<Semester> findAllByStatusOrderByFromDateDesc(EntityStatus status);

}
