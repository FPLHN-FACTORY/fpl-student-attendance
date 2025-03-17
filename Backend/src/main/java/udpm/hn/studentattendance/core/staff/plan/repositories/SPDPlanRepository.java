package udpm.hn.studentattendance.core.staff.plan.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterCreatePlanRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanRequest;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDFactoryResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDProjectResponse;
import udpm.hn.studentattendance.repositories.PlanRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SPDPlanRepository extends PlanRepository {

    @Query(value = """
        SELECT 
            ROW_NUMBER() OVER (ORDER BY pl.created_at DESC) as orderNumber,
            pl.id,
            pl.name AS planName,
            p.name AS projectName,
            lp.name AS level,
            pl.from_date AS fromDate,
            pl.to_date AS toDate,
            CONCAT(s.name, ' - ', s.year) AS semesterName,
            s2.name AS subjectName,
            pl.status AS status
        FROM plan pl
        JOIN project p ON p.id = pl.id_project
        LEFT JOIN level_project lp ON lp.id = p.id_level_project
        LEFT JOIN semester s ON s.id = p.id_semester
        LEFT JOIN subject_facility sf ON sf.id = p.id_subject_facility
        LEFT JOIN subject s2 ON s2.id = sf.id_subject
        WHERE 
            pl.status = 1 AND
            p.status = 1 AND
            sf.status = 1 AND
            s.status = 1 AND
            s2.status = 1 AND
            sf.id_facility = :#{#request.idFacility} AND
            (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR BINARY pl.name LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')) AND
            (:#{#request.level} IS NULL OR lp.id = :#{#request.level}) AND
            (:#{#request.semester} IS NULL OR s.name = :#{#request.semester}) AND
            (:#{#request.year} IS NULL OR s.year = :#{#request.year}) AND
            (:#{#request.subject} IS NULL OR s2.id = :#{#request.subject})
        ORDER BY pl.created_at DESC 
    """, countQuery = """
        SELECT 
            COUNT(DISTINCT pl.id)
        FROM plan pl
        JOIN project p ON p.id = pl.id_project
        LEFT JOIN level_project lp ON lp.id = p.id_level_project
        LEFT JOIN semester s ON s.id = p.id_semester
        LEFT JOIN subject_facility sf ON sf.id = p.id_subject_facility
        LEFT JOIN subject s2 ON s2.id = sf.id_subject
        WHERE 
            pl.status = 1 AND
            p.status = 1 AND
            sf.status = 1 AND
            s.status = 1 AND
            s2.status = 1 AND
            sf.id_facility = :#{#request.idFacility} AND
            (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR BINARY pl.name LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')) AND
            (:#{#request.level} IS NULL OR lp.id = :#{#request.level}) AND
            (:#{#request.semester} IS NULL OR s.name = :#{#request.semester}) AND
            (:#{#request.year} IS NULL OR s.year = :#{#request.year}) AND
            (:#{#request.subject} IS NULL OR s2.id = :#{#request.subject})
    """, nativeQuery = true)
    Page<SPDPlanResponse> getAllByFilter(Pageable pageable, SPDFilterPlanRequest request);

    @Query(value = """
        SELECT 
            1 as orderNumber,
            pl.id,
            pl.name AS planName,
            p.name AS projectName,
            lp.name AS level,
            pl.from_date AS fromDate,
            pl.to_date AS toDate,
            CONCAT(s.name, ' - ', s.year) AS semesterName,
            s2.name AS subjectName,
            pl.status AS status
        FROM plan pl
        JOIN project p ON p.id = pl.id_project
        LEFT JOIN level_project lp ON lp.id = p.id_level_project
        LEFT JOIN semester s ON s.id = p.id_semester
        LEFT JOIN subject_facility sf ON sf.id = p.id_subject_facility
        LEFT JOIN subject s2 ON s2.id = sf.id_subject
        WHERE 
            pl.status = 1 AND
            p.status = 1 AND
            sf.status = 1 AND
            s.status = 1 AND
            s2.status = 1 AND
            sf.id_facility = :idFacility AND
            pl.id = :idPlan
    """, nativeQuery = true)
    Optional<SPDPlanResponse> getByIdPlan(String idPlan, String idFacility);

    @Query(value = """
        SELECT 
            p.id,
            p.name,
            s.from_date AS fromDate,
            s.to_date AS toDate
        FROM project p
        LEFT JOIN plan pl ON pl.id_project = p.id
        LEFT JOIN level_project lp ON lp.id = p.id_level_project
        LEFT JOIN semester s ON s.id = p.id_semester
        LEFT JOIN subject_facility sf ON sf.id = p.id_subject_facility
        LEFT JOIN subject s2 ON s2.id = sf.id_subject
        WHERE 
            p.status = 1 AND
            sf.status = 1 AND
            s.status = 1 AND
            s2.status = 1 AND
            pl.id IS NULL AND
            sf.id_facility = :#{#request.idFacility} AND
            (:#{#request.level} IS NULL OR lp.id = :#{#request.level}) AND
            (:#{#request.semester} IS NULL OR s.name = :#{#request.semester}) AND
            (:#{#request.year} IS NULL OR s.year = :#{#request.year}) AND
            (:#{#request.subject} IS NULL OR s2.id = :#{#request.subject})
        ORDER BY p.name ASC 
    """, nativeQuery = true)
    List<SPDProjectResponse> getListProject(SPDFilterCreatePlanRequest request);


    @Query(value = """
        SELECT 
            f.id,
            f.name AS factoryName,
            CONCAT(us.code, ' - ', us.name) AS staffName
        FROM factory f
        JOIN project p ON p.id = f.id_project
        LEFT JOIN user_staff us ON us.id = f.id_user_staff
        LEFT JOIN plan_date pd ON pd.id_factory = f.id AND pd.status = 1
        WHERE 
            f.status = 1 AND
            p.status = 1 AND
            pd.id IS NULL AND
            p.id = :idProject
        ORDER BY f.created_at DESC 
    """, nativeQuery = true)
    List<SPDFactoryResponse> getListFactory(String idProject);

}
