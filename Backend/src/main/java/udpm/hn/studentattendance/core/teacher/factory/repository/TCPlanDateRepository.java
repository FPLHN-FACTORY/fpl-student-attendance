package udpm.hn.studentattendance.core.teacher.factory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.response.SPDPlanDateResponse;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCFilterShiftFactoryRequest;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCPlanDateResponse;
import udpm.hn.studentattendance.repositories.PlanDateRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TCPlanDateRepository extends PlanDateRepository {

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
        JOIN facility f2 ON sf.id_facility = f2.id
        WHERE 
            f.status = 1 AND
            p.status = 1 AND
            sf.status = 1 AND
            f2.status = 1 AND
            sf.id_facility = :#{#request.idFacility} AND
            f.id = :#{#request.idFactory} AND
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
        JOIN facility f2 ON sf.id_facility = f2.id
        WHERE 
            f.status = 1 AND
            p.status = 1 AND
            sf.status = 1 AND
            f2.status = 1 AND
            sf.id_facility = :#{#request.idFacility} AND
            f.id = :#{#request.idFactory} AND
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
    Page<TCPlanDateResponse> getAllByFilter(Pageable pageable, TCFilterShiftFactoryRequest request);

}
