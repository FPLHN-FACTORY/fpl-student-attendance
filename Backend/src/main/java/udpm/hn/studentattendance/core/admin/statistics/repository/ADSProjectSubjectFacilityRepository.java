package udpm.hn.studentattendance.core.admin.statistics.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.core.admin.statistics.model.response.ADSProjectSubjectFacilityResponse;
import udpm.hn.studentattendance.repositories.ProjectRepository;

import java.util.List;

@Repository
public interface ADSProjectSubjectFacilityRepository extends ProjectRepository {

    @Query(value = """
            WITH
            semester_terms AS (
                -- Tạo danh sách các kỳ học cố định
                SELECT 'SPRING' as term, 'SPRING' as display_term
                UNION ALL
                SELECT 'SUMMER' as term, 'SUMMER' as display_term
                UNION ALL
                SELECT 'FALL' as term, 'FALL' as display_term
            ),
            factory_stats AS (
                -- Tính toán cho từng xưởng: số sinh viên và số buổi học
                SELECT
                    s.id as semester_id,
                    s.code,
                    UPPER(SUBSTRING_INDEX(s.code, '-', 1)) as term,
                    s.year,
                    ft.id as factory_id,
                    COUNT(DISTINCT usf.id) as student_count,
                    COUNT(DISTINCT CASE WHEN pd.start_date <= UNIX_TIMESTAMP(NOW()) * 1000 THEN pd.id END) as shift_count,
                    COUNT(DISTINCT ad.id) as attendance_count
                FROM semester s
                JOIN project p ON p.id_semester = s.id
                JOIN factory ft ON ft.id_project = p.id
                LEFT JOIN user_student_factory usf ON usf.id_factory = ft.id
                LEFT JOIN plan_factory pf ON pf.id_factory = ft.id
                LEFT JOIN plan_date pd ON pd.id_plan_factory = pf.id AND pd.end_date <= s.to_date
                LEFT JOIN attendance ad ON ad.id_plan_date = pd.id
                WHERE s.year = :year
                GROUP BY s.id, s.code, term, s.year, ft.id
            ),
            semester_totals AS (
                -- Tổng hợp theo học kỳ
                SELECT
                    semester_id,
                    MAX(code) as code,
                    term,
                    year,
                    COUNT(DISTINCT factory_id) as totalFactory,
                    SUM(student_count) as totalStudentFactory,
                    SUM(shift_count) as totalCurrentShift,
                    SUM(attendance_count) as totalAttendance,
                    -- Tính tổng số điểm danh có thể có: tổng của (số sinh viên × số buổi học) cho mỗi xưởng
                    SUM(student_count * shift_count) as totalPossibleAttendance
                FROM factory_stats
                GROUP BY semester_id, term, year
            ),
            all_semesters AS (
                -- Kết hợp tất cả các kỳ, kể cả kỳ chưa có dữ liệu
                SELECT
                    st.term,
                    COALESCE(st2.code, CONCAT(st.display_term, '-', :year)) as code,
                    :year as year,
                    COALESCE(st2.totalFactory, 0) as totalFactory,
                    COALESCE(st2.totalStudentFactory, 0) as totalStudentFactory,
                    COALESCE(st2.totalCurrentShift, 0) as totalCurrentShift,
                    COALESCE(st2.totalAttendance, 0) as totalAttendance,
                    COALESCE(st2.totalPossibleAttendance, 0) as totalPossibleAttendance
                FROM semester_terms st
                LEFT JOIN semester_totals st2 ON st.term = st2.term AND st2.year = :year
            )

            SELECT
                code,
                totalFactory,
                totalStudentFactory,
                totalCurrentShift,
                totalAttendance,
                totalPossibleAttendance,
                CASE
                    WHEN totalPossibleAttendance > 0
                    THEN ROUND(
                                totalAttendance * 100.0 / totalPossibleAttendance, 2
                            )
                    ELSE 0
                END AS attendancePercentage
            FROM all_semesters
            ORDER BY
                CASE
                    WHEN term = 'SPRING' THEN 1
                    WHEN term = 'SUMMER' THEN 2
                    WHEN term = 'FALL' THEN 3
                    ELSE 4
                END
            """, nativeQuery = true)
    List<ADSProjectSubjectFacilityResponse> getProjectSubjectFacilityResponses(int year);

}
