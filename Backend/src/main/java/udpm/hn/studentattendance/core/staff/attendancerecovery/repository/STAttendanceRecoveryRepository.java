package udpm.hn.studentattendance.core.staff.attendancerecovery.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.request.STAttendanceRecoveryRequest;
import udpm.hn.studentattendance.core.staff.attendancerecovery.model.response.STAttendanceRecoveryResponse;
import udpm.hn.studentattendance.repositories.AttendanceRecoveryRepository;

@Repository
public interface STAttendanceRecoveryRepository extends AttendanceRecoveryRepository {

    @Query(value = """
            SELECT
                ar.id          AS id,
                ar.name        AS name,
                ar.description AS description,
                ar.day         AS dayHappen,
                ar.total_student AS totalStudent
            FROM attendance_recovery ar
            WHERE
                ar.id_facility = :facilityId
                AND ar.status = 1
                AND (
                    :#{#req.searchQuery} IS NULL
                    OR TRIM(:#{#req.searchQuery}) = ''
                    OR (
                        TRIM(ar.name)        LIKE CONCAT('%', TRIM(:#{#req.searchQuery}), '%')
                        OR TRIM(ar.description) LIKE CONCAT('%', TRIM(:#{#req.searchQuery}), '%')
                    )
                )
                AND (
                    :#{#req.fromDate} IS NULL
                    OR ar.day >= :#{#req.fromDate}
                )
                AND (
                    :#{#req.toDate} IS NULL
                    OR ar.day <= :#{#req.toDate}
                )
            """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM attendance_recovery ar
                    WHERE
                        ar.id_facility = :facilityId
                        AND ar.status = 1
                        AND (
                            :#{#req.searchQuery} IS NULL
                            OR TRIM(:#{#req.searchQuery}) = ''
                            OR (
                                TRIM(ar.name)        LIKE CONCAT('%', TRIM(:#{#req.searchQuery}), '%')
                                OR TRIM(ar.description) LIKE CONCAT('%', TRIM(:#{#req.searchQuery}), '%')
                            )
                        )
                        AND (
                            :#{#req.fromDate} IS NULL
                            OR ar.day >= :#{#req.fromDate}
                        )
                        AND (
                            :#{#req.toDate} IS NULL
                            OR ar.day <= :#{#req.toDate}
                        )
                    """,
            nativeQuery = true
    )
    Page<STAttendanceRecoveryResponse> getListAttendanceRecovery(
            STAttendanceRecoveryRequest req,
            String facilityId,
            Pageable pageable
    );
}
