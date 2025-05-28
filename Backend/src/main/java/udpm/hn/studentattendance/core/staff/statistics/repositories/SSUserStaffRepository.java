package udpm.hn.studentattendance.core.staff.statistics.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.staff.statistics.model.response.SSUserResponse;
import udpm.hn.studentattendance.repositories.UserStaffRepository;

import java.util.List;

@Repository
public interface SSUserStaffRepository extends UserStaffRepository {

    @Query(value = """
        SELECT
            us.id,
            us.code,
            us.name,
            us.email_fe AS email
        FROM user_staff us
        WHERE
            us.status = 1 AND
            us.email_fe <> :email AND
            EXISTS(
                SELECT 1
                FROM role r
                JOIN facility f ON r.id_facility = f.id
                WHERE
                    r.id_user_staff = us.id AND
                    r.code = 1 AND
                    f.status = 1 AND
                    r.status = 1 AND
                    f.id = :idFacility
            )
        ORDER BY us.name ASC
    """, nativeQuery = true)
    List<SSUserResponse> getAllListStaff(String idFacility, String email);

    @Query(value = """
        SELECT
            us.id,
            us.code,
            us.name,
            us.email_fe AS email
        FROM user_staff us
        WHERE
            us.status = 1 AND
            us.email_fe <> :email AND
            EXISTS(
                SELECT 1
                FROM role r
                JOIN facility f ON r.id_facility = f.id
                JOIN subject_facility sf ON f.id = sf.id_facility
                JOIN project p ON sf.id = p.id_subject_facility
                JOIN semester s ON p.id_semester = s.id
                JOIN factory f2 ON p.id = f2.id_project
                WHERE
                    r.id_user_staff = us.id AND
                    s.status = 1 AND
                    sf.status = 1 AND
                    p.status = 1 AND
                    f2.status = 1 AND
                    r.code = 3 AND
                    f.status = 1 AND
                    r.status = 1 AND
                    f2.id_user_staff = us.id AND
                    f.id = :idFacility AND
                    s.id = :idSemester
            )
        ORDER BY us.name ASC
    """, nativeQuery = true)
    List<SSUserResponse> getAllListTeacher(String idFacility, String idSemester, String email);

}
