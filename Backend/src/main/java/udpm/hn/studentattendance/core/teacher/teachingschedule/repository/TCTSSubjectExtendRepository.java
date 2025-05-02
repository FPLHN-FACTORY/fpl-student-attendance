package udpm.hn.studentattendance.core.teacher.teachingschedule.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Subject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.SubjectRepository;

import java.util.List;

@Repository
public interface TCTSSubjectExtendRepository extends SubjectRepository {
    @Query(value = """
                SELECT
                DISTINCT
                sb
                FROM Subject sb
                LEFT JOIN SubjectFacility sf ON sf.subject.id = sb.id
                LEFT JOIN Project p ON p.subjectFacility.id = sf.id
                LEFT JOIN Factory ft ON ft.project.id = p.id
                LEFT JOIN UserStaff us ON us.id = ft.userStaff.id
                WHERE us.id = :userId
                AND sb.status = :status
            """)
    List<Subject> getAllSubjectByStaff(String userId, EntityStatus status);
}
