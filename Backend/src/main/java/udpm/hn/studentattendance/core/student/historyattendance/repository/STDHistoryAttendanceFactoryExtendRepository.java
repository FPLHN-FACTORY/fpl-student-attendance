package udpm.hn.studentattendance.core.student.historyattendance.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.FactoryRepository;

import java.util.List;

@Repository
public interface STDHistoryAttendanceFactoryExtendRepository extends FactoryRepository {
    @Query(value = """
           SELECT ft
           FROM Factory ft
           JOIN UserStudentFactory usf ON usf.factory.id = ft.id
           WHERE
            usf.userStudent.id = :userStudentId
    """)
    List<Factory> getAllFactoryByUser(String userStudentId);

}
