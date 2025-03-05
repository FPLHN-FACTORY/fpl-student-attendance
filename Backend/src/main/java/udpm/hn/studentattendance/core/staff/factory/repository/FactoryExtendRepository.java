package udpm.hn.studentattendance.core.staff.factory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.factory.model.request.FactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.model.response.FactoryResponse;
import udpm.hn.studentattendance.repositories.FactoryRepository;

@Repository
public interface FactoryExtendRepository extends FactoryRepository {
    @Query(value = """
    SELECT 
        ft.id AS factoryId,
        ft.name AS factoryName,
        ft.status AS factoryStatus,
        p.name AS projectName,
        sub.code AS subjectCode,
        f.name AS facilityName,
        us.name AS staffName,
        pd.start_date AS planStartDate,
        pd.shift AS planShift
    FROM factory ft
     LEFT JOIN project p ON p.id = ft.id_project
     LEFT JOIN subject_facility sf ON p.id_subject_facility = sf.id
     LEFT JOIN subject sub ON sub.id = sf.id_subject
     LEFT JOIN facility f ON f.id = sf.id_facility
     LEFT JOIN user_staff us ON us.id = ft.id_user_staff
     LEFT JOIN plan_date pd ON pd.id_factory = ft.id
    WHERE 
        (:#{#factoryRequest.idProject} IS NULL OR p.id = :#{#factoryRequest.idProject})
        AND (:#{#factoryRequest.idStaff} IS NULL OR us.id = :#{#factoryRequest.idStaff})
        AND (:#{#factoryRequest.searchPlanStartDate} IS NULL OR pd.start_date = :#{#factoryRequest.searchPlanStartDate})
        AND (:#{#factoryRequest.searchPlanShift} IS NULL OR pd.shift = :#{#factoryRequest.searchPlanShift})
        AND (:#{#factoryRequest.searchQuery} IS NULL OR (
                ft.name LIKE CONCAT('%', :#{#factoryRequest.searchQuery}, '%')
                OR p.name LIKE CONCAT('%', :#{#factoryRequest.searchQuery}, '%')
                OR sub.code LIKE CONCAT('%', :#{#factoryRequest.searchQuery}, '%')
                OR f.name LIKE CONCAT('%', :#{#factoryRequest.searchQuery}, '%')
                OR us.name LIKE CONCAT('%', :#{#factoryRequest.searchQuery}, '%')
        ))
        AND (:#{#factoryRequest.status} IS NULL OR ft.status = :#{#factoryRequest.status})
""", countQuery = """
    SELECT COUNT(*)
    FROM factory ft
    JOIN project p ON p.id = ft.id_project
    JOIN subject_facility sf ON p.id_subject_facility = sf.id
    JOIN subject sub ON sub.id = sf.id_subject
    JOIN facility f ON f.id = sf.id_facility
    JOIN user_staff us ON us.id = ft.id_user_staff
    LEFT JOIN plan_date pd ON pd.id_factory = ft.id
    WHERE 
        (:#{#factoryRequest.idProject} IS NULL OR p.id = :#{#factoryRequest.idProject})
        AND (:#{#factoryRequest.idStaff} IS NULL OR us.id = :#{#factoryRequest.idStaff})
        AND (:#{#factoryRequest.searchPlanStartDate} IS NULL OR pd.start_date = :#{#factoryRequest.searchPlanStartDate})
        AND (:#{#factoryRequest.searchPlanShift} IS NULL OR pd.shift = :#{#factoryRequest.searchPlanShift})
        AND (:#{#factoryRequest.searchQuery} IS NULL OR (
                LOWER(ft.name) LIKE LOWER(CONCAT('%', :#{#factoryRequest.searchQuery}, '%'))
                OR LOWER(p.name) LIKE LOWER(CONCAT('%', :#{#factoryRequest.searchQuery}, '%'))
                OR LOWER(sub.code) LIKE LOWER(CONCAT('%', :#{#factoryRequest.searchQuery}, '%'))
                OR LOWER(f.name) LIKE LOWER(CONCAT('%', :#{#factoryRequest.searchQuery}, '%'))
                OR LOWER(us.name) LIKE LOWER(CONCAT('%', :#{#factoryRequest.searchQuery}, '%'))
        ))
        AND (:#{#factoryRequest.status} IS NULL OR ft.status = :#{#factoryRequest.status})
""", nativeQuery = true)
    Page<FactoryResponse> getAllFactory(Pageable pageable, FactoryRequest factoryRequest);



}
