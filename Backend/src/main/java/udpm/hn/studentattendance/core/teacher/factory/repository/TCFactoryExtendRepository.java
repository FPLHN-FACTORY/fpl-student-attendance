package udpm.hn.studentattendance.core.teacher.factory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCFactoryRequest;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCFactoryResponse;
import udpm.hn.studentattendance.repositories.FactoryRepository;

@Repository
public interface TCFactoryExtendRepository extends FactoryRepository {

    @Query(value = """
            SELECT
                ROW_NUMBER() OVER (ORDER BY LEAST(ft.status, p.status, sf.status, sj.status, lp.status, pf.status, pl.status, f.status) DESC, ft.created_at DESC) AS rowNumber,
                ft.id AS factoryId,
                us.id AS userStaffId,
                ft.name AS factoryName,
                p.id AS projectId,
                p.name AS projectName,
                ft.description AS factoryDescription,
                (SELECT COUNT(DISTINCT usf.id)
                    FROM user_student_factory usf
                    JOIN user_student us ON usf.id_user_student = us.id
                    WHERE
                        usf.status = 1 AND
                    usf.id_factory = ft.id
                ) AS totalStudent,
                (SELECT COUNT(DISTINCT pd.id)
                    FROM plan_date pd
                    JOIN plan_factory pf ON pd.id_plan_factory = pf.id
                    WHERE
                        pd.status = 1 AND
                        pd.id_plan_factory = pf.id AND
                        pf.id_factory = ft.id
                ) AS totalShift,
                LEAST(ft.status, p.status, sf.status, sj.status, lp.status, pf.status, pl.status, f.status) as factoryStatus
            FROM factory ft
            JOIN user_staff us ON us.id = ft.id_user_staff
            JOIN project p ON p.id = ft.id_project
            JOIN subject_facility sf ON sf.id = p.id_subject_facility
            JOIN subject sj ON sf.id_subject = sj.id
            JOIN level_project lp ON p.id_level_project = lp.id
            JOIN plan_factory pf ON ft.id = pf.id_factory
            JOIN plan pl ON p.id = pl.id_project
            JOIN facility f ON f.id = sf.id_facility
            JOIN semester s ON p.id_semester = s.id
            WHERE
                us.code = :userStaffCode
                AND f.id = :facilityId
                AND s.status = 1
                AND (:#{#teacherStudentRequest.semesterId} IS NULL OR s.id = :#{#teacherStudentRequest.semesterId})
                AND (:#{#teacherStudentRequest.factoryName} IS NULL OR ft.name LIKE CONCAT('%', TRIM(:#{#teacherStudentRequest.factoryName}), '%'))
                AND (:#{#teacherStudentRequest.projectId} IS NULL OR p.id LIKE CONCAT('%', TRIM(:#{#teacherStudentRequest.projectId}), '%'))
                AND (:#{#teacherStudentRequest.factoryStatus} IS NULL OR LEAST(ft.status, p.status, sf.status, sj.status, lp.status, pf.status, pl.status, f.status) = :#{#teacherStudentRequest.factoryStatus})

            ORDER BY LEAST(ft.status, p.status, sf.status, sj.status, lp.status, pf.status, pl.status, f.status) DESC, ft.created_at DESC
            """, countQuery = """
            SELECT COUNT(*)
            FROM factory ft
            JOIN user_staff us ON us.id = ft.id_user_staff
            JOIN project p ON p.id = ft.id_project
            JOIN subject_facility sf ON sf.id = p.id_subject_facility
            JOIN subject sj ON sf.id_subject = sj.id
            JOIN level_project lp ON p.id_level_project = lp.id
            JOIN plan_factory pf ON ft.id = pf.id_factory
            JOIN plan pl ON p.id = pl.id_project
            JOIN facility f ON f.id = sf.id_facility
            JOIN semester s ON p.id_semester = s.id
            WHERE
                us.code = :userStaffCode
                AND f.id = :facilityId
                AND s.status = 1
                AND (:#{#teacherStudentRequest.factoryName} IS NULL OR ft.name LIKE CONCAT('%', TRIM(:#{#teacherStudentRequest.factoryName}), '%'))
                AND (:#{#teacherStudentRequest.projectId} IS NULL OR p.id LIKE CONCAT('%', TRIM(:#{#teacherStudentRequest.projectId}), '%'))
                AND (:#{#teacherStudentRequest.factoryStatus} IS NULL OR LEAST(ft.status, p.status, sf.status, sj.status, lp.status, pf.status, pl.status, f.status) = :#{#teacherStudentRequest.factoryStatus})
                AND (:#{#teacherStudentRequest.semesterId} IS NULL OR s.id = :#{#teacherStudentRequest.semesterId})

            """, nativeQuery = true)
    Page<TCFactoryResponse> getAllFactoryByTeacher(Pageable pageable, String facilityId, String userStaffCode,
            TCFactoryRequest teacherStudentRequest);

}
