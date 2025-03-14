package udpm.hn.studentattendance.core.admin.project.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.project.model.request.AdminProjectSearchRequest;
import udpm.hn.studentattendance.core.admin.project.model.response.ProjectResponse;
import udpm.hn.studentattendance.entities.SubjectFacility;
import udpm.hn.studentattendance.repositories.ProjectRepository;

import java.util.Optional;

@Repository
public interface AdminProjectManagementRepository extends ProjectRepository {

    @Query(value = """
                    SELECT 
                        ROW_NUMBER() OVER (ORDER BY p.created_at DESC) AS indexs,
                        p.id AS id,
                        p.name AS name,
                        lp.name AS nameLevelProject,
                        s.name AS nameSubject,
                        sem.name AS nameSemester,
                        p.description AS description,
                        p.status AS status
                    FROM project p
                    LEFT JOIN level_project lp ON p.id_level_project = lp.id
                    LEFT JOIN subject_facility sf ON p.id_subject_facility = sf.id
                    LEFT JOIN subject s ON sf.id_subject = s.id
                    LEFT JOIN semester sem ON p.id_semester = sem.id
                    LEFT JOIN facility f ON sem.id_facility = f.id
                    WHERE (
                        (:#{#request.name} IS NULL OR p.name LIKE CONCAT('%', :#{#request.name}, '%'))
                        AND (:#{#request.levelProjectId} IS NULL OR lp.id = :#{#request.levelProjectId})
                        AND (:#{#request.semesterId} IS NULL OR sem.id = :#{#request.semesterId})
                        AND (:#{#request.subjectId} IS NULL OR sf.id_subject = :#{#request.subjectId})
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
                    LEFT JOIN facility f ON sem.id_facility = f.id
                    WHERE (
                        (:#{#request.name} IS NULL OR p.name LIKE CONCAT('%', :#{#request.name}, '%'))
                        AND (:#{#request.levelProjectId} IS NULL OR lp.id = :#{#request.levelProjectId})
                        AND (:#{#request.semesterId} IS NULL OR sem.id = :#{#request.semesterId})
                        AND (:#{#request.subjectId} IS NULL OR sf.id = :#{#request.subjectId})
                        AND (:#{#request.facilityId} IS NULL OR f.id = :#{#request.facilityId})
                        AND (:#{#request.status} IS NULL OR p.status = :#{#request.status})
                    )
            """, nativeQuery = true)
    Page<ProjectResponse> getListProject(Pageable pageable, @Param("request") AdminProjectSearchRequest request);

    @Query(value = """
            select *
            from subject_facility
            where id_subject = :idSubject and id_facility = :idFacility
            """, nativeQuery = true)
    SubjectFacility getSubjectFacilityById(@Param("idSubject")String idSubject,@Param("idFacility") String idFacility);

    @Query(value = """
    SELECT 
    p.id as id,
    p.name as name,
    p.semester.semesterName as nameSemester,
    p.levelProject.name as nameLevelProject,
    p.subjectFacility.subject.code as nameSubject,
    p.description as description,
    p.status as status,
    p.levelProject.id as levelProjectId,
    p.semester.id as semesterId,
    p.subjectFacility.subject.id as subjectId
    FROM Project p
    WHERE 
    p.id = :projectId
""")
    Optional<ProjectResponse> getDetailProject(String projectId);

}
