package udpm.hn.studentattendance.core.teacher.factory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCStudentFactoryRequest;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCStudentFactoryResponse;
import udpm.hn.studentattendance.repositories.UserStudentFactoryRepository;

@Repository
public interface TCStudentFactoryExtendRepository extends UserStudentFactoryRepository {
    @Query(value = """
            SELECT
                usf.id AS studentFactoryId,
                us.id AS studentId,
                ft.id AS factoryId,
                us.code AS studentCode,
                us.name AS studentName,
                us.email AS studentEmail,
                usf.status AS statusStudentFactory,
                ROW_NUMBER() OVER (ORDER BY usf.created_at DESC) AS rowNumber
            FROM user_student_factory usf
            LEFT JOIN user_student us ON us.id = usf.id_user_student
            LEFT JOIN factory ft ON ft.id = usf.id_factory
            WHERE
                ft.id = :factoryId
                AND ft.status = 1
                AND us.status = 1
                AND (:#{#studentRequest.status} IS NULL OR usf.status = :#{#studentRequest.status})
                AND (
                    :#{#studentRequest.searchQuery} IS NULL OR :#{#studentRequest.searchQuery} = ''
                    OR us.code LIKE CONCAT('%', :#{#studentRequest.searchQuery}, '%')
                    OR us.name LIKE CONCAT('%', :#{#studentRequest.searchQuery}, '%')
                    OR us.email LIKE CONCAT('%', :#{#studentRequest.searchQuery}, '%')
                )
            ORDER BY usf.created_at DESC
            """, countQuery = """
            SELECT COUNT(DISTINCT usf.id)
            FROM user_student_factory usf
            LEFT JOIN user_student us ON us.id = usf.id_user_student
            LEFT JOIN factory ft ON ft.id = usf.id_factory
            WHERE
                ft.id = :factoryId
                AND ft.status = 1
                AND us.status = 1
                AND (:#{#studentRequest.status} IS NULL OR usf.status = :#{#studentRequest.status})
                AND (
                    :#{#studentRequest.searchQuery} IS NULL OR :#{#studentRequest.searchQuery} = ''
                    OR us.code LIKE CONCAT('%', :#{#studentRequest.searchQuery}, '%')
                    OR us.name LIKE CONCAT('%', :#{#studentRequest.searchQuery}, '%')
                    OR us.email LIKE CONCAT('%', :#{#studentRequest.searchQuery}, '%')
                )

            """, nativeQuery = true)
    Page<TCStudentFactoryResponse> getUserStudentInFactory(Pageable pageable, String factoryId,
                                                           TCStudentFactoryRequest studentRequest);

    @Query(value = """
            SELECT
                CASE WHEN COUNT(distinct pd.id) > 0 THEN 'TRUE' ELSE 'FALSE' END
            FROM plan_date pd
            JOIN plan_factory pf ON pf.id = pd.id_plan_factory
            JOIN factory f ON pf.id_factory = f.id
            JOIN plan p ON pf.id_plan = p.id
            JOIN project pj ON f.id_project = pj.id
            JOIN subject_facility sf ON sf.id = pj.id_subject_facility
            JOIN facility f2 ON sf.id_facility = f2.id
            WHERE
                pd.status = 1 AND
                pf.status = 1 AND
                p.status = 1 AND
                f.status = 1 AND
                f2.status = 1 AND
                f.id = :idFactory AND
                EXISTS(
                    SELECT 1
                    FROM user_student us
                    JOIN user_student_factory usf ON usf.id_user_student = us.id
                    JOIN plan_factory pf2 ON pf2.id_factory = usf.id_factory
                    JOIN plan_date pd2 ON pd2.id_plan_factory = pf2.id
                    WHERE
                        us.id = :idUserStudent AND
                        usf.status = 1 AND
                        us.status = 1 AND
                        pf2.status = 1 AND
                        pd2.status = 1 AND
                        pd2.id <> pd.id AND
                        (
                            (pd2.start_date BETWEEN pd.start_date AND pd.end_date) OR
                            (pd2.end_date BETWEEN pd.start_date AND pd.end_date)
                        ) AND
                        pd2.start_date >= (UNIX_TIMESTAMP(CURRENT_DATE) * 1000)
                ) AND
                f2.id = :idFacility
            """, nativeQuery = true)
    boolean isStudentExistsShift(String idFacility, String idFactory, String idUserStudent);

}
