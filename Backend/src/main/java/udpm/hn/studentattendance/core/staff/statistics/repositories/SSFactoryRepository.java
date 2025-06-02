package udpm.hn.studentattendance.core.staff.statistics.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.statistics.model.request.SSFilterFactoryStatsRequest;
import udpm.hn.studentattendance.core.staff.statistics.model.response.SSFactoryStatsResponse;
import udpm.hn.studentattendance.core.staff.statistics.model.response.SSPlanDateStudentFactoryResponse;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.repositories.FactoryRepository;

import java.util.List;

@Repository
public interface SSFactoryRepository extends FactoryRepository {

    @Query(value = """
                SELECT
                    ROW_NUMBER() OVER (ORDER BY f.name ASC) as orderNumber,
                    f.id,
                    p.id AS projectId,
                    p.name AS projectName,
                    f.name AS factoryName,
                    lp.name AS levelProject,
                    s2.name AS subjectName,
                    COUNT(DISTINCT pd.id) AS totalShift,
                    COUNT(DISTINCT CASE WHEN pd.end_date <= UNIX_TIMESTAMP(NOW()) * 1000 THEN pd.id END) AS totalCurrentShift,
                    (SELECT COUNT(usf.id) FROM user_student_factory usf WHERE f.id = usf.id_factory AND usf.status = 1) AS totalStudent,
                    pl.status AS status
                FROM factory f
                JOIN plan_factory pf ON f.id = pf.id_factory
                JOIN plan pl ON pf.id_plan = pl.id
                JOIN project p ON p.id = f.id_project
                JOIN level_project lp ON p.id_level_project = lp.id
                JOIN semester s ON p.id_semester = s.id
                JOIN subject_facility sf ON sf.id = p.id_subject_facility
                JOIN subject s2 ON sf.id_subject = s2.id
                LEFT JOIN plan_date pd ON pd.id_plan_factory = pf.id
                WHERE
                    pf.status = 1 AND
                    p.status = 1 AND
                    f.status = 1 AND
                    sf.status = 1 AND
                    lp.status = 1 AND
                    s.status = 1 AND
                    s2.status = 1 AND
                    s.id = :#{#request.idSemester} AND
                    sf.id_facility = :#{#request.idFacility}
                GROUP BY
                    f.name, f.id, p.id, p.name, lp.name, s2.name, pl.status
                ORDER BY pl.status DESC, f.name ASC
            """, countQuery = """
                SELECT
                    COUNT(DISTINCT f.id)
                FROM factory f
                JOIN plan_factory pf ON f.id = pf.id_factory
                JOIN plan pl ON pf.id_plan = pl.id
                JOIN project p ON p.id = f.id_project
                JOIN level_project lp ON p.id_level_project = lp.id
                JOIN semester s ON p.id_semester = s.id
                JOIN subject_facility sf ON sf.id = p.id_subject_facility
                JOIN subject s2 ON sf.id_subject = s2.id
                WHERE
                    pf.status = 1 AND
                    p.status = 1 AND
                    f.status = 1 AND
                    sf.status = 1 AND
                    lp.status = 1 AND
                    s.status = 1 AND
                    s2.status = 1 AND
                    s.id = :#{#request.idSemester} AND
                    sf.id_facility = :#{#request.idFacility}
            """, nativeQuery = true)
    Page<SSFactoryStatsResponse> getAllByFilter(Pageable pageable, SSFilterFactoryStatsRequest request);

    @Query(value = """
                SELECT
                    f.*
                FROM factory f
                WHERE
                    EXISTS(
                        SELECT 1
                        FROM plan_factory pf
                        JOIN plan pl ON pf.id_plan = pl.id
                        JOIN project p ON p.id = f.id_project
                        JOIN level_project lp ON p.id_level_project = lp.id
                        JOIN semester s ON p.id_semester = s.id
                        JOIN subject_facility sf ON sf.id = p.id_subject_facility
                        JOIN subject s2 ON sf.id_subject = s2.id
                        WHERE
                            f.id = pf.id_factory AND
                            pf.status = 1 AND
                            p.status = 1 AND
                            sf.status = 1 AND
                            lp.status = 1 AND
                            s.status = 1 AND
                            s2.status = 1 AND
                            pl.status = 1 AND
                            s.id = :idSemester AND
                            sf.id_facility = :idFacility
                    ) AND
                    f.status = 1
                ORDER BY f.name ASC
    """, nativeQuery = true)
    List<Factory> getAllFactoryBySemester(String idSemester, String idFacility);

    @Query(value = """
        SELECT
            ROW_NUMBER() OVER (ORDER BY us.name ASC) AS orderNumber,
            pd.id,
            us.name,
            us.code,
            pd.start_date,
            pd.end_date,
            pd.shift,
            pd.required_checkin,
            pd.required_checkout,
            COALESCE(a.attendance_status, 0) AS status,
            COALESCE(a.created_at, 0) AS createdAt,
            COALESCE(a.updated_at, 0) AS updatedAt,
            a.late_checkout,
            a.late_checkin
        FROM plan_date pd
        JOIN plan_factory pf ON pd.id_plan_factory = pf.id
        JOIN user_student_factory usf ON pf.id_factory = usf.id_factory
        JOIN user_student us ON usf.id_user_student = us.id
        LEFT JOIN attendance a ON pd.id = a.id_plan_date AND a.id_user_student = us.id
        WHERE
            pd.status = 1 AND
            pf.status = 1 AND
            us.status = 1 AND
            usf.status = 1 AND
            pf.id_factory = :idFactory AND
            pd.start_date >= :startDate AND
            pd.end_date <= :endDate AND
            EXISTS(
                SELECT 1
                FROM plan p
                JOIN factory f ON f.id = pf.id_factory
                JOIN project pj ON f.id_project = pj.id
                JOIN subject_facility sf ON sf.id = pj.id_subject_facility
                JOIN subject s2 ON s2.id = sf.id_subject
                JOIN facility f2 ON sf.id_facility = f2.id
                JOIN semester s ON pj.id_semester = s.id
                WHERE
                     pf.id_plan = p.id AND
                     f.status = 1 AND
                     p.status = 1 AND
                     pj.status = 1 AND
                     s.status = 1 AND
                     s2.status = 1 AND
                     f2.status = 1 AND
                     sf.status = 1
            )
        ORDER BY us.name ASC
    """, nativeQuery = true)
    List<SSPlanDateStudentFactoryResponse> getAllPlanDateAttendanceByIdFactory(String idFactory, Long startDate, Long endDate);

}
