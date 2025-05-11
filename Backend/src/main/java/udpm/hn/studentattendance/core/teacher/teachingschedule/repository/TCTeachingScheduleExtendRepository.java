package udpm.hn.studentattendance.core.teacher.teachingschedule.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.teacher.teachingschedule.model.request.TCTeachingScheduleRequest;
import udpm.hn.studentattendance.core.teacher.teachingschedule.model.response.TCTSDetailPlanDateResponse;
import udpm.hn.studentattendance.core.teacher.teachingschedule.model.response.TCTeachingScheduleResponse;
import udpm.hn.studentattendance.entities.PlanDate;
import udpm.hn.studentattendance.repositories.PlanDateRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TCTeachingScheduleExtendRepository extends PlanDateRepository {

    @Query(value = """
            SELECT 
                pd.id AS idPlanDate,
                pd.start_date AS startTeaching,
                pd.end_date AS endTeaching,
                pd.shift AS shift,
                pd.type AS type,
                pd.link AS link,
                sb.code AS subjectCode,
                ft.name AS factoryName,
                ft.id AS factoryId,
                us.id AS userId,
                CONCAT(p.name, ' - ', lp.name) AS projectName,
                pd.late_arrival AS lateArrival,
                pd.room as room,
                pd.description AS description
            FROM
                plan_date pd
                LEFT JOIN plan_factory pf ON pf.id = pd.id_plan_factory
                LEFT JOIN factory ft ON ft.id = pf.id_factory
                LEFT JOIN user_staff us ON us.id = ft.id_user_staff
                LEFT JOIN project p ON p.id = ft.id_project
                LEFT JOIN subject_facility sf ON sf.id = p.id_subject_facility
                LEFT JOIN facility f ON f.id = sf.id_facility
                LEFT JOIN subject sb ON sb.id = sf.id_subject
                LEFT JOIN level_project lp ON lp.id = p.id_level_project
            WHERE
                us.id = :userId
                AND us.status = 1
                AND pd.status = 1
                AND ft.status = 1
                AND p.status = 1
                AND sf.status = 1
                AND sb.status = 1
                AND pf.status = 1
            AND pd.start_date BETWEEN :#{#teachingScheduleRequest.startDate} AND :#{#teachingScheduleRequest.endDate}
                AND (:#{#teachingScheduleRequest.idSubject} IS NULL OR sb.id = :#{#teachingScheduleRequest.idSubject})
                AND (:#{#teachingScheduleRequest.idFactory} IS NULL OR ft.id = :#{#teachingScheduleRequest.idFactory})
                AND (:#{#teachingScheduleRequest.idProject} IS NULL OR p.id = :#{#teachingScheduleRequest.idProject})
                AND (:#{#teachingScheduleRequest.shift} IS NULL OR pd.shift = :#{#teachingScheduleRequest.shift})
                AND (:#{#teachingScheduleRequest.shiftType} IS NULL OR pd.type = :#{#teachingScheduleRequest.shiftType})
            ORDER BY pd.start_date ASC
            """, countQuery = """
            SELECT COUNT(*)
            FROM
                plan_date pd
                LEFT JOIN plan_factory pf ON pf.id = pd.id_plan_factory
                LEFT JOIN factory ft ON ft.id = pf.id_factory
                LEFT JOIN user_staff us ON us.id = ft.id_user_staff
                LEFT JOIN project p ON p.id = ft.id_project
                LEFT JOIN subject_facility sf ON sf.id = p.id_subject_facility
                LEFT JOIN subject sb ON sb.id = sf.id_subject
                LEFT JOIN facility f ON f.id = sf.id_facility
            WHERE
                us.id = :userId
                AND us.status = 1
                AND pd.status = 1
                AND ft.status = 1
                AND p.status = 1
                AND sf.status = 1
                AND sb.status = 1
                AND pf.status = 1
                AND pd.start_date BETWEEN :#{#teachingScheduleRequest.startDate} AND :#{#teachingScheduleRequest.endDate}
                AND (:#{#teachingScheduleRequest.idSubject} IS NULL OR sb.id = :#{#teachingScheduleRequest.idSubject})
                AND (:#{#teachingScheduleRequest.idFactory} IS NULL OR ft.id = :#{#teachingScheduleRequest.idFactory})
                AND (:#{#teachingScheduleRequest.idProject} IS NULL OR p.id = :#{#teachingScheduleRequest.idProject})
                AND (:#{#teachingScheduleRequest.shift} IS NULL OR pd.shift = :#{#teachingScheduleRequest.shift})
                AND (:#{#teachingScheduleRequest.shiftType} IS NULL OR pd.type = :#{#teachingScheduleRequest.shiftType})
            """, nativeQuery = true)
    Page<TCTeachingScheduleResponse> getAllTeachingScheduleByStaff(
            String userId, Pageable pageable, TCTeachingScheduleRequest teachingScheduleRequest);

    @Query(value = """
                        SELECT
                                    pd.id AS idPlanDate,
                                    pd.start_date AS startTeaching,
                                    pd.end_date AS endTeaching,
                                    pd.shift AS shift,
                                    sb.code AS subjectCode,
                                    ft.name AS factoryName,
                                    ft.id AS factoryId,
                                    us.id AS userId,
                                    p.name AS projectName,
                                    pd.type as type,
                                    pd.link as link,
                                    pd.late_arrival AS lateArrival,
                                    pd.description AS description,
                                    pd.room as room
                                FROM
                                    plan_date pd
                                    LEFT JOIN plan_factory pf ON pf.id = pd.id_plan_factory
                                    LEFT JOIN factory ft ON ft.id = pf.id_factory
                                    LEFT JOIN user_staff us ON us.id = ft.id_user_staff
                                    LEFT JOIN project p ON p.id = ft.id_project
                                    LEFT JOIN subject_facility sf ON sf.id = p.id_subject_facility
                                    LEFT JOIN subject sb ON sb.id = sf.id_subject
                                    LEFT JOIN facility f ON f.id = sf.id_facility
                                WHERE
                                    us.id = :userId
                                    AND us.status = 1
                                    AND pd.status = 1
                                    AND ft.status = 1
                                    AND p.status = 1
                                    AND sf.status = 1
                                    AND sb.status = 1
                                    AND pf.status = 1
                                    AND DATE(FROM_UNIXTIME(pd.start_date / 1000)) = CURDATE()
                                ORDER BY pd.start_date ASC

            """, countQuery = """
            SELECT COUNT(*)
            FROM
                plan_date pd
                LEFT JOIN plan_factory pf ON pf.id = pd.id_plan_factory
                LEFT JOIN factory ft ON ft.id = pf.id_factory
                LEFT JOIN user_staff us ON us.id = ft.id_user_staff
                LEFT JOIN project p ON p.id = ft.id_project
                LEFT JOIN subject_facility sf ON sf.id = p.id_subject_facility
                LEFT JOIN subject sb ON sb.id = sf.id_subject
            WHERE
                us.id = :userId
                AND us.status = 1
                AND pd.status = 1
                AND ft.status = 1
                AND p.status = 1
                AND sf.status = 1
                AND sb.status = 1
                AND pf.status = 1
                AND DATE(FROM_UNIXTIME(pd.start_date / 1000)) = CURDATE()
            """, nativeQuery = true)
    Page<TCTeachingScheduleResponse> getAllTeachingSchedulePresent(String userId, Pageable pageable,
                                                                   TCTeachingScheduleRequest teachingScheduleRequest);

    @Query(value = """
            SELECT
            pd
            FROM PlanDate pd
                  """)
    List<PlanDate> getAllType();

    @Query(value = """
                SELECT
                pd.late_arrival as lateArrival,
                pd.description as description,
                pd.link as link,
                pd.type as type,
                pd.room as room,
                pd.id as planDateId
                FROM plan_date pd
                WHERE pd.id = :planDateId
            """, nativeQuery = true)
    Optional<TCTSDetailPlanDateResponse> getPlanDateById(String planDateId);

    @Query(value = """
            SELECT
            ROW_NUMBER() OVER (ORDER BY pd.start_date ASC) AS indexs,
            pd.id AS idPlanDate,
            pd.start_date AS startTeaching,
            pd.end_date AS endTeaching,
            pd.shift AS shift,
            sb.code AS subjectCode,
            ft.name AS factoryName,
            ft.id AS factoryId,
            us.id AS userId,
            p.name AS projectName,
            pd.type as type,
            pd.link as link,
            pd.late_arrival AS lateArrival,
            pd.description AS description
            FROM
                plan_date pd
                LEFT JOIN plan_factory pf ON pf.id = pd.id_plan_factory
                LEFT JOIN factory ft ON ft.id = pf.id_factory
                LEFT JOIN user_staff us ON us.id = ft.id_user_staff
                LEFT JOIN project p ON p.id = ft.id_project
                LEFT JOIN subject_facility sf ON sf.id = p.id_subject_facility
                LEFT JOIN subject sb ON sb.id = sf.id_subject
            WHERE
                us.id = :userId
                AND us.status = 1
                AND pd.status = 1
                AND ft.status = 1
                AND p.status = 1
                AND sf.status = 1
                AND sb.status = 1
                AND pf.status = 1
                AND pd.start_date BETWEEN :#{#teachingScheduleRequest.startDate} AND :#{#teachingScheduleRequest.endDate}
            ORDER BY pd.start_date ASC

            """, nativeQuery = true)
    List<TCTeachingScheduleResponse> exportExcelTeachingSchedule(
            String userId, TCTeachingScheduleRequest teachingScheduleRequest);


    @Query(
            value = """
                      SELECT CASE
                               WHEN pd.start_date + pd.late_arrival * 60 < UNIX_TIMESTAMP(NOW()) * 1000 THEN 'TRUE'
                               ELSE 'FALSE'
                             END
                      FROM plan_date pd
                      WHERE pd.id = :planDateId
                    """,
            nativeQuery = true
    )
    boolean isOutOfTime(String planDateId);


}