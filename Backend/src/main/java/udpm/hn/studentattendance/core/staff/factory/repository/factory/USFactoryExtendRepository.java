package udpm.hn.studentattendance.core.staff.factory.repository.factory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.factory.model.request.USFactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.model.response.USDetailFactoryResponse;
import udpm.hn.studentattendance.core.staff.factory.model.response.USFactoryResponse;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.repositories.FactoryRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface USFactoryExtendRepository extends FactoryRepository {
    @Query(value = """
            SELECT
                ROW_NUMBER() OVER (ORDER BY ft.created_at DESC) as rowNumber,
                ft.id AS id,
                CONCAT(ft.name, ' (', s.code, ')') AS name,
                ft.status AS factoryStatus,
                CONCAT(p.name, ' - ', lp.name) AS projectName,
                sub.code AS subjectCode,
                CONCAT(us.code, ' - ', us.name) AS staffName,
                ft.description AS factoryDescription
            FROM factory ft
            LEFT JOIN project p ON p.id = ft.id_project
            LEFT JOIN level_project lp ON lp.id = p.id_level_project
            LEFT JOIN subject_facility sf ON p.id_subject_facility = sf.id
            LEFT JOIN subject sub ON sub.id = sf.id_subject
            LEFT JOIN user_staff us ON us.id = ft.id_user_staff
            LEFT JOIN facility f ON f.id = sf.id_facility
            LEFT JOIN semester s ON s.id = p.id_semester
            WHERE
                f.id = :facilityId
                AND f.status = 1
                AND p.status = 1
                AND sf.status = 1
                AND sub.status = 1
                AND f.status = 1
                AND lp.status = 1
                AND (:#{#staffFactoryRequest.idProject} IS NULL OR p.id = :#{#staffFactoryRequest.idProject})
                AND (:#{#staffFactoryRequest.idStaff} IS NULL OR us.id = :#{#staffFactoryRequest.idStaff})
                AND (:#{#staffFactoryRequest.idSemester} IS NULL OR s.id = :#{#staffFactoryRequest.idSemester})
                AND (:#{#staffFactoryRequest.status} IS NULL OR ft.status = :#{#staffFactoryRequest.status})
                AND (:#{#staffFactoryRequest.factoryName} IS NULL OR
                    CONCAT(ft.name, ' (', s.code, ')') LIKE CONCAT('%', :#{#staffFactoryRequest.factoryName}, '%'))
            ORDER BY ft.created_at DESC, ft.status DESC 
            """, countQuery = """
                SELECT COUNT(*)
                FROM factory ft
                JOIN project p ON p.id = ft.id_project
                JOIN level_project lp ON lp.id = p.id_level_project
                JOIN subject_facility sf ON p.id_subject_facility = sf.id
                JOIN subject sub ON sub.id = sf.id_subject
                JOIN facility f ON f.id = sf.id_facility
                JOIN user_staff us ON us.id = ft.id_user_staff
                LEFT JOIN semester s ON s.id = p.id_semester
                WHERE
            f.id = :facilityId
            AND f.status = 1
            AND p.status = 1
            AND sf.status = 1
            AND sub.status = 1
            AND f.status = 1
            AND lp.status = 1
            AND (:#{#staffFactoryRequest.idProject} IS NULL OR p.id = :#{#staffFactoryRequest.idProject})
            AND (:#{#staffFactoryRequest.idStaff} IS NULL OR us.id = :#{#staffFactoryRequest.idStaff})
            AND (:#{#staffFactoryRequest.idSemester} IS NULL OR s.id = :#{#staffFactoryRequest.idSemester})
            AND (:#{#staffFactoryRequest.factoryName} IS NULL OR
                CONCAT(ft.name, '-', s.code) LIKE CONCAT('%', :#{#staffFactoryRequest.factoryName}, '%'))
            AND (:#{#staffFactoryRequest.status} IS NULL OR ft.status = :#{#staffFactoryRequest.status})
                """, nativeQuery = true)
    Page<USFactoryResponse> getAllFactory(Pageable pageable, String facilityId,
                                          USFactoryRequest staffFactoryRequest);

    @Query(value = """
            SELECT
                ROW_NUMBER() OVER (ORDER BY ft.created_at DESC) as rowNumber,
                ft.id AS id,
                CONCAT(ft.name, ' (', s.code, ')') AS name,
                ft.status AS factoryStatus,
                CONCAT(p.name, ' - ', lp.name) AS projectName,
                sub.code AS subjectCode,
                CONCAT(us.code, ' - ', us.name) AS staffName,
                ft.description AS factoryDescription
            FROM factory ft
            LEFT JOIN project p ON p.id = ft.id_project
            LEFT JOIN level_project lp ON lp.id = p.id_level_project
            LEFT JOIN subject_facility sf ON p.id_subject_facility = sf.id
            LEFT JOIN subject sub ON sub.id = sf.id_subject
            LEFT JOIN user_staff us ON us.id = ft.id_user_staff
            LEFT JOIN facility f ON f.id = sf.id_facility
            LEFT JOIN semester s ON s.id = p.id_semester
            WHERE
                f.id = :facilityId
                AND f.status = 1
                AND p.status = 1
                AND sf.status = 1
                AND sub.status = 1
                AND f.status = 1
                AND lp.status = 1
            ORDER BY ft.created_at DESC, ft.status DESC 
            """, nativeQuery = true)
    List<USFactoryResponse> exportAllFactory(String facilityId);

    @Query(value = """
            SELECT
            f.id as id,
            f.name as factoryName,
            f.description as factoryDescription,
            p.name as nameProject,
            us.code as staffCode,
            us.name as staffName,
            p.id as projectId,
            us.id as staffId
            FROM
            Factory f
            LEFT JOIN
            Project p ON p.id = f.project.id
            LEFT JOIN
            UserStaff us ON us.id = f.userStaff.id
            WHERE
            f.id = :factoryId
             """)
    Optional<USDetailFactoryResponse> getFactoryById(String factoryId);

    @Query(value = """
            SELECT
            CASE WHEN COUNT(*) > 0 THEN 'TRUE' ELSE 'FALSE' END
            FROM
            factory ft
            WHERE
            ft.name = :name
            AND
            ft.id_project = :idProject AND
            (:idFactory IS NULL OR ft.id != :idFactory)
                    """, nativeQuery = true)
    boolean isExistNameAndProject(String name, String idProject, String idFactory);

    @Query(value = """
            SELECT
            ft
            FROM
            Factory ft
            JOIN Project p ON ft.project.id = p.id
            JOIN Semester s ON p.semester.id = s.id
            JOIN SubjectFacility sf ON p.subjectFacility.id = sf.id
            AND sf.facility.id = :facilityId
            AND s.id = :semesterId
            """)
    List<Factory> getAllFactoryBySemester(String facilityId, String semesterId);

    @Query(value = """
            SELECT
            CASE WHEN COUNT(*) >= 3 THEN 'TRUE' ELSE 'FALSE' END
            FROM
            factory ft
            LEFT JOIN
            project p ON p.id = ft.id_project
            WHERE
            ft.id_user_staff = :userStaffId
            AND
            p.id_semester = :semesterId
            AND
            ft.status = 1
            AND 
            p.status = 1
                """, nativeQuery = true)
    boolean isTeacherJoinThanThreeFactory(String userStaffId, String semesterId);
}
