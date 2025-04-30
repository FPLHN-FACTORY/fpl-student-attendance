package udpm.hn.studentattendance.core.staff.factory.repository.factory;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import udpm.hn.studentattendance.repositories.PlanFactoryRepository;

@Repository
public interface USFactoryPlanExtendRepository extends PlanFactoryRepository {
        @Query(value = """
                        SELECT CASE
                        WHEN COUNT(*) > 0
                        THEN 'TRUE' ELSE 'FALSE' END
                        FROM plan_factory pf
                        WHERE
                        pf.id_factory = :factoryId
                        AND
                        pf.status = 1
                        """, nativeQuery = true)
        boolean existsPlanByFactoryId(String factoryId);

}
