package udpm.hn.studentattendance.core.admin.statistics.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADSProjectSubjectFacilityResponse;
import udpm.hn.studentattendance.repositories.ProjectRepository;

import java.util.List;

@Repository
public interface ADSProjectSubjectFacilityRepository extends ProjectRepository {

    @Query(value = """
        SELECT 
            ROW_NUMBER() OVER (ORDER BY sb.name) AS rowNumber,
            sb.name AS subjectName,
            COALESCE(done.doneProject, 0) AS doneProject,
            COALESCE(processing.processingProject, 0) AS processingProject
        FROM subject sb
        JOIN subject_facility sf ON sb.id = sf.id_subject
        LEFT JOIN (
            SELECT 
                sf_inner.id_subject,
                COUNT(DISTINCT p.id) AS doneProject
            FROM project p 
            JOIN subject_facility sf_inner ON p.id_subject_facility = sf_inner.id
            JOIN semester s ON p.id_semester = s.id
            WHERE 
                s.to_date < UNIX_TIMESTAMP(CURDATE()) AND
                s.status = 1 AND
                sf_inner.status = 1 AND
                p.status = 1
            GROUP BY sf_inner.id_subject
        ) done ON sb.id = done.id_subject
        LEFT JOIN (
            SELECT 
                sf_inner.id_subject,
                COUNT(DISTINCT p.id) AS processingProject
            FROM project p
            JOIN subject_facility sf_inner ON p.id_subject_facility = sf_inner.id
            JOIN semester s ON p.id_semester = s.id
            WHERE
                s.from_date <= UNIX_TIMESTAMP(CURDATE()) OR 
                s.to_date >= UNIX_TIMESTAMP(CURDATE()) AND
                s.status = 1 AND
                sf_inner.status = 1 AND
                p.status = 1
            GROUP BY sf_inner.id_subject
        ) processing ON sb.id = processing.id_subject
        WHERE 
            sb.status = 1 AND
            sf.status = 1
        GROUP BY sb.id, sb.name, done.doneProject, processing.processingProject
        """,
            countQuery = """
                    SELECT COUNT(DISTINCT sb.id)
                            FROM subject sb
                            JOIN subject_facility sf ON sb.id = sf.id_subject
                            LEFT JOIN (
                                SELECT
                                    sf_inner.id_subject,
                                    COUNT(DISTINCT p.id) AS doneProject
                                FROM project p
                                JOIN subject_facility sf_inner ON p.id_subject_facility = sf_inner.id
                                JOIN semester s ON p.id_semester = s.id
                                WHERE
                                    s.to_date < UNIX_TIMESTAMP(CURDATE()) AND
                                    s.status = 1 AND
                                    sf_inner.status = 1 AND
                                    p.status = 1
                                GROUP BY sf_inner.id_subject
                            ) done ON sb.id = done.id_subject
                            LEFT JOIN (
                                SELECT
                                    sf_inner.id_subject,
                                    COUNT(DISTINCT p.id) AS processingProject
                                FROM project p
                                JOIN subject_facility sf_inner ON p.id_subject_facility = sf_inner.id
                                JOIN semester s ON p.id_semester = s.id
                                WHERE
                                    s.from_date <= UNIX_TIMESTAMP(CURDATE()) AND\s
                                    s.to_date >= UNIX_TIMESTAMP(CURDATE()) AND
                                    s.status = 1 AND
                                    sf_inner.status = 1 AND
                                    p.status = 1
                                GROUP BY sf_inner.id_subject
                            ) processing ON sb.id = processing.id_subject
                            WHERE
                                sb.status = 1 AND
                                sf.status = 1
                    """, nativeQuery = true)
    Page<ADSProjectSubjectFacilityResponse> getProjectSubjectFacilityResponses(Pageable pageable);


}
