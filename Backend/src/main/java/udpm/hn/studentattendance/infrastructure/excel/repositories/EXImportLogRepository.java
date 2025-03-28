package udpm.hn.studentattendance.infrastructure.excel.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.ImportLog;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogResponse;
import udpm.hn.studentattendance.repositories.ImportLogRepository;

import java.util.Optional;

@Repository
public interface EXImportLogRepository extends ImportLogRepository {

    Optional<ImportLog> findByIdUserAndCodeAndFileNameAndFacility_Id(String idUser, String code, String fileName, String idFacility);

    @Query(value = """
        SELECT 
            il.id,
            il.created_at,
            il.file_name,
            (SELECT COUNT(*) FROM import_log_detail WHERE id_import_log = il.id AND status = 1) AS totalSuccess,
            (SELECT COUNT(*) FROM import_log_detail WHERE id_import_log = il.id AND status = 0) AS totalError
        FROM import_log il
        WHERE
            il.type = :type AND
            il.id_user = :userId AND
            (:facilityId IS NULL OR il.id_facility = :facilityId)
        ORDER BY il.created_at DESC 
    """, countQuery = """
        SELECT 
            COUNT(*)
        FROM import_log il
        WHERE
            il.type = :type AND
            il.id_user = :userId AND
            (:facilityId IS NULL OR il.id_facility = :facilityId)
    """, nativeQuery = true)
    Page<ExImportLogResponse> getListHistory(Pageable pageable, int type, String userId, String facilityId);

}
