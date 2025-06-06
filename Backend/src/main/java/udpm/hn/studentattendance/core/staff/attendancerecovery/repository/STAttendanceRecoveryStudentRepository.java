package udpm.hn.studentattendance.core.staff.attendancerecovery.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.UserStudentRepository;

import java.util.List;

@Repository
public interface STAttendanceRecoveryStudentRepository extends UserStudentRepository {

    @Query(value = """
        SELECT 
        us
        FROM UserStudent us
        WHERE 
        us.code = :code
        AND us.status = :status
""")
    UserStudent getStudentByCode(String code, EntityStatus status);
}
