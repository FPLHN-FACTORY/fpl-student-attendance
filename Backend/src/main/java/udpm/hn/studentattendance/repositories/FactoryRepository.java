package udpm.hn.studentattendance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Factory;

@Repository
public interface FactoryRepository extends JpaRepository<Factory, String> {

}
