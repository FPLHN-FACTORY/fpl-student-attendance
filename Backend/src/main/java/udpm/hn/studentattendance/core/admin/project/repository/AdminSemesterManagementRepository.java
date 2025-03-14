package udpm.hn.studentattendance.core.admin.project.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.project.model.response.SemesterResponse;
import udpm.hn.studentattendance.entities.LevelProject;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.repositories.ProjectRepository;

import java.util.List;

@Repository
public interface AdminSemesterManagementRepository extends ProjectRepository {

    @Query(value = """
                SELECT 
                    ROW_NUMBER() OVER (ORDER BY s.created_at DESC) AS indexs,
                    s.id AS id,
                    s.name AS name
                FROM semester s
                LEFT JOIN facility f ON s.id_facility = f.id
                WHERE s.status = 1
                ORDER BY s.created_at DESC
            """, nativeQuery = true)
    List<SemesterResponse> getSemesters();

    @Query(value = """
                SELECT 
                    *
                FROM semester 
                where code = :code
            """, nativeQuery = true)
    Semester getSemester(@Param("code") String code);
}
