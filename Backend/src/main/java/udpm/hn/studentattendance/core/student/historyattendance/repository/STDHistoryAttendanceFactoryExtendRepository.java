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

    @Query(value = """
        SELECT f.*
        FROM factory f
        WHERE
            EXISTS(
                SELECT 1
                FROM user_student_factory usf
                JOIN project p ON f.id_project = p.id
                JOIN semester s ON p.id_semester = s.id
                WHERE
                    usf.id_user_student = :idUserStudent
                    AND usf.status = 1
                    AND usf.id_factory = f.id
                    AND s.id = :idSemester
            )
        ORDER BY f.name ASC
    """, nativeQuery = true)
    List<Factory> getAllByUserStudentAndSemester(String idUserStudent, String idSemester);

}
