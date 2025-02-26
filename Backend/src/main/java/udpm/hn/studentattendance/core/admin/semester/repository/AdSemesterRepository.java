package udpm.hn.studentattendance.core.admin.semester.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.semester.model.request.AdSemesterRequest;
import udpm.hn.studentattendance.core.admin.semester.model.respone.AdSemesterRespone;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.repositories.SemesterRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdSemesterRepository extends SemesterRepository {
    @Query(
            value = """
                    SELECT s.id AS semesterId,
                           s.code AS semesterCode,
                           s.name AS semesterName,
                           s.fromDate AS startDate,
                           s.toDate AS endDate
                    FROM Semester s
                    WHERE (:#{#request.semesterName} IS NULL OR CONCAT(s.name, ' - ', s.year) LIKE CONCAT('%', TRIM(:#{#request.semesterName}), '%'))
                      AND ((:#{#request.fromDateSemester} IS NULL OR :#{#request.toDateSemester} IS NULL)
                           OR (s.fromDate >= :#{#request.fromDateSemester} AND s.toDate <= :#{#request.toDateSemester})
                           OR (s.toDate >= :#{#request.fromDateSemester} AND s.fromDate <= :#{#request.toDateSemester}))
                    """,
            countQuery = """
                    SELECT COUNT(s.id)
                    FROM Semester s
                    WHERE (:#{#request.semesterName} IS NULL OR CONCAT(s.name, ' - ', s.year) LIKE CONCAT('%', TRIM(:#{#request.semesterName}), '%'))
                      AND ((:#{#request.fromDateSemester} IS NULL OR :#{#request.toDateSemester} IS NULL)
                           OR (s.fromDate >= :#{#request.fromDateSemester} AND s.toDate <= :#{#request.toDateSemester})
                           OR (s.toDate >= :#{#request.fromDateSemester} AND s.fromDate <= :#{#request.toDateSemester}))
                    """
    )
    Page<AdSemesterRespone> getAllSemester(Pageable pageable, AdSemesterRequest request);

    @Query(value = """
             SELECT s
             FROM Semester s
             WHERE (s.fromDate >= :startTime AND s.fromDate <= :endTime)
             OR (s.toDate >= :startTime AND s.toDate <= :endTime)
             OR (:startTime >= s.fromDate AND :startTime <= s.toDate)
             OR (:endTime >= s.fromDate AND :endTime <= s.toDate)
            """)
    List<Semester> checkConflictTime(Long startTime, Long endTime);

    @Query(value = """
                FROM Semester s
                WHERE TRIM(s.name) = TRIM(:semesterName)
                AND s.year = :semesterYear
                AND s.status = 'ACTIVE'
            """)
    Optional<Semester> checkSemesterExistNameAndYear(String semesterName, Integer semesterYear);

    @Query(value = """
            SELECT 
            s.id as semesterId,
            s.name as semesterName,
            s.fromDate as startTime,
            s.toDate as endTime
            FROM Semester s
            WHERE TRIM(s.id) = TRIM(:semesterId)
            """)
    Optional<Semester> getDetailSemesterById(String semesterId);
}
