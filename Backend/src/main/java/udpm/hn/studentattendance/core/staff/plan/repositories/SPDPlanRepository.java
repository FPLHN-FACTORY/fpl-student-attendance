package udpm.hn.studentattendance.core.staff.plan.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
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
                    ROW_NUMBER() OVER (ORDER BY pl.status DESC, pl.created_at DESC) as orderNumber,
                    pl.id,
                    pl.name AS planName,
                    p.id AS projectId,
                    p.name AS projectName,
                    lp.name AS level,
                    pl.from_date AS fromDate,
                    pl.to_date AS toDate,
                    s.from_date AS fromDateSemester,
                    s.to_date AS toDateSemester,
                    pl.description,
                    pl.max_late_arrival,
                    CONCAT(s.name, ' - ', s.year) AS semesterName,
                    s2.name AS subjectName,
                    LEAST(pl.status, p.status, lp.status, s.status, sf.status, s2.status) AS status
                FROM plan pl
                JOIN project p ON p.id = pl.id_project
                JOIN level_project lp ON lp.id = p.id_level_project
                JOIN semester s ON s.id = p.id_semester
                JOIN subject_facility sf ON sf.id = p.id_subject_facility
                JOIN subject s2 ON s2.id = sf.id_subject
                WHERE
                    p.status = 1 AND
                    s.status = 1 AND
                    sf.status = 1 AND
                    s2.status = 1 AND
                    sf.id_facility = :#{#request.idFacility} AND
                    (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR pl.name LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')) AND
                    (:#{#request.level} IS NULL OR lp.id = :#{#request.level}) AND
                    (:#{#request.semester} IS NULL OR s.name = :#{#request.semester}) AND
                    (:#{#request.year} IS NULL OR s.year = :#{#request.year}) AND
                    (:#{#request.subject} IS NULL OR s2.id = :#{#request.subject}) AND
                    (:#{#request.status} IS NULL OR pl.status = :#{#request.status})
                ORDER BY pl.status DESC, pl.created_at DESC
            """, countQuery = """
                SELECT
                    COUNT(DISTINCT pl.id)
                FROM plan pl
                JOIN project p ON p.id = pl.id_project
                JOIN level_project lp ON lp.id = p.id_level_project
                JOIN semester s ON s.id = p.id_semester
                JOIN subject_facility sf ON sf.id = p.id_subject_facility
                JOIN subject s2 ON s2.id = sf.id_subject
                WHERE
                    p.status = 1 AND
                    s.status = 1 AND
                    sf.status = 1 AND
                    s2.status = 1 AND
                    sf.id_facility = :#{#request.idFacility} AND
                    (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR pl.name LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')) AND
                    (:#{#request.level} IS NULL OR lp.id = :#{#request.level}) AND
                    (:#{#request.semester} IS NULL OR s.name = :#{#request.semester}) AND
                    (:#{#request.year} IS NULL OR s.year = :#{#request.year}) AND
                    (:#{#request.subject} IS NULL OR s2.id = :#{#request.subject}) AND
                    (:#{#request.status} IS NULL OR pl.status = :#{#request.status})
            """, nativeQuery = true)
    Page<SPDPlanResponse> getAllByFilter(Pageable pageable, SPDFilterPlanRequest request);

    @Query(value = """
                SELECT
                    1 as orderNumber,
                    pl.id,
                    pl.name AS planName,
                    p.id AS projectId,
                    p.name AS projectName,
                    lp.name AS level,
                    pl.from_date AS fromDate,
                    pl.to_date AS toDate,
                    s.from_date AS fromDateSemester,
                    s.to_date AS toDateSemester,
                    pl.description,
                    pl.max_late_arrival,
                    CONCAT(s.name, ' - ', s.year) AS semesterName,
                    s2.name AS subjectName,
                    LEAST(pl.status, p.status, lp.status, s.status, sf.status, s2.status) AS status
                FROM plan pl
                JOIN project p ON p.id = pl.id_project
                JOIN level_project lp ON lp.id = p.id_level_project
                JOIN semester s ON s.id = p.id_semester
                JOIN subject_facility sf ON sf.id = p.id_subject_facility
                JOIN subject s2 ON s2.id = sf.id_subject
                WHERE
                    p.status = 1 AND
                    lp.status = 1 AND
                    s.status = 1 AND
                    sf.status = 1 AND
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
                JOIN level_project lp ON lp.id = p.id_level_project
                JOIN semester s ON s.id = p.id_semester
                JOIN subject_facility sf ON sf.id = p.id_subject_facility
                JOIN subject s2 ON s2.id = sf.id_subject
                WHERE
                    p.status = 1 AND
                    s.status = 1 AND
                    lp.status = 1 AND
                    sf.status = 1 AND
                    s2.status = 1 AND
                    (SELECT COUNT(*) FROM plan pl WHERE pl.id_project = p.id AND pl.status = 1) < 1 AND
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
                    CASE WHEN COUNT(*) > 0 THEN 'TRUE' ELSE 'FALSE' END
                FROM plan p
                WHERE
                    p.status = 1 AND
                    p.id_project = :idProject AND
                    (:idPlan IS NULL OR p.id != :idPlan)
            """, nativeQuery = true)
    boolean isExistsProjectInPlan(String idProject, String idPlan);

    @Modifying
    @Transactional
    @Query(value = """
                DELETE ad
                FROM attendance ad
                JOIN plan_date pd ON pd.id = ad.id_plan_date
                JOIN plan_factory pf ON pf.id = pd.id_plan_factory
                JOIN plan p ON p.id = pf.id_plan
                WHERE
                    p.id = :idPlan
            """, countQuery = "SELECT 1", nativeQuery = true)
    int deleteAllAttendanceByIdPlan(String idPlan);

    @Modifying
    @Transactional
    @Query(value = """
                DELETE pf
                FROM plan_factory pf
                JOIN plan p ON p.id = pf.id_plan
                WHERE
                    p.id = :idPlan
            """, countQuery = "SELECT 1", nativeQuery = true)
    int deleteAllPlanFactoryByIdPlan(String idPlan);

    @Modifying
    @Transactional
    @Query(value = """
                DELETE pd
                FROM plan_date pd
                JOIN plan_factory pf ON pd.id_plan_factory = pf.id
                JOIN plan p ON p.id = pf.id_plan
                WHERE
                    p.id = :idPlan
            """, countQuery = "SELECT 1", nativeQuery = true)
    int deleteAllPlanDateByIdPlan(String idPlan);

    @Modifying
    @Transactional
    @Query(value = """
                DELETE ad
                FROM attendance ad
                JOIN plan_date pd ON pd.id = ad.id_plan_date
                JOIN plan_factory pf ON pf.id = pd.id_plan_factory
                JOIN plan p ON p.id = pf.id_plan
                WHERE
                    p.id = :idPlan AND
                    (pd.start_date < p.from_date OR pd.start_date > p.to_date)
            """, countQuery = "SELECT 1", nativeQuery = true)
    int deleteAllAttendanceOutRangeDateByIdPlan(String idPlan);

    @Modifying
    @Transactional
    @Query(value = """
                DELETE pd
                FROM plan_date pd
                JOIN plan_factory pf ON pd.id_plan_factory = pf.id
                JOIN plan p ON p.id = pf.id_plan
                WHERE
                    p.id = :idPlan AND
                    (pd.start_date < p.from_date OR pd.start_date > p.to_date)
            """, countQuery = "SELECT 1", nativeQuery = true)
    int deleteAllPlanDateOutRangeDateByIdPlan(String idPlan);

}
