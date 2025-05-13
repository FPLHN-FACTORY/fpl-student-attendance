package udpm.hn.studentattendance.core.staff.plan.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateResponse;
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
            CASE
                WHEN UNIX_TIMESTAMP(NOW()) * 1000 > pd.start_date
                THEN 'DA_DIEN_RA'
                ELSE 'CHUA_DIEN_RA'
            END AS status
        FROM plan_date pd
        JOIN plan_factory pf ON pd.id_plan_factory = pf.id
        JOIN factory f ON f.id = pf.id_factory
        JOIN project p ON p.id = f.id_project
        JOIN subject_facility sf ON sf.id = p.id_subject_facility
        WHERE 
            sf.id_facility = :#{#request.idFacility} AND
            pf.id = :#{#request.idPlanFactory} AND
            (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR pd.description LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')) AND
            (:#{#request.shift} IS NULL OR pd.shift = :#{#request.shift}) AND
            (:#{#request.type} IS NULL OR pd.type = :#{#request.type}) AND
            (:#{#request.startDate} IS NULL OR (
                DAY(FROM_UNIXTIME(pd.start_date / 1000)) = DAY(FROM_UNIXTIME(:#{#request.startDate} / 1000)) AND
                MONTH(FROM_UNIXTIME(pd.start_date / 1000)) = MONTH(FROM_UNIXTIME(:#{#request.startDate} / 1000)) AND
                YEAR(FROM_UNIXTIME(pd.start_date / 1000)) = YEAR(FROM_UNIXTIME(:#{#request.startDate} / 1000))
            )) AND
            (:#{#request.status} IS NULL OR (
                CASE
                    WHEN UNIX_TIMESTAMP(NOW()) * 1000 > pd.start_date
                    THEN 'DA_DIEN_RA'
                    ELSE 'CHUA_DIEN_RA'
                END
            ) = :#{#request.status})
        ORDER BY pd.start_date ASC
    """, countQuery = """
        SELECT 
            COUNT(pd.id)
        FROM plan_date pd
        JOIN plan_factory pf ON pd.id_plan_factory = pf.id
        JOIN factory f ON f.id = pf.id_factory
        JOIN project p ON p.id = f.id_project
        JOIN subject_facility sf ON sf.id = p.id_subject_facility
        WHERE 
            sf.id_facility = :#{#request.idFacility} AND
            pf.id = :#{#request.idPlanFactory} AND
            (NULLIF(TRIM(:#{#request.keyword}), '') IS NULL OR pd.description LIKE CONCAT('%', TRIM(:#{#request.keyword}), '%')) AND
            (:#{#request.shift} IS NULL OR pd.shift = :#{#request.shift}) AND
            (:#{#request.type} IS NULL OR pd.type = :#{#request.type}) AND
            (:#{#request.startDate} IS NULL OR (
                DAY(FROM_UNIXTIME(pd.start_date / 1000)) = DAY(FROM_UNIXTIME(:#{#request.startDate} / 1000)) AND
                MONTH(FROM_UNIXTIME(pd.start_date / 1000)) = MONTH(FROM_UNIXTIME(:#{#request.startDate} / 1000)) AND
                YEAR(FROM_UNIXTIME(pd.start_date / 1000)) = YEAR(FROM_UNIXTIME(:#{#request.startDate} / 1000))
            )) AND
            (:#{#request.status} IS NULL OR (
                CASE
                    WHEN UNIX_TIMESTAMP(NOW()) * 1000 > pd.start_date
                    THEN 'DA_DIEN_RA'
                    ELSE 'CHUA_DIEN_RA'
                END
            ) = :#{#request.status})
    """, nativeQuery = true)
    Page<SPDPlanDateResponse> getAllByFilter(Pageable pageable, SPDFilterPlanDateRequest request);

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
                WHEN UNIX_TIMESTAMP(NOW()) * 1000 > pd.start_date
                THEN 'DA_DIEN_RA'
                ELSE 'CHUA_DIEN_RA'
            END AS status
        FROM plan_date pd
        JOIN plan_factory pf ON pd.id_plan_factory = pf.id
        JOIN plan pl ON pl.id = pf.id_plan
        JOIN factory f ON f.id = pf.id_factory
        JOIN project p ON p.id = f.id_project
        JOIN subject_facility sf ON sf.id = p.id_subject_facility
        WHERE 
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
        JOIN factory f ON pd.id_plan_factory = f.id
        WHERE 
            pd.status = 1 AND
            f.status = 1 AND
            f.id_user_staff = :idUserStaff AND
            pd.start_date <= :endDate AND
            pd.end_date >= :startDate
    """, nativeQuery = true)
    boolean isExistsTeacherOnShift(String idUserStaff, Long startDate, Long endDate);


}
