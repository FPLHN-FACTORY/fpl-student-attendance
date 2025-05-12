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
                ROW_NUMBER() OVER (ORDER BY ft.created_at DESC) AS rowNumber,
                ft.id AS factoryId,
                us.id AS userStaffId,
                ft.name AS factoryName,
                p.id AS projectId,
                p.name AS projectName,
                ft.description AS factoryDescription,
                ft.status as factoryStatus
            FROM factory ft
            LEFT JOIN user_staff us ON us.id = ft.id_user_staff
            LEFT JOIN project p ON p.id = ft.id_project
            LEFT JOIN subject_facility sf ON sf.id = p.id_subject_facility
            LEFT JOIN facility f ON f.id = sf.id_facility
            LEFT JOIN semester s ON p.id_semester = s.id
            WHERE
                us.code = :userStaffCode
                AND f.id = :facilityId
                AND f.status = 1
                AND p.status = 1
                AND sf.status = 1
                AND ft.status = 1
                AND (:#{#teacherStudentRequest.semesterId} IS NULL OR s.id = :#{#teacherStudentRequest.semesterId})
                AND (:#{#teacherStudentRequest.factoryName} IS NULL OR ft.name LIKE CONCAT('%', :#{#teacherStudentRequest.factoryName}, '%'))
                AND (:#{#teacherStudentRequest.projectId} IS NULL OR p.id LIKE CONCAT('%', :#{#teacherStudentRequest.projectId}, '%'))
                AND (:#{#teacherStudentRequest.factoryStatus} IS NULL OR ft.status = :#{#teacherStudentRequest.factoryStatus})

            ORDER BY ft.created_at DESC
            """, countQuery = """
            SELECT COUNT(*)
            FROM factory ft
            LEFT JOIN user_staff us ON us.id = ft.id_user_staff
            LEFT JOIN project p ON p.id = ft.id_project
            LEFT JOIN subject_facility sf ON sf.id = p.id_subject_facility
            LEFT JOIN facility f ON f.id = sf.id_facility
            LEFT JOIN semester s ON p.id_semester = s.id
            WHERE
                us.code = :userStaffCode
                AND f.id = :facilityId
                AND f.status = 1
                AND p.status = 1
                AND sf.status = 1
                AND ft.status = 1
                AND (:#{#teacherStudentRequest.factoryName} IS NULL OR ft.name LIKE CONCAT('%', :#{#teacherStudentRequest.factoryName}, '%'))
                AND (:#{#teacherStudentRequest.projectId} IS NULL OR p.id LIKE CONCAT('%', :#{#teacherStudentRequest.projectId}, '%'))
                AND (:#{#teacherStudentRequest.factoryStatus} IS NULL OR ft.status = :#{#teacherStudentRequest.factoryStatus})
                AND (:#{#teacherStudentRequest.semesterId} IS NULL OR s.id = :#{#teacherStudentRequest.semesterId})

            """, nativeQuery = true)
    Page<TCFactoryResponse> getAllFactoryByTeacher(Pageable pageable, String facilityId, String userStaffCode,
                                                   TCFactoryRequest teacherStudentRequest);

}
