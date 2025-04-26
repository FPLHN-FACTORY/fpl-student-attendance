package udpm.hn.studentattendance.core.teacher.teaching_schedule.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.FactoryRepository;

import java.util.List;

@Repository
public interface Teacher_TSFactoryExtendRepository extends FactoryRepository {
    @Query(value = """
            SELECT
            f
            FROM Factory f
            LEFT JOIN UserStaff us ON us.id = f.userStaff.id
            WHERE us.id = :userId
            AND f.status = :status
            """)
    List<Factory> getAllFactoryByStaff(String userId, EntityStatus status);
}
