package udpm.hn.studentattendance.core.teacher.factory.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCPlanFactoryResponse;
import udpm.hn.studentattendance.repositories.PlanFactoryRepository;

import java.util.Optional;

@Repository
public interface TCPlanFactoryRepository extends PlanFactoryRepository {

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
                    LEAST(pf.status, p.status, f.status, pl.status) AS status
                FROM factory f
                LEFT JOIN plan_factory pf ON pf.id_factory = f.id
                LEFT JOIN plan pl ON pf.id_plan = pl.id
                LEFT JOIN project p ON p.id = f.id_project
                LEFT JOIN subject_facility sf ON sf.id = p.id_subject_facility
                LEFT JOIN user_staff us ON us.id = f.id_user_staff
                LEFT JOIN plan_date pd ON pd.id_plan_factory = pf.id
                WHERE
                    p.status = 1 AND
                    f.status = 1 AND
                    sf.status = 1 AND
                    f.id = :idFactory AND
                    sf.id_facility = :idFacility
                GROUP BY
                    pf.id, f.name, us.code, us.name, pf.id_plan, pf.status, pl.name, pl.from_date, pl.to_date
            """, nativeQuery = true)
    Optional<TCPlanFactoryResponse> getDetail(String idFactory, String idFacility);

}
