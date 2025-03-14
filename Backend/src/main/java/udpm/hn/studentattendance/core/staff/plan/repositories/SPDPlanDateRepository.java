package udpm.hn.studentattendance.core.staff.plan.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterCreatePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateDetailRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateDetailResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateResponse;
import udpm.hn.studentattendance.repositories.PlanDateRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SPDPlanDateRepository extends PlanDateRepository {

    @Query(value = """
        SELECT 
            ROW_NUMBER() OVER (ORDER BY MAX(pd.created_at) DESC) as orderNumber,
            f.id,
            f.name AS factoryName,
            p.name AS projectName,
            lp.name AS level,
            MIN(pd.start_date) AS fromDate,
            MAX(pd.start_date) AS toDate,
            CONCAT(s.name, ' - ', s.year) AS semesterName,
            s2.name AS subjectName,
            CONCAT(us.code, ' - ', us.name) AS staffName,
            COUNT(DISTINCT pd.id) AS totalShift,
            MAX(pd.created_at) AS lastUpdated
        FROM factory f
        JOIN project p ON p.id = f.id_project
        LEFT JOIN level_project lp ON lp.id = p.id_level_project
        LEFT JOIN semester s ON s.id = p.id_semester
        LEFT JOIN subject_facility sf ON sf.id = p.id_subject_facility
        LEFT JOIN subject s2 ON s2.id = sf.id_subject
        LEFT JOIN user_staff us ON us.id = f.id_user_staff
        JOIN plan_date pd ON pd.id_factory = f.id
        WHERE 
            f.status = 1 AND
            p.status = 1 AND
            sf.status = 1 AND
            s.status = 1 AND
            pd.id IS NOT NULL AND
            sf.id_facility = :#{#request.idFacility} AND
            (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR 
                f.name LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%') OR
                p.name LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')) AND
            (:#{#request.level} IS NULL OR lp.id = :#{#request.level}) AND
            (:#{#request.semester} IS NULL OR s.name = :#{#request.semester}) AND
            (:#{#request.year} IS NULL OR s.year = :#{#request.year}) AND
            (:#{#request.subject} IS NULL OR s2.id = :#{#request.subject})
        GROUP BY 
            f.id,
            f.name, 
            p.name, 
            lp.name, 
            s.name, 
            s.year, 
            s2.name, 
            us.code, 
            us.name
        ORDER BY lastUpdated DESC 
    """, countQuery = """
        SELECT 
            COUNT(DISTINCT f.id)
        FROM factory f
        JOIN project p ON p.id = f.id_project
        LEFT JOIN level_project lp ON lp.id = p.id_level_project
        LEFT JOIN semester s ON s.id = p.id_semester
        LEFT JOIN subject_facility sf ON sf.id = p.id_subject_facility
        LEFT JOIN subject s2 ON s2.id = sf.id_subject
        LEFT JOIN user_staff us ON us.id = f.id_user_staff
        JOIN plan_date pd ON pd.id_factory = f.id
        WHERE 
            f.status = 1 AND
            p.status = 1 AND
            sf.status = 1 AND
            s.status = 1 AND
            pd.id IS NOT NULL AND
            sf.id_facility = :#{#request.idFacility} AND
            (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR 
                f.name LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%') OR
                p.name LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')) AND
            (:#{#request.level} IS NULL OR lp.id = :#{#request.level}) AND
            (:#{#request.semester} IS NULL OR s.name = :#{#request.semester}) AND
            (:#{#request.year} IS NULL OR s.year = :#{#request.year}) AND
            (:#{#request.subject} IS NULL OR s2.id = :#{#request.subject})
    """, nativeQuery = true)
    Page<SPDPlanDateResponse> getAllByFilter(Pageable pageable, SPDFilterPlanDateRequest request);

    @Query(value = """
        SELECT 
            1 as orderNumber,
            f.id,
            f.name AS factoryName,
            p.name AS projectName,
            lp.name AS level,
            s.to_date AS toDate,
            s.from_date AS fromDate,
            CONCAT(s.name, ' - ', s.year) AS semesterName,
            s2.name AS subjectName,
            CONCAT(us.code, ' - ', us.name) AS staffName,
            COUNT(DISTINCT pd.id) AS totalShift
        FROM factory f
        JOIN project p ON p.id = f.id_project
        LEFT JOIN level_project lp ON lp.id = p.id_level_project
        LEFT JOIN semester s ON s.id = p.id_semester
        LEFT JOIN subject_facility sf ON sf.id = p.id_subject_facility
        LEFT JOIN subject s2 ON s2.id = sf.id_subject
        LEFT JOIN user_staff us ON us.id = f.id_user_staff
        JOIN plan_date pd ON pd.id_factory = f.id
        WHERE 
            f.status = 1 AND
            p.status = 1 AND
            sf.status = 1 AND
            s.status = 1 AND
            pd.id IS NOT NULL AND
            sf.id_facility = :idFacility AND
            f.id = :idFactory
        GROUP BY 
            f.id,
            f.created_at, 
            f.name, 
            p.name, 
            lp.name, 
            s.name, 
            s.year, 
            s2.name, 
            us.code, 
            us.name,
            s.to_date,
            s.from_date
    """, nativeQuery = true)
    Optional<SPDPlanDateResponse> getDetailByIdFactory(String idFactory, String idFacility);

    @Query(value = """
        SELECT 
            ROW_NUMBER() OVER (ORDER BY pd.start_date DESC) as orderNumber,
            pd.id,
            pd.start_date,
            pd.shift,
            pd.late_arrival,
            pd.description,
            CASE
                WHEN UNIX_TIMESTAMP(NOW()) * 1000 > (pd.start_date + 7200)
                THEN 'DA_DIEN_RA'
                ELSE 'CHUA_DIEN_RA'
            END AS status
        FROM plan_date pd
        JOIN factory f ON f.id = pd.id_factory
        JOIN project p ON p.id = f.id_project
        JOIN semester s ON p.id_semester = s.id
        JOIN subject_facility sf ON sf.id = p.id_subject_facility
        WHERE 
            f.status = 1 AND
            p.status = 1 AND
            sf.status = 1 AND
            s.status = 1 AND
            sf.id_facility = :#{#request.idFacility} AND
            f.id = :#{#request.idFactory} AND
            (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR pd.description LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')) AND
            (:#{#request.shift} IS NULL OR pd.shift = :#{#request.shift}) AND
            (:#{#request.startDate} IS NULL OR (
                DAY(FROM_UNIXTIME(pd.start_date / 1000)) = DAY(FROM_UNIXTIME(:#{#request.startDate} / 1000)) AND
                MONTH(FROM_UNIXTIME(pd.start_date / 1000)) = MONTH(FROM_UNIXTIME(:#{#request.startDate} / 1000)) AND
                YEAR(FROM_UNIXTIME(pd.start_date / 1000)) = YEAR(FROM_UNIXTIME(:#{#request.startDate} / 1000))
            )) AND
            (:#{#request.status} IS NULL OR (
                CASE
                    WHEN UNIX_TIMESTAMP(NOW()) * 1000 > (pd.start_date + 7200)
                    THEN 'DA_DIEN_RA'
                    ELSE 'CHUA_DIEN_RA'
                END
            ) = :#{#request.status})
        ORDER BY pd.start_date DESC
    """, countQuery = """
        SELECT 
            COUNT(pd.id)
        FROM plan_date pd
        JOIN factory f ON f.id = pd.id_factory
        JOIN project p ON p.id = f.id_project
        JOIN semester s ON p.id_semester = s.id
        JOIN subject_facility sf ON sf.id = p.id_subject_facility
        WHERE 
            f.status = 1 AND
            p.status = 1 AND
            sf.status = 1 AND
            s.status = 1 AND
            sf.id_facility = :#{#request.idFacility} AND
            f.id = :#{#request.idFactory} AND
            (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR pd.description LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')) AND
            (:#{#request.shift} IS NULL OR pd.shift = :#{#request.shift}) AND
            (:#{#request.startDate} IS NULL OR (
                DAY(FROM_UNIXTIME(pd.start_date / 1000)) = DAY(FROM_UNIXTIME(:#{#request.startDate} / 1000)) AND
                MONTH(FROM_UNIXTIME(pd.start_date / 1000)) = MONTH(FROM_UNIXTIME(:#{#request.startDate} / 1000)) AND
                YEAR(FROM_UNIXTIME(pd.start_date / 1000)) = YEAR(FROM_UNIXTIME(:#{#request.startDate} / 1000))
            )) AND
            (:#{#request.status} IS NULL OR (
                CASE
                    WHEN UNIX_TIMESTAMP(NOW()) * 1000 > (pd.start_date + 7200)
                    THEN 'DA_DIEN_RA'
                    ELSE 'CHUA_DIEN_RA'
                END
            ) = :#{#request.status})
    """, nativeQuery = true)
    Page<SPDPlanDateDetailResponse> getAllDetailByFilter(Pageable pageable, SPDFilterPlanDateDetailRequest request);

    @Query(value = """
        SELECT 
            1 as orderNumber,
            pd.id,
            pd.start_date,
            pd.shift,
            pd.late_arrival,
            s.to_date,
            s.from_date,
            CASE
                WHEN UNIX_TIMESTAMP(NOW()) * 1000 > (pd.start_date + 7200)
                THEN 'DA_DIEN_RA'
                ELSE 'CHUA_DIEN_RA'
            END AS status
        FROM plan_date pd
        JOIN factory f ON f.id = pd.id_factory
        JOIN project p ON p.id = f.id_project
        JOIN semester s ON p.id_semester = s.id
        JOIN subject_facility sf ON sf.id = p.id_subject_facility
        WHERE 
            f.status = 1 AND
            p.status = 1 AND
            sf.status = 1 AND
            s.status = 1 AND
            sf.id_facility = :idFacility AND
            pd.id = :idPlanDate
    """, nativeQuery = true)
    Optional<SPDPlanDateDetailResponse> getPlanDateById(String idPlanDate, String idFacility);

    @Modifying
    @Query(value = """
        DELETE pd
        FROM plan_date pd
        JOIN factory f ON f.id = pd.id_factory
        JOIN project p ON p.id = f.id_project
        JOIN subject_facility sf ON sf.id = p.id_subject_facility
        WHERE
            sf.id_facility = :idFacility AND
            pd.id = :idPlanDate AND
            UNIX_TIMESTAMP(NOW()) * 1000 <= (pd.start_date + 7200)
    """, countQuery = "SELECT 1", nativeQuery = true)
    int deletePlanDateById(String idFacility, String idPlanDate);

    @Query(value = """
        SELECT 
            CASE WHEN COUNT(*) > 0 THEN 'TRUE' ELSE 'FALSE' END 
        FROM plan_date
        WHERE 
            status = 1 AND
            id_factory = :idFactory AND
            start_date = :startDate AND
            shift = :shift AND
            (:idPlanDate IS NULL OR id != :idPlanDate)
    """, nativeQuery = true)
    boolean isExistsShiftInFactory(String idFactory, String idPlanDate, Long startDate, Integer shift);

    @Query(value = """
        SELECT 
            CASE WHEN COUNT(*) > 0 THEN 'TRUE' ELSE 'FALSE' END 
        FROM plan_date pd
        JOIN factory f ON pd.id_factory = f.id
        WHERE 
            pd.status = 1 AND
            f.status = 1 AND
            f.id_user_staff = :idUserStaff AND
            pd.start_date = :startDate AND
            pd.shift = :shift AND
            (:idPlanDate IS NULL OR pd.id != :idPlanDate)
    """, nativeQuery = true)
    boolean isExistsShiftInPlanDate(String idUserStaff, String idPlanDate, Long startDate, Integer shift);

    @Query(value = """
        SELECT 
            ROW_NUMBER() OVER (ORDER BY f.created_at DESC) as orderNumber,
            f.id,
            f.name AS factoryName,
            p.name AS projectName,
            lp.name AS level,
            CONCAT(s.name, ' - ', s.year) AS semesterName,
            s2.name AS subjectName,
            CONCAT(us.code, ' - ', us.name) AS staffName,
            s.to_date AS toDate,
            s.from_date AS fromDate
        FROM factory f
        JOIN project p ON p.id = f.id_project
        LEFT JOIN level_project lp ON lp.id = p.id_level_project
        LEFT JOIN semester s ON s.id = p.id_semester
        LEFT JOIN subject_facility sf ON sf.id = p.id_subject_facility
        LEFT JOIN subject s2 ON s2.id = sf.id_subject
        LEFT JOIN user_staff us ON us.id = f.id_user_staff
        LEFT JOIN plan_date pd ON pd.id_factory = f.id
        WHERE 
            f.status = 1 AND
            p.status = 1 AND
            sf.status = 1 AND
            s.status = 1 AND
            pd.id IS NULL AND
            sf.id_facility = :#{#request.idFacility} AND
            (:#{#request.level} IS NULL OR lp.id = :#{#request.level}) AND
            (:#{#request.semester} IS NULL OR s.name = :#{#request.semester}) AND
            (:#{#request.year} IS NULL OR s.year = :#{#request.year}) AND
            (:#{#request.subject} IS NULL OR s2.id = :#{#request.subject})
        GROUP BY 
            f.created_at,
            f.id,
            f.name,
            p.name,
            us.code,
            us.name,
            s.to_date,
            s.from_date,
            lp.name,
            s2.name,
            s.year,
            s.name
        ORDER BY f.created_at DESC 
    """, nativeQuery = true)
    List<SPDPlanDateResponse> getListFactory(SPDFilterCreatePlanDateRequest request);

}
