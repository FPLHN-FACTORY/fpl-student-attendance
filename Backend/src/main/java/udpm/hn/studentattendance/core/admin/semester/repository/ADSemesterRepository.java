package udpm.hn.studentattendance.core.admin.semester.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.semester.model.request.ADSemesterRequest;
import udpm.hn.studentattendance.core.admin.semester.model.response.ADSemesterResponse;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.SemesterRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ADSemesterRepository extends SemesterRepository {
        @Query(value = """
                        SELECT
                            DISTINCT
                            ROW_NUMBER() OVER (ORDER BY s.createdAt desc ) AS semesterIndex,
                            s.id AS id,
                            s.code AS semesterCode,
                            s.semesterName AS semesterName,
                            s.status AS semesterStatus,
                            s.fromDate AS startDate,
                            s.toDate AS endDate
                        FROM Semester s
                        WHERE (:#{#request.semesterCode} IS NULL OR s.code LIKE CONCAT('%', TRIM(:#{#request.semesterCode}), '%'))
                          AND (:#{#request.status} IS NULL OR s.status = :#{#request.status})
                          AND ((:#{#request.fromDateSemester} IS NULL OR :#{#request.toDateSemester} IS NULL)
                               OR (s.fromDate >= :#{#request.fromDateSemester} AND s.toDate <= :#{#request.toDateSemester})
                               OR (s.toDate >= :#{#request.fromDateSemester} AND s.fromDate <= :#{#request.toDateSemester}))
                        ORDER BY s.status desc
                        """, countQuery = """
                        SELECT COUNT(s.id)
                        FROM Semester s
                        WHERE (:#{#request.semesterCode} IS NULL OR s.code LIKE CONCAT('%', TRIM(:#{#request.semesterCode}), '%'))
                          AND (:#{#request.status} IS NULL OR s.status = :#{#request.status})
                          AND ((:#{#request.fromDateSemester} IS NULL OR :#{#request.toDateSemester} IS NULL)
                               OR (s.fromDate >= :#{#request.fromDateSemester} AND s.toDate <= :#{#request.toDateSemester})
                               OR (s.toDate >= :#{#request.fromDateSemester} AND s.fromDate <= :#{#request.toDateSemester}))
                        """)
        Page<ADSemesterResponse> getAllSemester(Pageable pageable, ADSemesterRequest request);

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
                            SELECT 
                            s
                            FROM Semester s
                            WHERE TRIM(s.semesterName) = TRIM(:semesterName)
                            AND s.year = :semesterYear
                            AND s.status = :status 
                            AND (:semesterId IS NULL OR s.id != :semesterId)
                        """)
        Optional<Semester> checkSemesterExistNameAndYear(String semesterName, Integer semesterYear, EntityStatus status,
                        String semesterId);

        @Query(value = """
                        SELECT
                        s.id as id,
                        s.semesterName as semesterName,
                        s.fromDate as startTime,
                        s.toDate as endTime
                        FROM Semester s
                        WHERE TRIM(s.id) = TRIM(:semesterId)
                        """)
        Optional<ADSemesterResponse> getDetailSemesterById(String semesterId);

}
