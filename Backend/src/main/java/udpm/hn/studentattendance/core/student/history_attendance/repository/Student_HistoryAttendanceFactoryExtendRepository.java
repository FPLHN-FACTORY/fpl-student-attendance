package udpm.hn.studentattendance.core.student.history_attendance.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.FactoryRepository;

import java.util.List;

@Repository
public interface Student_HistoryAttendanceFactoryExtendRepository extends FactoryRepository {
    @Query(
            value = """
            SELECT 
            ft
            FROM 
            Factory ft
            LEFT JOIN 
            UserStudentFactory usf ON usf.factory.id = ft.id
            WHERE 
            ft.status = :factoryStatus
            AND 
            usf.userStudent.id = :userStudentId
"""
    )
    List<Factory> getAllFactoryByUser(EntityStatus factoryStatus, String userStudentId);

}
