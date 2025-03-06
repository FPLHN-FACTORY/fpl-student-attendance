package udpm.hn.studentattendance.core.admin.subject.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.subject.model.request.AdminSubjectSearchRequest;
import udpm.hn.studentattendance.core.admin.subject.model.response.AdminSubjectResponse;
import udpm.hn.studentattendance.repositories.SubjectRepository;

@Repository
public interface AdminSubjectRepository extends SubjectRepository {

    @Query(
            value = """
            SELECT
                ROW_NUMBER() OVER (ORDER BY s.created_at DESC) AS indexs,
                s.id AS id,
                s.name AS name,
                s.status AS status,
                s.code AS code,
                COUNT(sf.id_subject) AS sizeSubjectSemester
            FROM
                subject s
            LEFT JOIN
                subject_facility sf ON s.id = sf.id_subject
            WHERE
                (:#{#request.name} IS NULL OR s.name LIKE CONCAT('%', :#{#request.name}, '%'))
                AND (:#{#request.status} IS NULL OR s.status = :#{#request.status})
            GROUP BY
                s.id, s.name, s.status, s.code, s.created_at
            ORDER BY
                s.created_at DESC
            """,
            countQuery = """
            SELECT
                COUNT(*)
            FROM
                subject s
            LEFT JOIN
                subject_facility sf ON s.id = sf.id_subject
            WHERE
                (:#{#request.name} IS NULL OR s.name LIKE CONCAT('%', :#{#request.name}, '%'))
                AND (:#{#request.status} IS NULL OR s.status = :#{#request.status})
            """,
            nativeQuery = true
    )
    Page<AdminSubjectResponse> getAll(Pageable pageable, AdminSubjectSearchRequest request);
}