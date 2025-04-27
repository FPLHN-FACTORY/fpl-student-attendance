package udpm.hn.studentattendance.core.admin.levelproject.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectSearchRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.response.ADLevelProjectResponse;
import udpm.hn.studentattendance.repositories.LevelProjectRepository;

@Repository
public interface ADLevelProjectRepository extends LevelProjectRepository {

    @Query(value = """
                    select
                        row_number() over(
                        order by created_at desc) as indexs,
                        id as id,
                        name as name,
                        description as description,
                        status as status,
                        code as code
                    from
                        level_project
                    WHERE
                        (:#{#request.name} IS NULL OR name LIKE CONCAT('%', :#{#request.name}, '%')
                            or code LIKE CONCAT('%', :#{#request.name}, '%')
                            )
                        AND (:#{#request.status} IS NULL OR status = :#{#request.status})
                    ORDER BY created_at DESC
            """, countQuery = """
                    select
                        count(*)
                    from
                        level_project
                    WHERE
                        (:#{#request.name} IS NULL OR name LIKE CONCAT('%', :#{#request.name}, '%')
                            or code LIKE CONCAT('%', :#{#request.name}, '%') )
                        AND (:#{#request.status} IS NULL OR status = :#{#request.status})
            """, nativeQuery = true)
    Page<ADLevelProjectResponse> getAll(Pageable pageable, ADLevelProjectSearchRequest request);
}
