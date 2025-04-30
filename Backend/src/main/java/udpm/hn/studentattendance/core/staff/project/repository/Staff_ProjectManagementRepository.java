package udpm.hn.studentattendance.core.staff.project.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.project.model.request.USProjectSearchRequest;
import udpm.hn.studentattendance.core.staff.project.model.response.USProjectResponse;
import udpm.hn.studentattendance.repositories.ProjectRepository;

import java.util.Optional;

@Repository
public interface Staff_ProjectManagementRepository extends ProjectRepository {

    @Query(value = """
                    SELECT
                        ROW_NUMBER() OVER (ORDER BY p.created_at DESC) AS indexs,
                        p.id AS id,
                        p.name AS name,
                        lp.name AS nameLevelProject,
                        s.name AS nameSubject,
                        sem.code AS nameSemester,
                        p.description AS description,
                        p.status AS status
                    FROM project p
                    LEFT JOIN level_project lp ON p.id_level_project = lp.id
                    LEFT JOIN subject_facility sf ON p.id_subject_facility = sf.id
                    LEFT JOIN subject s ON sf.id_subject = s.id
                    LEFT JOIN semester sem ON p.id_semester = sem.id
                    LEFT JOIN facility f ON sf.id_facility = f.id
                    WHERE (
                        (:#{#request.name} IS NULL OR p.name LIKE CONCAT('%', :#{#request.name}, '%'))
                        AND (:#{#request.levelProjectId} IS NULL OR lp.id = :#{#request.levelProjectId})
                        AND (:#{#request.semesterId} IS NULL OR sem.id = :#{#request.semesterId})
                        AND (:#{#request.subjectId} IS NULL OR sf.id = :#{#request.subjectId})
                        AND (:#{#request.facilityId} IS NULL OR f.id = :#{#request.facilityId})
                        AND (:#{#request.status} IS NULL OR p.status = :#{#request.status})
                    )
                    ORDER BY p.created_at DESC
            """, countQuery = """
                    SELECT
                       COUNT(*)
                    FROM project p
                    LEFT JOIN level_project lp ON p.id_level_project = lp.id
                    LEFT JOIN subject_facility sf ON p.id_subject_facility = sf.id
                    LEFT JOIN subject s ON sf.id_subject = s.id
                    LEFT JOIN semester sem ON p.id_semester = sem.id
                    LEFT JOIN facility f ON sf.id_facility = f.id
                    WHERE (
                        (:#{#request.name} IS NULL OR p.name LIKE CONCAT('%', :#{#request.name}, '%'))
                        AND (:#{#request.levelProjectId} IS NULL OR lp.id = :#{#request.levelProjectId})
                        AND (:#{#request.semesterId} IS NULL OR sem.id = :#{#request.semesterId})
                        AND (:#{#request.subjectId} IS NULL OR sf.id = :#{#request.subjectId})
                        AND (:#{#request.facilityId} IS NULL OR f.id = :#{#request.facilityId})
                        AND (:#{#request.status} IS NULL OR p.status = :#{#request.status})
                    )
            """, nativeQuery = true)
    Page<USProjectResponse> getListProject(Pageable pageable, @Param("request") USProjectSearchRequest request);

    @Query(value = """
                SELECT
                                     p.id as id,
                                     p.name as name,
                                     s.name as nameSemester,
                                     lp.name as nameLevelProject,
                                     sb.name as nameSubject,
                                     p.description as description,
                                     p.status as status,
                                 	 lp.id as levelProjectId,
                                     s.id AS semesterId,
                                     sf.id as subjectFacilityId
                                     FROM project p
                                     LEFT JOIN semester s ON p.id_semester = s.id
                                     LEFT JOIN subject_facility sf ON p.id_subject_facility = sf.id
                                     LEFT JOIN level_project lp ON p.id_level_project = lp.id
                                     LEFT JOIN subject sb ON sf.id_subject = sb.id
                                     WHERE\s
                                     p.id = :projectId
            """, nativeQuery = true)
    Optional<USProjectResponse> getDetailProject(String projectId);

}
