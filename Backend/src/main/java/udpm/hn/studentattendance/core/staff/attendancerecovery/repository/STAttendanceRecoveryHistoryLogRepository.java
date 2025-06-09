package udpm.hn.studentattendance.core.staff.attendancerecovery.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogDetailResponse;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogResponse;
import udpm.hn.studentattendance.repositories.ImportLogRepository;

import java.util.List;

@Repository
public interface STAttendanceRecoveryHistoryLogRepository extends ImportLogRepository {

    @Query(value = """
        SELECT 
            il.id,
            il.created_at,
            il.file_name,
            (SELECT COUNT(*) FROM import_log_detail WHERE id_import_log = il.id AND status = 1) AS totalSuccess,
            (SELECT COUNT(*) FROM import_log_detail WHERE id_import_log = il.id AND status = 0) AS totalError
        FROM import_log il
        JOIN attendance_recovery ar ON il.id = ar.id_import_log
        WHERE
            il.type = :type AND
            il.id_user = :userId AND
            (:facilityId IS NULL OR il.id_facility = :facilityId) AND 
            ar.id_import_log = :idImportLog
        ORDER BY il.created_at DESC 
    """, countQuery = """
        SELECT 
            COUNT(*)
        FROM import_log il
        JOIN attendance_recovery ar ON il.id = ar.id_import_log
        WHERE
            il.type = :type AND
            il.id_user = :userId AND
            (:facilityId IS NULL OR il.id_facility = :facilityId)
            ar.id_import_log = :idImportLog
    """, nativeQuery = true)
    Page<ExImportLogResponse> getListHistory(Pageable pageable, int type, String userId, String facilityId, String idImportLog);

    @Query(value = """
        SELECT 
            ild.line,
            ild.message,
            ild.status
        FROM import_log_detail ild
        JOIN import_log il ON ild.id_import_log = il.id
        WHERE
            il.id = :idImportLog AND
            il.id_user = :userId AND
            (:facilityId IS NULL OR il.id_facility = :facilityId)
        ORDER BY ild.line ASC
    """, nativeQuery = true)
    List<ExImportLogDetailResponse> getAllList(String idImportLog, String userId, String facilityId);
}
