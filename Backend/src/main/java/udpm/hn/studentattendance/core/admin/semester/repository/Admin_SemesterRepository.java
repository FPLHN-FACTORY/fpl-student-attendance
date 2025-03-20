package udpm.hn.studentattendance.core.admin.semester.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.semester.model.request.Admin_SemesterRequest;
import udpm.hn.studentattendance.core.admin.semester.model.response.Admin_SemesterResponse;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.repositories.SemesterRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Admin_SemesterRepository extends SemesterRepository {
    @Query(
            value = """
        SELECT  
            DISTINCT 
            ROW_NUMBER() OVER (ORDER BY s.toDate desc ) AS semesterIndex, 
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
        """,
            countQuery = """
        SELECT COUNT(s.id)
        FROM Semester s
        WHERE (:#{#request.semesterCode} IS NULL OR s.code LIKE CONCAT('%', TRIM(:#{#request.semesterCode}), '%'))
          AND (:#{#request.status} IS NULL OR s.status = :#{#request.status})
          AND ((:#{#request.fromDateSemester} IS NULL OR :#{#request.toDateSemester} IS NULL)
               OR (s.fromDate >= :#{#request.fromDateSemester} AND s.toDate <= :#{#request.toDateSemester})
               OR (s.toDate >= :#{#request.fromDateSemester} AND s.fromDate <= :#{#request.toDateSemester}))
        """
    )
    Page<Admin_SemesterResponse> getAllSemester(Pageable pageable, Admin_SemesterRequest request);




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
                WHERE TRIM(s.semesterName) = TRIM(:semesterName)
                AND s.year = :semesterYear
                AND s.status = :status
            """)
    Optional<Semester> checkSemesterExistNameAndYear(String semesterName, Integer semesterYear, EntityStatus status);

    @Query(value = """
            SELECT 
            s.id as id,
            s.semesterName as semesterName,
            s.fromDate as startTime,
            s.toDate as endTime
            FROM Semester s
            WHERE TRIM(s.id) = TRIM(:semesterId)
            """)
    Optional<Admin_SemesterResponse> getDetailSemesterById(String semesterId);
}
