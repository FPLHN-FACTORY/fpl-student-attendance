package udpm.hn.studentattendance.core.staff.plan.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanFactoryRequest;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDFactoryResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanFactoryResponse;
import udpm.hn.studentattendance.repositories.PlanFactoryRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SPDPlanFactoryRepository extends PlanFactoryRepository {

    @Query(value = """
                SELECT
                    ROW_NUMBER() OVER (ORDER BY MAX(pd.created_at) DESC) as orderNumber,
                    pf.id,
                    pf.id_plan AS planId,
                    pl.name AS planName,
                    f.name AS factoryName,
                    CONCAT(us.code, ' - ', us.name) AS staffName,
                    MIN(pd.start_date) AS fromDate,
                    MAX(pd.start_date) AS toDate,
                    COUNT(DISTINCT pd.id) AS totalShift,
                    COUNT(DISTINCT CASE WHEN pd.end_date <= UNIX_TIMESTAMP(NOW()) * 1000 THEN pd.id END) AS totalCurrentShift,
                    LEAST(pf.status, p.status, f.status, pl.status) AS status,
                    (SELECT COUNT(usf.id) FROM user_student_factory usf WHERE f.id = usf.id_factory AND usf.status = 1) AS totalStudent,
                    MAX(pd.created_at) AS lastUpdated
                FROM plan_factory pf
                JOIN plan pl ON pf.id_plan = pl.id
                JOIN factory f ON pf.id_factory = f.id
                JOIN project p ON p.id = f.id_project
                JOIN subject_facility sf ON sf.id = p.id_subject_facility
                LEFT JOIN user_staff us ON us.id = f.id_user_staff
                LEFT JOIN plan_date pd ON pd.id_plan_factory = pf.id
                WHERE
                    p.status = 1 AND
                    f.status = 1 AND
                    sf.status = 1 AND
                    pf.id_plan = :#{#request.idPlan} AND
                    sf.id_facility = :#{#request.idFacility} AND
                    (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR
                        f.name LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%') OR
                        us.name LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%') OR
                        us.code LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')) AND
                    (:#{#request.fromDate} IS NULL OR pd.start_date >= :#{#request.fromDate}) AND
                    (:#{#request.toDate} IS NULL OR pd.start_date <= :#{#request.toDate}) AND
                    (:#{#request.status} IS NULL OR pf.status = :#{#request.status})
                GROUP BY
                    pf.id, f.name, us.code, us.name, pf.id_plan, pf.status, pl.name
                ORDER BY pf.status DESC, lastUpdated DESC
            """, countQuery = """
                SELECT
                    COUNT(DISTINCT f.id)
                FROM plan_factory pf
                JOIN plan pl ON pf.id_plan = pl.id
                JOIN factory f on f.id = pf.id_factory
                JOIN project p ON p.id = f.id_project
                JOIN subject_facility sf ON sf.id = p.id_subject_facility
                LEFT JOIN user_staff us ON us.id = f.id_user_staff
                LEFT JOIN plan_date pd ON pd.id_plan_factory = pf.id
                WHERE
                    p.status = 1 AND
                    f.status = 1 AND
                    sf.status = 1 AND
                    pf.id_plan = :#{#request.idPlan} AND
                    sf.id_facility = :#{#request.idFacility} AND
                    (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR
                        f.name LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%') OR
                        p.name LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')) AND
                    (:#{#request.fromDate} IS NULL OR pd.start_date >= :#{#request.fromDate}) AND
                    (:#{#request.toDate} IS NULL OR pd.start_date <= :#{#request.toDate}) AND
                    (:#{#request.status} IS NULL OR pf.status = :#{#request.status})
            """, nativeQuery = true)
    Page<SPDPlanFactoryResponse> getAllByFilter(Pageable pageable, SPDFilterPlanFactoryRequest request);

    @Query(value = """
                  SELECT
                    1 as orderNumber,
                    pf.id,
                    pf.id_plan AS planId,
                    pl.name AS planName,
                    f.name AS factoryName,
                    CONCAT(us.code, ' - ', us.name) AS staffName,
                    pl.from_date AS fromDate,
                    pl.to_date AS toDate,
                    COUNT(DISTINCT pd.id) AS totalShift,
                    COUNT(DISTINCT CASE WHEN pd.end_date <= UNIX_TIMESTAMP(NOW()) * 1000 THEN pd.id END) AS totalCurrentShift,
                    LEAST(pf.status, p.status, f.status, pl.status) AS status
                FROM plan_factory pf
                JOIN plan pl ON pf.id_plan = pl.id
                JOIN factory f ON pf.id_factory = f.id
                JOIN project p ON p.id = f.id_project
                JOIN subject_facility sf ON sf.id = p.id_subject_facility
                LEFT JOIN user_staff us ON us.id = f.id_user_staff
                LEFT JOIN plan_date pd ON pd.id_plan_factory = pf.id
                WHERE
                    p.status = 1 AND
                    f.status = 1 AND
                    sf.status = 1 AND
                    pf.id = :idPlanFactory AND
                    sf.id_facility = :idFacility
                GROUP BY
                    pf.id, f.name, us.code, us.name, pf.id_plan, pf.status, pl.name, pl.from_date, pl.to_date
            """, nativeQuery = true)
    Optional<SPDPlanFactoryResponse> getDetail(String idPlanFactory, String idFacility);

    @Query(value = """
                SELECT
                    f.id,
                    f.name AS factoryName,
                    CONCAT(us.code, ' - ', us.name) AS staffName
                FROM factory f
                JOIN project p ON p.id = f.id_project
                LEFT JOIN user_staff us ON us.id = f.id_user_staff
                LEFT JOIN plan_factory pf ON pf.id_factory = f.id AND pf.status = 1
                LEFT JOIN plan pl ON pf.id_plan = pl.id
                WHERE
                    f.status = 1 AND
                    p.status = 1 AND
                    (pf.id IS NULL OR pl.status = 0) AND
                    p.id = :idProject
                ORDER BY f.created_at DESC
            """, nativeQuery = true)
    List<SPDFactoryResponse> getListFactory(String idProject);

    @Query(value = """
                SELECT
                    CASE WHEN COUNT(*) > 0 THEN 'TRUE' ELSE 'FALSE' END
                FROM plan_factory pf
                WHERE
                    pf.status = 1 AND
                    pf.id_factory = :idFactory
            """, nativeQuery = true)
    boolean isExistsFactoryInPlan(String idFactory);

    @Modifying
    @Transactional
    @Query(value = """
                DELETE ad
                FROM attendance ad
                JOIN plan_date pd ON pd.id = ad.id_plan_date
                JOIN plan_factory pf ON pf.id = pd.id_plan_factory
                WHERE
                    pf.id = :idPlanFactory AND
                    pf.status = 0
            """, countQuery = "SELECT 1", nativeQuery = true)
    int deleteAllAttendanceByIdPlanFactory(String idPlanFactory);

    @Modifying
    @Transactional
    @Query(value = """
                DELETE pd
                FROM plan_date pd
                JOIN plan_factory pf ON pf.id = pd.id_plan_factory
                WHERE
                    pf.id = :idPlanFactory AND
                    pf.status = 0
            """, countQuery = "SELECT 1", nativeQuery = true)
    int deleteAllPlanDateByIdPlanFactory(String idPlanFactory);

}
