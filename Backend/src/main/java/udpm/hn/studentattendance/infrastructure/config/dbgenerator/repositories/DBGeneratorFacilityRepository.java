package udpm.hn.studentattendance.infrastructure.config.dbgenerator.repositories;

import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.repositories.FacilityRepository;

import java.util.Optional;

@Repository
public interface DBGeneratorFacilityRepository extends FacilityRepository {

    Optional<Facility> findByCode(String code);

}
