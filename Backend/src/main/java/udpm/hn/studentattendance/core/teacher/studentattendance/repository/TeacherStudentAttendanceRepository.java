package udpm.hn.studentattendance.core.teacher.studentattendance.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.teacher.studentattendance.model.response.TeacherStudentAttendanceResponse;
import udpm.hn.studentattendance.repositories.AttendanceRepository;

import java.util.List;

@Repository
public interface TeacherStudentAttendanceRepository extends AttendanceRepository {

    @Query(value = """
        select
        	ROW_NUMBER() OVER (ORDER BY a.created_at DESC) AS rowNumber,
            a.id as id,
        	us.code as studentCode,
        	us.name as studentName,
        	a.attendance_status as status
        from attendance a
        join user_student us
        on a.id_user_student = us.id
        where a.id_plan_date = :req
    """, nativeQuery = true)
    List<TeacherStudentAttendanceResponse> getAll(String req);

    @Query(value = """
        select usf.id_user_student
        from plan_date pd
        join plan_factory pf on pd.id_plan_factory = pf.id
        join factory f on pf.id_factory = f.id
        join user_student_factory usf on usf.id_factory = f.id
            
        where pd.id = :req
    """, nativeQuery = true)
    List<String> getIdStudentByIdPlanDate(String req);

    @Query(value = """
        select a.id
        from attendance a
        join plan_date pd on a.id_plan_date = pd.id
        join user_student us on a.id_user_student = us.id
        where pd.id = :b and us.id = :a  
    """, nativeQuery = true)
    String getIdAttendanceByIdStudentAndPlanDate(String a, String b);
}
