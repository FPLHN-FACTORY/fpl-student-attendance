package udpm.hn.studentattendance.core.staff.projectmanagement.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.projectmanagement.model.request.ProjectCreateRequest;
import udpm.hn.studentattendance.core.staff.projectmanagement.model.request.StaffProjectSearchRequest;
import udpm.hn.studentattendance.core.staff.projectmanagement.model.response.ProjectResponse;
import udpm.hn.studentattendance.entities.Project;
import udpm.hn.studentattendance.repositories.ProjectRepository;

import java.util.List;

@Repository
public interface StaffProjectManagementRepository extends ProjectRepository {
    @Query(value = """
                    select
                        row_number() over(
                        order by p.created_at desc) as indexs,
                        p.id as id,
                        p.name as name,
                        p.created_at as createdTime,
                        lp.name as nameLevelProject,
                        p.status as status
                    from
                        project p
                    left join level_project lp on
                        p.id_level_project = lp.id
                    WHERE
                        (:#{#request.name} IS NULL OR p.name LIKE CONCAT('%', :#{#request.name}, '%') 
                            OR lp.id LIKE CONCAT('%', :#{#request.levelProjectId}, '%')
                            )
                        AND (:#{#request.status} IS NULL OR p.status = :#{#request.status})
                    ORDER BY p.id DESC
            """, countQuery = """
                    select
                        count(*)
                    from
                        project p
                    left join level_project lp on
                        p.id_level_project = lp.id
                    WHERE
                        (:#{#request.name} IS NULL OR p.name LIKE CONCAT('%', :#{#request.name}, '%') 
                            OR lp.id LIKE CONCAT('%', :#{#request.levelProjectId}, '%')
                            )
                        AND (:#{#request.status} IS NULL OR p.status = :#{#request.status})
            """, nativeQuery = true)
    Page<ProjectResponse> getListProject(Pageable pageable, StaffProjectSearchRequest request);

}
