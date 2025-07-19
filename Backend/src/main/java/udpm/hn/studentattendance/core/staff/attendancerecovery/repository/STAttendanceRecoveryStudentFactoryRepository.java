package udpm.hn.studentattendance.core.staff.attendancerecovery.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.UserStudentFactory;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.UserStudentFactoryRepository;

@Repository
public interface STAttendanceRecoveryStudentFactoryRepository extends UserStudentFactoryRepository {

    @Query(value = """
            SELECT 
            usf
            FROM UserStudentFactory usf
            WHERE 
            usf.userStudent.id = :userId
            AND usf.status = :status
            AND usf.status = :studentStatus
            """)
    UserStudentFactory getUserStudentFactoryByUserId(String userId, EntityStatus status, EntityStatus studentStatus);
}
