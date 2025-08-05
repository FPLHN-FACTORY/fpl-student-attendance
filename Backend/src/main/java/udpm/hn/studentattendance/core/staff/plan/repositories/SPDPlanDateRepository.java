package udpm.hn.studentattendance.core.staff.plan.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateGroupResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateResponse;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDUserStudentResponse;
import udpm.hn.studentattendance.repositories.PlanDateRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SPDPlanDateRepository extends PlanDateRepository {

    @Query(value = """
        SELECT
            ROW_NUMBER() OVER (ORDER BY pd.start_date ASC) as orderNumber,
            pd.id,
            pd.start_date,
            pd.end_date,
            pd.shift,
            pd.type,
            pd.late_arrival,
            pd.description,
            pd.link,
            pd.room,
            pd.required_location,
            pd.required_ip,
            pd.required_checkin,
            pd.required_checkout,
            us.id AS idTeacher,
            CONCAT(us.code, ' - ', us.name) AS nameTeacher,
            CASE
                WHEN UNIX_TIMESTAMP(NOW()) * 1000 > pd.end_date THEN 'DA_DIEN_RA'
                WHEN UNIX_TIMESTAMP(NOW()) * 1000 < pd.start_date THEN 'CHUA_DIEN_RA'
                ELSE 'DANG_DIEN_RA'
            END AS status
        FROM plan_date pd
        JOIN plan_factory pf ON pd.id_plan_factory = pf.id
        JOIN factory f ON f.id = pf.id_factory
        JOIN project p ON p.id = f.id_project
        JOIN subject_facility sf ON sf.id = p.id_subject_facility
        JOIN facility f2 ON sf.id_facility = f2.id
        LEFT JOIN user_staff us ON pd.id_user_staff = us.id
        WHERE
            pd.status = 1 AND
            f2.status = 1 AND
            sf.id_facility = :#{#request.idFacility} AND
            pf.id = :#{#request.idPlanFactory} AND
            (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR pd.description LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')) AND
            (:#{#request.shift} IS NULL OR FIND_IN_SET(:#{#request.shift}, pd.shift)) AND
            (:#{#request.type} IS NULL OR pd.type = :#{#request.type}) AND
            (:#{#request.startDate} IS NULL OR (
                DAY(FROM_UNIXTIME(pd.start_date / 1000)) = DAY(FROM_UNIXTIME(:#{#request.startDate} / 1000)) AND
                MONTH(FROM_UNIXTIME(pd.start_date / 1000)) = MONTH(FROM_UNIXTIME(:#{#request.startDate} / 1000)) AND
                YEAR(FROM_UNIXTIME(pd.start_date / 1000)) = YEAR(FROM_UNIXTIME(:#{#request.startDate} / 1000))
            )) AND
            (:#{#request.status} IS NULL OR (
                CASE
                    WHEN UNIX_TIMESTAMP(NOW()) * 1000 > pd.end_date THEN 'DA_DIEN_RA'
                    WHEN UNIX_TIMESTAMP(NOW()) * 1000 < pd.start_date THEN 'CHUA_DIEN_RA'
                    ELSE 'DANG_DIEN_RA'
                END
            ) = :#{#request.status}) AND
            DATE_FORMAT(FROM_UNIXTIME(pd.start_date / 1000), '%d/%m/%Y') = :day
        ORDER BY pd.start_date ASC
    """, nativeQuery = true)
    List<SPDPlanDateResponse> getAllByFilter(String day, SPDFilterPlanDateRequest request);

    @Query(value = """
        SELECT
            MAX(pd.id) AS id,
            ROW_NUMBER() OVER (ORDER BY MIN(pd.start_date)) AS orderNumber,
            DATE_FORMAT(FROM_UNIXTIME(MIN(pd.start_date) / 1000), '%d/%m/%Y') AS day,
            MIN(pd.start_date) AS startDate,
            DATE(FROM_UNIXTIME(pd.start_date / 1000)) AS group_date,
            COUNT(*) AS totalShift,
            CASE
                WHEN UNIX_TIMESTAMP(NOW()) * 1000 > MIN(pd.end_date) THEN 'DA_DIEN_RA'
                WHEN UNIX_TIMESTAMP(NOW()) * 1000 < MIN(pd.start_date) THEN 'CHUA_DIEN_RA'
                ELSE 'DANG_DIEN_RA'
            END AS status
        FROM plan_date pd
        JOIN plan_factory pf ON pd.id_plan_factory = pf.id
        JOIN factory f ON f.id = pf.id_factory
        JOIN project p ON p.id = f.id_project
        JOIN subject_facility sf ON sf.id = p.id_subject_facility
        JOIN facility f2 ON sf.id_facility = f2.id
        WHERE
            pd.status = 1 AND
            f2.status = 1 AND
            sf.id_facility = :#{#request.idFacility} AND
            pf.id = :#{#request.idPlanFactory} AND
            (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR pd.description LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')) AND
            (:#{#request.shift} IS NULL OR FIND_IN_SET(:#{#request.shift}, pd.shift)) AND
            (:#{#request.type} IS NULL OR pd.type = :#{#request.type}) AND
            (:#{#request.startDate} IS NULL OR (
                DAY(FROM_UNIXTIME(pd.start_date / 1000)) = DAY(FROM_UNIXTIME(:#{#request.startDate} / 1000)) AND
                MONTH(FROM_UNIXTIME(pd.start_date / 1000)) = MONTH(FROM_UNIXTIME(:#{#request.startDate} / 1000)) AND
                YEAR(FROM_UNIXTIME(pd.start_date / 1000)) = YEAR(FROM_UNIXTIME(:#{#request.startDate} / 1000))
            )) AND
            (:#{#request.status} IS NULL OR (
                CASE
                    WHEN UNIX_TIMESTAMP(NOW()) * 1000 > pd.end_date THEN 'DA_DIEN_RA'
                    WHEN UNIX_TIMESTAMP(NOW()) * 1000 < pd.start_date THEN 'CHUA_DIEN_RA'
                    ELSE 'DANG_DIEN_RA'
                END
            ) = :#{#request.status})
        GROUP BY group_date
        ORDER BY MIN(pd.start_date) ASC
    """, countQuery = """
        SELECT COUNT(*) FROM (
            SELECT
                MAX(pd.id) AS id,
                DATE(FROM_UNIXTIME(pd.start_date / 1000)) AS group_date
            FROM plan_date pd
            JOIN plan_factory pf ON pd.id_plan_factory = pf.id
            JOIN factory f ON f.id = pf.id_factory
            JOIN project p ON p.id = f.id_project
            JOIN subject_facility sf ON sf.id = p.id_subject_facility
            JOIN facility f2 ON sf.id_facility = f2.id
            WHERE
                pd.status = 1 AND
                f2.status = 1 AND
                sf.id_facility = :#{#request.idFacility} AND
                pf.id = :#{#request.idPlanFactory} AND
                (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR pd.description LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')) AND
                (:#{#request.shift} IS NULL OR FIND_IN_SET(:#{#request.shift}, pd.shift)) AND
                (:#{#request.type} IS NULL OR pd.type = :#{#request.type}) AND
                (:#{#request.startDate} IS NULL OR (
                    DATE(FROM_UNIXTIME(pd.start_date / 1000)) = DATE(FROM_UNIXTIME(:#{#request.startDate} / 1000))
                )) AND
                (:#{#request.status} IS NULL OR (
                    CASE
                        WHEN UNIX_TIMESTAMP(NOW()) * 1000 > pd.end_date THEN 'DA_DIEN_RA'
                        WHEN UNIX_TIMESTAMP(NOW()) * 1000 < pd.start_date THEN 'CHUA_DIEN_RA'
                        ELSE 'DANG_DIEN_RA'
                    END
                ) = :#{#request.status})
            GROUP BY group_date
        ) AS temp
    """, nativeQuery = true)
    Page<SPDPlanDateGroupResponse> getAllGroupByFilter(Pageable pageable, SPDFilterPlanDateRequest request);

    @Query(value = """
        SELECT
            ROW_NUMBER() OVER (ORDER BY pd.start_date ASC) as orderNumber,
            pd.id,
            pd.start_date,
            pd.end_date,
            pd.shift,
            pd.type,
            pd.late_arrival,
            pd.description,
            pd.link,
            pd.room,
            pd.required_location,
            pd.required_ip,
            pd.required_checkin,
            pd.required_checkout,
            CASE
                WHEN UNIX_TIMESTAMP(NOW()) * 1000 > pd.end_date THEN 'DA_DIEN_RA'
                WHEN UNIX_TIMESTAMP(NOW()) * 1000 < pd.start_date THEN 'CHUA_DIEN_RA'
                ELSE 'DANG_DIEN_RA'
            END AS status
        FROM plan_date pd
        JOIN plan_factory pf ON pd.id_plan_factory = pf.id
        JOIN factory f ON f.id = pf.id_factory
        JOIN project p ON p.id = f.id_project
        JOIN subject_facility sf ON sf.id = p.id_subject_facility
        JOIN subject s ON sf.id_subject = s.id
        JOIN semester sem ON p.id_semester = sem.id
        JOIN facility f2 ON sf.id_facility = f2.id
        WHERE 
            f.status = 1 AND
            p.status = 1 AND
            sf.status = 1 AND
            f2.status = 1 AND
            s.status = 1 AND
            sem.status = 1 AND
            pf.id = :idPlanFactory AND
            pd.start_date > UNIX_TIMESTAMP(NOW()) * 1000 
        ORDER BY pd.start_date ASC
    """, nativeQuery = true)
    List<SPDPlanDateResponse> getAllListNotRunning(String idPlanFactory);

    @Query(value = """
        SELECT 
            1 as orderNumber,
            pd.id,
            pd.start_date,
            pd.end_date,
            pd.shift,
            pd.late_arrival,
            pl.to_date,
            pl.from_date,
            pd.link,
            pd.room,
            pd.required_location,
            pd.required_ip,
            pd.required_checkin,
            pd.required_checkout,
            CASE
                WHEN UNIX_TIMESTAMP(NOW()) * 1000 > pd.end_date THEN 'DA_DIEN_RA'
                WHEN UNIX_TIMESTAMP(NOW()) * 1000 < pd.start_date THEN 'CHUA_DIEN_RA'
                ELSE 'DANG_DIEN_RA'
            END AS status
        FROM plan_date pd
        JOIN plan_factory pf ON pd.id_plan_factory = pf.id
        JOIN plan pl ON pl.id = pf.id_plan
        JOIN factory f ON f.id = pf.id_factory
        JOIN project p ON p.id = f.id_project
        JOIN subject_facility sf ON sf.id = p.id_subject_facility
        JOIN facility f2 ON sf.id_facility = f2.id
        WHERE
            pd.status = 1 AND
            f2.status = 1 AND
            sf.id_facility = :idFacility AND
            pd.id = :idPlanDate
    """, nativeQuery = true)
    Optional<SPDPlanDateResponse> getPlanDateById(String idPlanDate, String idFacility);

    @Modifying
    @Transactional
    @Query(value = """
        DELETE pd
        FROM plan_date pd
        JOIN plan_factory pf ON pd.id_plan_factory = pf.id
        JOIN factory f ON f.id = pf.id_factory
        JOIN project p ON p.id = f.id_project
        JOIN subject_facility sf ON sf.id = p.id_subject_facility
        WHERE
            sf.id_facility = :idFacility AND
            pd.id IN(:idPlanDates) AND
            UNIX_TIMESTAMP(NOW()) * 1000 <= pd.end_date
    """, countQuery = "SELECT 1", nativeQuery = true)
    int deletePlanDateById(String idFacility, List<String> idPlanDates);

    @Modifying
    @Transactional
    @Query(value = """
        DELETE pd
        FROM plan_date pd
        JOIN plan_factory pf ON pd.id_plan_factory = pf.id
        JOIN factory f ON f.id = pf.id_factory
        JOIN project p ON p.id = f.id_project
        JOIN subject_facility sf ON sf.id = p.id_subject_facility
        WHERE
            sf.id_facility = :idFacility AND
            DATE_FORMAT(FROM_UNIXTIME(pd.start_date / 1000), '%d/%m/%Y') IN(:days) AND
            UNIX_TIMESTAMP(NOW()) * 1000 <= pd.end_date
    """, countQuery = "SELECT 1", nativeQuery = true)
    int deletePlanDateByDay(String idFacility, List<String> days);

    @Query(value = """
        SELECT 
            CASE WHEN COUNT(*) > 0 THEN 'TRUE' ELSE 'FALSE' END 
        FROM plan_date
        WHERE 
            status = 1 AND
            id_plan_factory = :idPlanFactory AND
            start_date <= :endDate AND
            end_date >= :startDate AND
            (:idPlanDate IS NULL OR id != :idPlanDate)
    """, nativeQuery = true)
    boolean isExistsShiftInFactory(String idPlanFactory, String idPlanDate, Long startDate, Long endDate);

    @Query(value = """
        SELECT
            CASE WHEN COUNT(*) > 0 THEN 'TRUE' ELSE 'FALSE' END
        FROM plan_date pd
        JOIN plan_factory pf ON pd.id_plan_factory = pf.id
        JOIN plan p ON pf.id_plan = p.id
        JOIN factory f ON pf.id_factory = f.id
        JOIN project pj ON f.id_project = pj.id
        JOIN subject_facility sf ON sf.id = pj.id_subject_facility
        JOIN subject s2 ON sf.id_subject = s2.id
        JOIN semester s ON pj.id_semester = s.id
        JOIN level_project lp ON pj.id_level_project = lp.id
        JOIN facility f2 ON sf.id_facility = f2.id
        WHERE
            pd.status = 1 AND
            pf.status = 1 AND
            p.status = 1 AND
            f.status = 1 AND
            pj.status = 1 AND
            sf.status = 1 AND
            s2.status = 1 AND
            s.status = 1 AND
            lp.status = 1 AND
            s2.status = 1 AND
            (:idPlanDate IS NULL OR pd.id != :idPlanDate) AND
            f.id_user_staff = :idUserStaff AND
            pd.start_date <= :endDate AND
            pd.end_date >= :startDate
    """, nativeQuery = true)
    boolean isExistsTeacherOnShift(String idUserStaff, Long startDate, Long endDate, String idPlanDate);

    @Query(value = """
        SELECT
            CASE WHEN COUNT(*) > 0 THEN 'TRUE' ELSE 'FALSE' END
        FROM plan_date pd
        JOIN plan_factory pf ON pd.id_plan_factory = pf.id
        JOIN plan p ON pf.id_plan = p.id
        JOIN factory f ON pf.id_factory = f.id
        JOIN project pj ON f.id_project = pj.id
        JOIN subject_facility sf ON sf.id = pj.id_subject_facility
        JOIN subject s2 ON sf.id_subject = s2.id
        JOIN semester s ON pj.id_semester = s.id
        JOIN level_project lp ON pj.id_level_project = lp.id
        JOIN facility f2 ON sf.id_facility = f2.id
        WHERE
            pd.status = 1 AND
            pf.status = 1 AND
            p.status = 1 AND
            f.status = 1 AND
            pj.status = 1 AND
            sf.status = 1 AND
            s2.status = 1 AND
            s.status = 1 AND
            lp.status = 1 AND
            s2.status = 1 AND
            (:idPlanDate IS NULL OR pd.id != :idPlanDate) AND
            pd.room LIKE :room AND
            pd.start_date <= :endDate AND
            pd.end_date >= :startDate
    """, nativeQuery = true)
    boolean isExistsRoomOnShift(String room, Long startDate, Long endDate, String idPlanDate);

    @Query(value = """
        SELECT DISTINCT
            us.id,
            us.code,
            us.name,
            us.email,
            f.name AS factoryName,
            p.name AS planName,
            pd.id AS planDateId
        FROM user_student us
        JOIN user_student_factory usf2 ON usf2.id_user_student = us.id
        JOIN factory f ON f.id = usf2.id_factory
        JOIN plan_factory pf ON pf.id_factory = f.id
        JOIN plan p ON pf.id_plan = p.id
        JOIN plan_date pd ON pd.id_plan_factory = pf.id
        JOIN project pj ON f.id_project = pj.id
        JOIN subject_facility sf ON sf.id = pj.id_subject_facility
        JOIN subject s2 ON sf.id_subject = s2.id
        JOIN semester s ON pj.id_semester = s.id
        JOIN level_project lp ON pj.id_level_project = lp.id
        JOIN facility f2 ON sf.id_facility = f2.id
        WHERE
            us.status = 1 AND
            usf2.status = 1 AND
            f.status = 1 AND
            pf.status = 1 AND
            p.status = 1 AND
            pd.status = 1 AND
            pj.status = 1 AND
            s2.status = 1 AND
            s.status = 1 AND
            lp.status = 1 AND
            f2.status = 1 AND
            (:idPlanDate IS NULL OR pd.id != :idPlanDate) AND
            pd.start_date >= (UNIX_TIMESTAMP(CURRENT_DATE) * 1000) AND
            pd.start_date <= :startDate AND
            pd.end_date >= :endDate AND
            EXISTS (
                SELECT 1
                FROM user_student_factory usf_check
                WHERE
                    usf_check.id_user_student = us.id AND
                    usf_check.id_factory = :idFactory AND
                    usf_check.status = 1
            ) AND
            f.id <> :idFactory
    """, nativeQuery = true)
    List<SPDUserStudentResponse> getListExistsStudentOnShift(String idFactory, long startDate, long endDate, String idPlanDate);

    @Transactional
    @Modifying
    @Query(value = """
        UPDATE plan_date
        SET link = :link
        WHERE
            id_plan_factory = :idPlanFactory AND
            type = 1
    """, nativeQuery = true)
    Integer updateAllLinkMeet(String idPlanFactory, String link);

}
