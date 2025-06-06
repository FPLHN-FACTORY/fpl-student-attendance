package udpm.hn.studentattendance.core.admin.subject.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.subject.model.request.ADSubjectSearchRequest;
import udpm.hn.studentattendance.core.admin.subject.model.response.ADSubjectResponse;
import udpm.hn.studentattendance.repositories.SubjectRepository;

@Repository
public interface ADSubjectExtendRepository extends SubjectRepository {

    @Query(value = """
             SELECT
                 ROW_NUMBER() OVER (ORDER BY s.status DESC, s.created_at DESC) AS orderNumber,
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
                facility f ON sf.id_facility = f.id AND f.status = 1
             WHERE
                 (:#{#request.name} IS NULL OR s.name LIKE CONCAT('%', TRIM(:#{#request.name}), '%'))
                 AND (:#{#request.status} IS NULL OR s.status = :#{#request.status})
             GROUP BY
                 s.id, s.name, s.status, s.code, s.created_at
             ORDER BY
                s.status DESC,
                s.created_at DESC
             """, countQuery = """
            SELECT
                COUNT(*)
            FROM
                subject s
            WHERE
                (:#{#request.name} IS NULL OR s.name LIKE CONCAT('%', TRIM(:#{#request.name}), '%'))
                AND (:#{#request.status} IS NULL OR s.status = :#{#request.status})
            """, nativeQuery = true)
    Page<ADSubjectResponse> getAll(Pageable pageable, ADSubjectSearchRequest request);

    @Query(value = """
                SELECT
                    CASE WHEN COUNT(*) > 0 THEN 'TRUE' ELSE 'FALSE' END
                FROM subject
                WHERE
                    status = 1 AND
                    code LIKE TRIM(:code) AND
                    (:idSubject IS NULL OR id != TRIM(:idSubject))
            """, nativeQuery = true)
    boolean isExistsCodeSubject(String code, String idSubject);

    @Query(value = """
                SELECT
                    CASE WHEN COUNT(*) > 0 THEN 'TRUE' ELSE 'FALSE' END
                FROM subject
                WHERE
                    status = 1 AND
                    name LIKE TRIM(:name) AND
                    (:idSubject IS NULL OR id != TRIM(:idSubject))
            """, nativeQuery = true)
    boolean isExistsNameSubject(String name, String idSubject);

}