package udpm.hn.studentattendance.core.student.attendance.repositories;

import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.UserStudentFactory;
import udpm.hn.studentattendance.repositories.PlanDateRepository;
import udpm.hn.studentattendance.repositories.UserStudentFactoryRepository;

import java.util.Optional;

@Repository
public interface SAUserStudentFactoryRepository extends UserStudentFactoryRepository {

    Optional<UserStudentFactory> findByUserStudent_IdAndFactory_Id(String idUser, String idFactory);
}
