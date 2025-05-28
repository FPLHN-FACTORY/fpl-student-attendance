package udpm.hn.studentattendance.infrastructure.excel.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.infrastructure.excel.model.response.ExImportLogDetailResponse;
import udpm.hn.studentattendance.repositories.ImportLogDetailRepository;

import java.util.List;

@Repository
public interface EXImportLogDetailRepository extends ImportLogDetailRepository {

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
