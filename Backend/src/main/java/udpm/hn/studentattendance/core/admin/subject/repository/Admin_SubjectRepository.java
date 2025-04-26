package udpm.hn.studentattendance.core.admin.subject.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.subject.model.request.Admin_SubjectSearchRequest;
import udpm.hn.studentattendance.core.admin.subject.model.response.Admin_SubjectResponse;
import udpm.hn.studentattendance.repositories.SubjectRepository;

@Repository
public interface Admin_SubjectRepository extends SubjectRepository {

    @Query(value = """
             SELECT
                 ROW_NUMBER() OVER (ORDER BY s.created_at DESC) AS indexs,
                 s.id AS id,
                 s.name AS name,
                 s.status AS status,
                 s.code AS code,
                 IFNULL(GROUP_CONCAT(f.name SEPARATOR ', '), 'Chưa có bộ môn cơ sở') AS sizeSubjectSemester
             FROM
                 subject s
             LEFT JOIN
                subject_facility sf ON s.id = sf.id_subject AND sf.status = 1
            LEFT JOIN
                facility f ON sf.id_facility = f.id
             WHERE
                 (:#{#request.name} IS NULL OR s.name LIKE CONCAT('%', :#{#request.name}, '%'))
                 AND (:#{#request.status} IS NULL OR s.status = :#{#request.status})
             GROUP BY
                 s.id, s.name, s.status, s.code, s.created_at
             ORDER BY
                 s.created_at DESC
             """, countQuery = """
            SELECT
                COUNT(*)
            FROM
                subject s
            LEFT JOIN
                subject_facility sf ON s.id = sf.id_subject AND sf.status = 1
            LEFT JOIN
                facility f ON sf.id_facility = f.id
            WHERE
                (:#{#request.name} IS NULL OR s.name LIKE CONCAT('%', :#{#request.name}, '%'))
                AND (:#{#request.status} IS NULL OR s.status = :#{#request.status})
                AND sf.status = 1
            """, nativeQuery = true)
    Page<Admin_SubjectResponse> getAll(Pageable pageable, Admin_SubjectSearchRequest request);
}