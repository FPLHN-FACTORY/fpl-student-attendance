package udpm.hn.studentattendance.core.staff.factory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.factory.model.request.StaffFactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.model.response.StaffFactoryResponse;
import udpm.hn.studentattendance.repositories.FactoryRepository;

@Repository
public interface StaffFactoryExtendRepository extends FactoryRepository {
    @Query(value = """
    SELECT 
        ft.id AS factoryId,
        ft.name AS factoryName,
        ft.status AS factoryStatus,
        p.name AS projectName,
        sub.code AS subjectCode,
        us.name AS staffName,
        pd.start_date AS planStartDate,
        pd.shift AS planShift
    FROM factory ft
     LEFT JOIN project p ON p.id = ft.id_project
     LEFT JOIN subject_facility sf ON p.id_subject_facility = sf.id
     LEFT JOIN subject sub ON sub.id = sf.id_subject
     LEFT JOIN user_staff us ON us.id = ft.id_user_staff
     LEFT JOIN plan_date pd ON pd.id_factory = ft.id
    WHERE 
        (:#{#staffFactoryRequest.idProject} IS NULL OR p.id = :#{#staffFactoryRequest.idProject})
        AND (:#{#staffFactoryRequest.idStaff} IS NULL OR us.id = :#{#staffFactoryRequest.idStaff})
        AND (:#{#staffFactoryRequest.searchPlanStartDate} IS NULL OR pd.start_date = :#{#staffFactoryRequest.searchPlanStartDate})
        AND (:#{#staffFactoryRequest.searchPlanShift} IS NULL OR pd.shift = :#{#staffFactoryRequest.searchPlanShift})
        AND (:#{#staffFactoryRequest.searchQuery} IS NULL OR (
                ft.name LIKE CONCAT('%', :#{#staffFactoryRequest.searchQuery}, '%')
                OR p.name LIKE CONCAT('%', :#{#staffFactoryRequest.searchQuery}, '%')
                OR sub.code LIKE CONCAT('%', :#{#staffFactoryRequest.searchQuery}, '%')
                OR us.name LIKE CONCAT('%', :#{#staffFactoryRequest.searchQuery}, '%')
        ))
        AND (:#{#staffFactoryRequest.status} IS NULL OR ft.status = :#{#staffFactoryRequest.status})
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
        (:#{#staffFactoryRequest.idProject} IS NULL OR p.id = :#{#staffFactoryRequest.idProject})
        AND (:#{#staffFactoryRequest.idStaff} IS NULL OR us.id = :#{#staffFactoryRequest.idStaff})
        AND (:#{#staffFactoryRequest.searchPlanStartDate} IS NULL OR pd.start_date = :#{#staffFactoryRequest.searchPlanStartDate})
        AND (:#{#staffFactoryRequest.searchPlanShift} IS NULL OR pd.shift = :#{#staffFactoryRequest.searchPlanShift})
        AND (:#{#staffFactoryRequest.searchQuery} IS NULL OR (
                LOWER(ft.name) LIKE LOWER(CONCAT('%', :#{#staffFactoryRequest.searchQuery}, '%'))
                OR LOWER(p.name) LIKE LOWER(CONCAT('%', :#{#staffFactoryRequest.searchQuery}, '%'))
                OR LOWER(sub.code) LIKE LOWER(CONCAT('%', :#{#staffFactoryRequest.searchQuery}, '%'))
                OR LOWER(f.name) LIKE LOWER(CONCAT('%', :#{#staffFactoryRequest.searchQuery}, '%'))
                OR LOWER(us.name) LIKE LOWER(CONCAT('%', :#{#staffFactoryRequest.searchQuery}, '%'))
        ))
        AND (:#{#staffFactoryRequest.status} IS NULL OR ft.status = :#{#staffFactoryRequest.status})
""", nativeQuery = true)
    Page<StaffFactoryResponse> getAllFactory(Pageable pageable, StaffFactoryRequest staffFactoryRequest);



}
