package udpm.hn.studentattendance.infrastructure.common.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.repositories.PlanDateRepository;

@Repository
public interface CommonPlanDateRepository extends PlanDateRepository {

    @Query(value = """
            SELECT IF(COUNT(pd.id) > 0, 'TRUE', 'FALSE')
            FROM plan_date pd
            WHERE
                EXISTS(
                    SELECT 1
                    FROM plan_factory pf
                    JOIN plan p ON pf.id_plan = p.id
                    JOIN project pj ON p.id_project = pj.id
                    JOIN subject_facility sf ON pj.id_subject_facility = sf.id
                    JOIN facility f ON sf.id_facility = f.id
                    WHERE
                        pf.id = pd.id_plan_factory
                        AND f.id = :idFacility
                )
                AND pd.end_date > UNIX_TIMESTAMP(NOW()) * 1000
            """, nativeQuery = true)
    boolean existsNotYetStartedByFacility(String idFacility);

    @Query(value = """
            SELECT IF(COUNT(pd.id) > 0, 'TRUE', 'FALSE')
            FROM plan_date pd
            WHERE
                EXISTS(
                    SELECT 1
                    FROM plan_factory pf
                    JOIN plan p ON pf.id_plan = p.id
                    JOIN project pj ON p.id_project = pj.id
                    JOIN subject_facility sf ON pj.id_subject_facility = sf.id
                    JOIN subject s ON sf.id_subject = s.id
                    WHERE
                        pf.id = pd.id_plan_factory
                        AND s.id = :idSubject
                )
                AND pd.end_date > UNIX_TIMESTAMP(NOW()) * 1000
            """, nativeQuery = true)
    boolean existsNotYetStartedBySubject(String idSubject);

    @Query(value = """
            SELECT IF(COUNT(pd.id) > 0, 'TRUE', 'FALSE')
            FROM plan_date pd
            WHERE
                EXISTS(
                    SELECT 1
                    FROM plan_factory pf
                    JOIN plan p ON pf.id_plan = p.id
                    JOIN project pj ON p.id_project = pj.id
                    JOIN subject_facility sf ON pj.id_subject_facility = sf.id
                    WHERE
                        pf.id = pd.id_plan_factory
                        AND sf.id = :idSubjectFacility
                )
                AND pd.end_date > UNIX_TIMESTAMP(NOW()) * 1000
            """, nativeQuery = true)
    boolean existsNotYetStartedBySubjectFacility(String idSubjectFacility);

    @Query(value = """
            SELECT IF(COUNT(pd.id) > 0, 'TRUE', 'FALSE')
            FROM plan_date pd
            WHERE
                EXISTS(
                    SELECT 1
                    FROM plan_factory pf
                    JOIN plan p ON pf.id_plan = p.id
                    JOIN project pj ON p.id_project = pj.id
                    JOIN level_project lp ON pj.id_level_project = lp.id
                    WHERE
                        pf.id = pd.id_plan_factory
                        AND lp.id = :idLevelProject
                )
                AND pd.end_date > UNIX_TIMESTAMP(NOW()) * 1000
            """, nativeQuery = true)
    boolean existsNotYetStartedByLevelProject(String idLevelProject);

    @Query(value = """
            SELECT IF(COUNT(pd.id) > 0, 'TRUE', 'FALSE')
            FROM plan_date pd
            WHERE
                EXISTS(
                    SELECT 1
                    FROM plan_factory pf
                    JOIN plan p ON pf.id_plan = p.id
                    JOIN project pj ON p.id_project = pj.id
                    WHERE
                        pf.id = pd.id_plan_factory
                        AND pj.id = :idProject
                )
                AND pd.end_date > UNIX_TIMESTAMP(NOW()) * 1000
            """, nativeQuery = true)
    boolean existsNotYetStartedByProject(String idProject);

    @Query(value = """
            SELECT IF(COUNT(pd.id) > 0, 'TRUE', 'FALSE')
            FROM plan_date pd
            WHERE
                EXISTS(
                    SELECT 1
                    FROM plan_factory pf
                    JOIN factory f ON pf.id_factory = f.id
                    WHERE
                        pf.id = pd.id_plan_factory
                        AND f.id = :idFactory
                )
                AND pd.end_date > UNIX_TIMESTAMP(NOW()) * 1000
            """, nativeQuery = true)
    boolean existsNotYetStartedByFactory(String idFactory);

    @Query(value = """
            SELECT IF(COUNT(pd.id) > 0, 'TRUE', 'FALSE')
            FROM plan_date pd
            WHERE
                EXISTS(
                    SELECT 1
                    FROM plan_factory pf
                    JOIN plan p ON pf.id_plan = p.id
                    WHERE
                        pf.id = pd.id_plan_factory
                        AND p.id = :idPlan
                )
                AND pd.end_date > UNIX_TIMESTAMP(NOW()) * 1000
            """, nativeQuery = true)
    boolean existsNotYetStartedByPlan(String idPlan);

    @Query(value = """
            SELECT IF(COUNT(pd.id) > 0, 'TRUE', 'FALSE')
            FROM plan_date pd
            WHERE
                EXISTS(
                    SELECT 1
                    FROM plan_factory pf
                    WHERE
                        pf.id = pd.id_plan_factory
                        AND pf.id = :idPlanFactory
                )
                AND pd.end_date > UNIX_TIMESTAMP(NOW()) * 1000
            """, nativeQuery = true)
    boolean existsNotYetStartedByPlanFactory(String idPlanFactory);

}
