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
                    LEAST(pf.status, p.status, f.status, pl.status, sj.status, lp.status, s.status) AS status
                FROM factory f
                JOIN plan_factory pf ON pf.id_factory = f.id
                JOIN plan pl ON pf.id_plan = pl.id
                JOIN project p ON p.id = f.id_project
                JOIN subject_facility sf ON sf.id = p.id_subject_facility
                JOIN level_project lp ON p.id_level_project = lp.id
                JOIN semester s ON p.id_semester = s.id
                JOIN subject sj ON sf.id_subject = sj.id
                JOIN user_staff us ON us.id = f.id_user_staff
                JOIN plan_date pd ON pd.id_plan_factory = pf.id
                WHERE
                    f.id = :idFactory AND
                    sf.id_facility = :idFacility
                GROUP BY
                    pf.id, f.name, us.code, us.name, pf.id_plan, pl.name, pl.from_date, pl.to_date, pf.status, p.status, f.status, pl.status, sj.status, lp.status, s.status
            """, nativeQuery = true)
    Optional<TCPlanFactoryResponse> getDetail(String idFactory, String idFacility);

}
