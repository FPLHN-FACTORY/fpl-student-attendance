package udpm.hn.studentattendance.core.authentication.repositories;

import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.repositories.FacilityRepository;

import java.util.List;

@Repository
public interface AuthenticationFacilityRepository extends FacilityRepository {

}
