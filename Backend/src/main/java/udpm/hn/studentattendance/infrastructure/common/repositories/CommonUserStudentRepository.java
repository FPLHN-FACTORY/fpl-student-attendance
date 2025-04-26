package udpm.hn.studentattendance.infrastructure.common.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import udpm.hn.studentattendance.repositories.UserStudentRepository;


@Repository
public interface CommonUserStudentRepository extends UserStudentRepository {

    @Transactional
    @Modifying
    @Query(value = """
        UPDATE user_student_factory usf_target
        JOIN (
            SELECT DISTINCT usf.id
            FROM plan_date pd
            JOIN plan_factory pf ON pf.id = pd.id_plan_factory
            JOIN factory f ON pf.id_factory = f.id
            JOIN user_student_factory usf ON usf.id_factory = f.id
            JOIN plan p ON pf.id_plan = p.id
            JOIN project pj ON f.id_project = pj.id
            JOIN subject_facility sf ON sf.id = pj.id_subject_facility
            JOIN subject s2 ON sf.id_subject = s2.id
            JOIN semester s ON pj.id_semester = s.id
            JOIN level_project lp ON pj.id_level_project = lp.id
            JOIN facility f2 ON sf.id_facility = f2.id
            JOIN user_student us ON usf.id_user_student = us.id
            JOIN user_student_factory usf2 ON usf2.id_user_student = us.id
            JOIN plan_factory pf2 ON pf2.id_factory = usf2.id_factory
            JOIN plan_date pd2 ON pd2.id_plan_factory = pf2.id
            JOIN factory f3 ON f3.id = pf2.id_factory
            WHERE
                s2.status = 1 AND 
                s.status = 1 AND 
                lp.status = 1 AND
                pd.status = 1 AND
                pf.status = 1 AND
                p.status = 1 AND
                f3.status = 1 AND
                f.status = 1 AND
                f2.status = 1 AND
                usf.status = 1 AND
                usf2.status = 1 AND
                us.status = 1 AND
                pd.id <> pd2.id AND
                (pd2.start_date < pd.start_date OR pd2.start_date > pd.end_date) AND
                pd.start_date >= (UNIX_TIMESTAMP(CURRENT_DATE) * 1000) AND
                f.id = f3.id AND
                f.id = :idFactory
        ) usf_ids ON usf_ids.id = usf_target.id
        SET usf_target.status = 0
    """, nativeQuery = true)
    Integer disableAllStudentDuplicateShiftByIdFactory(String idFactory);

    @Transactional
    @Modifying
    @Query(value = """
        UPDATE user_student_factory usf_target
        JOIN (
            SELECT DISTINCT usf.id
            FROM plan_date pd
            JOIN plan_factory pf ON pf.id = pd.id_plan_factory
            JOIN factory f ON pf.id_factory = f.id
            JOIN user_student_factory usf ON usf.id_factory = f.id
            JOIN plan p ON pf.id_plan = p.id
            JOIN project pj ON f.id_project = pj.id
            JOIN subject_facility sf ON sf.id = pj.id_subject_facility
            JOIN subject s2 ON sf.id_subject = s2.id
            JOIN semester s ON pj.id_semester = s.id
            JOIN level_project lp ON pj.id_level_project = lp.id
            JOIN facility f2 ON sf.id_facility = f2.id
            JOIN user_student us ON usf.id_user_student = us.id
            JOIN user_student_factory usf2 ON usf2.id_user_student = us.id
            JOIN plan_factory pf2 ON pf2.id_factory = usf2.id_factory
            JOIN plan_date pd2 ON pd2.id_plan_factory = pf2.id
            JOIN factory f3 ON f3.id = pf2.id_factory
            JOIN project pj2 ON pj2.id = f3.id_project
            WHERE
                s2.status = 1 AND 
                s.status = 1 AND 
                lp.status = 1 AND
                pd.status = 1 AND
                pf.status = 1 AND
                p.status = 1 AND
                f3.status = 1 AND
                pj2.status = 1 AND
                f.status = 1 AND
                f2.status = 1 AND
                usf.status = 1 AND
                usf2.status = 1 AND
                us.status = 1 AND
                pd.id <> pd2.id AND
                (pd2.start_date < pd.start_date OR pd2.start_date > pd.end_date) AND
                pd.start_date >= (UNIX_TIMESTAMP(CURRENT_DATE) * 1000) AND
                pj.id = pj2.id AND
                pj.id = :idProject
        ) usf_ids ON usf_ids.id = usf_target.id
        SET usf_target.status = 0
    """, nativeQuery = true)
    Integer disableAllStudentDuplicateShiftByIdProject(String idProject);

    @Transactional
    @Modifying
    @Query(value = """
        UPDATE user_student_factory usf_target
        JOIN (
            SELECT DISTINCT usf.id
            FROM plan_date pd
            JOIN plan_factory pf ON pf.id = pd.id_plan_factory
            JOIN factory f ON pf.id_factory = f.id
            JOIN user_student_factory usf ON usf.id_factory = f.id
            JOIN plan p ON pf.id_plan = p.id
            JOIN project pj ON f.id_project = pj.id
            JOIN subject_facility sf ON sf.id = pj.id_subject_facility
            JOIN subject s2 ON sf.id_subject = s2.id
            JOIN semester s ON pj.id_semester = s.id
            JOIN level_project lp ON pj.id_level_project = lp.id
            JOIN facility f2 ON sf.id_facility = f2.id
            JOIN user_student us ON usf.id_user_student = us.id
            JOIN user_student_factory usf2 ON usf2.id_user_student = us.id
            JOIN plan_factory pf2 ON pf2.id_factory = usf2.id_factory
            JOIN plan_date pd2 ON pd2.id_plan_factory = pf2.id
            JOIN factory f3 ON pf2.id_factory = f3.id
            JOIN project pj2 ON f3.id_project = pj2.id
            JOIN subject_facility sf2 ON sf2.id = pj2.id_subject_facility
            JOIN subject s3 ON sf2.id_subject = s3.id
            WHERE
                s2.status = 1 AND
                s.status = 1 AND 
                lp.status = 1 AND
                pd.status = 1 AND
                pf.status = 1 AND
                p.status = 1 AND
                f3.status = 1 AND
                f.status = 1 AND
                f2.status = 1 AND
                usf.status = 1 AND
                usf2.status = 1 AND
                us.status = 1 AND
                pj2.status = 1 AND
                sf2.status = 1 AND
                s3.status = 1 AND
                pd.id <> pd2.id AND
                (pd2.start_date < pd.start_date OR pd2.start_date > pd.end_date) AND
                pd.start_date >= (UNIX_TIMESTAMP(CURRENT_DATE) * 1000) AND
                s.id = s3.id AND
                s.id = :idSubject
        ) usf_ids ON usf_ids.id = usf_target.id
        SET usf_target.status = 0
    """, nativeQuery = true)
    Integer disableAllStudentDuplicateShiftByIdSubject(String idSubject);

    @Transactional
    @Modifying
    @Query(value = """
        UPDATE user_student_factory usf_target
        JOIN (
            SELECT DISTINCT usf.id
            FROM plan_date pd
            JOIN plan_factory pf ON pf.id = pd.id_plan_factory
            JOIN factory f ON pf.id_factory = f.id
            JOIN user_student_factory usf ON usf.id_factory = f.id
            JOIN plan p ON pf.id_plan = p.id
            JOIN project pj ON f.id_project = pj.id
            JOIN subject_facility sf ON sf.id = pj.id_subject_facility
            JOIN subject s2 ON sf.id_subject = s2.id
            JOIN semester s ON pj.id_semester = s.id
            JOIN level_project lp ON pj.id_level_project = lp.id
            JOIN facility f2 ON sf.id_facility = f2.id
            JOIN user_student us ON usf.id_user_student = us.id
            JOIN user_student_factory usf2 ON usf2.id_user_student = us.id
            JOIN plan_factory pf2 ON pf2.id_factory = usf2.id_factory
            JOIN plan_date pd2 ON pd2.id_plan_factory = pf2.id
            JOIN factory f3 ON pf2.id_factory = f3.id
            JOIN project pj2 ON f3.id_project = pj2.id
            JOIN subject_facility sf2 ON sf2.id = pj2.id_subject_facility
            WHERE
                s2.status = 1 AND
                s.status = 1 AND 
                lp.status = 1 AND
                pd.status = 1 AND
                pf.status = 1 AND
                p.status = 1 AND
                f3.status = 1 AND
                f.status = 1 AND
                f2.status = 1 AND
                usf.status = 1 AND
                usf2.status = 1 AND
                us.status = 1 AND
                pj2.status = 1 AND
                sf2.status = 1 AND
                pd.id <> pd2.id AND
                (pd2.start_date < pd.start_date OR pd2.start_date > pd.end_date) AND
                pd.start_date >= (UNIX_TIMESTAMP(CURRENT_DATE) * 1000) AND
                sf.id = sf2.id AND
                sf.id = :idSubjectFacility
        ) usf_ids ON usf_ids.id = usf_target.id
        SET usf_target.status = 0
    """, nativeQuery = true)
    Integer disableAllStudentDuplicateShiftByIdSubjectFacility(String idSubjectFacility);

    @Transactional
    @Modifying
    @Query(value = """
        UPDATE user_student_factory usf_target
        JOIN (
            SELECT DISTINCT usf.id
            FROM plan_date pd
            JOIN plan_factory pf ON pf.id = pd.id_plan_factory
            JOIN factory f ON pf.id_factory = f.id
            JOIN user_student_factory usf ON usf.id_factory = f.id
            JOIN plan p ON pf.id_plan = p.id
            JOIN project pj ON f.id_project = pj.id
            JOIN subject_facility sf ON sf.id = pj.id_subject_facility
            JOIN subject s2 ON sf.id_subject = s2.id
            JOIN semester s ON pj.id_semester = s.id
            JOIN level_project lp ON pj.id_level_project = lp.id
            JOIN facility f2 ON sf.id_facility = f2.id
            JOIN user_student us ON usf.id_user_student = us.id
            JOIN user_student_factory usf2 ON usf2.id_user_student = us.id
            JOIN plan_factory pf2 ON pf2.id_factory = usf2.id_factory
            JOIN plan_date pd2 ON pd2.id_plan_factory = pf2.id
            JOIN factory f3 ON pf2.id_factory = f3.id
            JOIN project pj2 ON f3.id_project = pj2.id
            JOIN subject_facility sf2 ON sf2.id = pj2.id_subject_facility
            JOIN facility f4 ON f4.id = sf2.id_facility
            WHERE
                s2.status = 1 AND
                s.status = 1 AND 
                lp.status = 1 AND
                pd.status = 1 AND
                pf.status = 1 AND
                p.status = 1 AND
                f3.status = 1 AND
                f.status = 1 AND
                f2.status = 1 AND
                usf.status = 1 AND
                usf2.status = 1 AND
                us.status = 1 AND
                pj2.status = 1 AND
                sf2.status = 1 AND
                f4.status = 1 AND
                pd.id <> pd2.id AND
                (pd2.start_date < pd.start_date OR pd2.start_date > pd.end_date) AND
                pd.start_date >= (UNIX_TIMESTAMP(CURRENT_DATE) * 1000) AND
                f2.id = f4.id AND
                f2.id = :idFacility
        ) usf_ids ON usf_ids.id = usf_target.id
        SET usf_target.status = 0
    """, nativeQuery = true)
    Integer disableAllStudentDuplicateShiftByIdFacility(String idFacility);

    @Transactional
    @Modifying
    @Query(value = """
        UPDATE user_student_factory usf_target
        JOIN (
            SELECT DISTINCT usf.id
            FROM plan_date pd
            JOIN plan_factory pf ON pf.id = pd.id_plan_factory
            JOIN factory f ON pf.id_factory = f.id
            JOIN user_student_factory usf ON usf.id_factory = f.id
            JOIN plan p ON pf.id_plan = p.id
            JOIN project pj ON f.id_project = pj.id
            JOIN subject_facility sf ON sf.id = pj.id_subject_facility
            JOIN subject s2 ON sf.id_subject = s2.id
            JOIN semester s ON pj.id_semester = s.id
            JOIN level_project lp ON pj.id_level_project = lp.id
            JOIN facility f2 ON sf.id_facility = f2.id
            JOIN user_student us ON usf.id_user_student = us.id
            JOIN user_student_factory usf2 ON usf2.id_user_student = us.id
            JOIN plan_factory pf2 ON pf2.id_factory = usf2.id_factory
            JOIN plan_date pd2 ON pd2.id_plan_factory = pf2.id
            JOIN factory f3 ON pf2.id_factory = f3.id
            JOIN project pj2 ON f3.id_project = pj2.id
            JOIN semester s3 ON pj2.id_semester = s3.id
            WHERE
                s2.status = 1 AND
                s.status = 1 AND 
                lp.status = 1 AND
                pd.status = 1 AND
                pf.status = 1 AND
                p.status = 1 AND
                f3.status = 1 AND
                f.status = 1 AND
                f2.status = 1 AND
                usf.status = 1 AND
                usf2.status = 1 AND
                us.status = 1 AND
                pj2.status = 1 AND
                s3.status = 1 AND
                pd.id <> pd2.id AND
                (pd2.start_date < pd.start_date OR pd2.start_date > pd.end_date) AND
                pd.start_date >= (UNIX_TIMESTAMP(CURRENT_DATE) * 1000) AND
                s.id = s3.id AND
                s.id = :idSemester
        ) usf_ids ON usf_ids.id = usf_target.id
        SET usf_target.status = 0
    """, nativeQuery = true)
    Integer disableAllStudentDuplicateShiftByIdSemester(String idSemester);

    @Transactional
    @Modifying
    @Query(value = """
        UPDATE user_student_factory usf_target
        JOIN (
            SELECT DISTINCT usf.id
            FROM plan_date pd
            JOIN plan_factory pf ON pf.id = pd.id_plan_factory
            JOIN factory f ON pf.id_factory = f.id
            JOIN user_student_factory usf ON usf.id_factory = f.id
            JOIN plan p ON pf.id_plan = p.id
            JOIN project pj ON f.id_project = pj.id
            JOIN subject_facility sf ON sf.id = pj.id_subject_facility
            JOIN subject s2 ON sf.id_subject = s2.id
            JOIN semester s ON pj.id_semester = s.id
            JOIN level_project lp ON pj.id_level_project = lp.id
            JOIN facility f2 ON sf.id_facility = f2.id
            JOIN user_student us ON usf.id_user_student = us.id
            JOIN user_student_factory usf2 ON usf2.id_user_student = us.id
            JOIN plan_factory pf2 ON pf2.id_factory = usf2.id_factory
            JOIN plan_date pd2 ON pd2.id_plan_factory = pf2.id
            JOIN factory f3 ON pf2.id_factory = f3.id
            JOIN project pj2 ON f3.id_project = pj2.id
            JOIN level_project lp2 ON pj2.id_level_project = lp2.id
            WHERE
                s2.status = 1 AND
                s.status = 1 AND 
                lp.status = 1 AND
                pd.status = 1 AND
                pf.status = 1 AND
                p.status = 1 AND
                f3.status = 1 AND
                f.status = 1 AND
                f2.status = 1 AND
                usf.status = 1 AND
                usf2.status = 1 AND
                us.status = 1 AND
                pj2.status = 1 AND
                lp2.status = 1 AND
                pd.id <> pd2.id AND
                (pd2.start_date < pd.start_date OR pd2.start_date > pd.end_date) AND
                pd.start_date >= (UNIX_TIMESTAMP(CURRENT_DATE) * 1000) AND
                lp.id = lp2.id AND
                lp.id = :idLevelProject
        ) usf_ids ON usf_ids.id = usf_target.id
        SET usf_target.status = 0
    """, nativeQuery = true)
    Integer disableAllStudentDuplicateShiftByIdLevelProject(String idLevelProject);

    @Transactional
    @Modifying
    @Query(value = """
        UPDATE user_student_factory usf_target
        JOIN (
            SELECT DISTINCT usf.id
            FROM plan_date pd
            JOIN plan_factory pf ON pf.id = pd.id_plan_factory
            JOIN factory f ON pf.id_factory = f.id
            JOIN user_student_factory usf ON usf.id_factory = f.id
            JOIN plan p ON pf.id_plan = p.id
            JOIN project pj ON f.id_project = pj.id
            JOIN subject_facility sf ON sf.id = pj.id_subject_facility
            JOIN subject s2 ON sf.id_subject = s2.id
            JOIN semester s ON pj.id_semester = s.id
            JOIN level_project lp ON pj.id_level_project = lp.id
            JOIN facility f2 ON sf.id_facility = f2.id
            JOIN user_student us ON usf.id_user_student = us.id
            JOIN user_student_factory usf2 ON usf2.id_user_student = us.id
            JOIN plan_factory pf2 ON pf2.id_factory = usf2.id_factory
            JOIN plan_date pd2 ON pd2.id_plan_factory = pf2.id
            JOIN plan p2 ON p2.id = pf2.id_plan
            WHERE
                s2.status = 1 AND 
                s.status = 1 AND 
                lp.status = 1 AND
                pd.status = 1 AND
                pf.status = 1 AND
                p.status = 1 AND
                p2.status = 1 AND
                f.status = 1 AND
                f2.status = 1 AND
                usf.status = 1 AND
                usf2.status = 1 AND
                us.status = 1 AND
                pd.id <> pd2.id AND
                (pd2.start_date < pd.start_date OR pd2.start_date > pd.end_date) AND
                pd.start_date >= (UNIX_TIMESTAMP(CURRENT_DATE) * 1000) AND
                p.id = :idPlan AND
                p2.id <> :idPlan
        ) usf_ids ON usf_ids.id = usf_target.id
        SET usf_target.status = 0
    """, nativeQuery = true)
    Integer disableAllStudentDuplicateShiftByIdPlan(String idPlan);

    @Transactional
    @Modifying
    @Query(value = """
        UPDATE user_student_factory usf_target
        JOIN (
            SELECT DISTINCT usf.id
            FROM plan_date pd
            JOIN plan_factory pf ON pf.id = pd.id_plan_factory
            JOIN factory f ON pf.id_factory = f.id
            JOIN user_student_factory usf ON usf.id_factory = f.id
            JOIN plan p ON pf.id_plan = p.id
            JOIN project pj ON f.id_project = pj.id
            JOIN subject_facility sf ON sf.id = pj.id_subject_facility
            JOIN subject s2 ON sf.id_subject = s2.id
            JOIN semester s ON pj.id_semester = s.id
            JOIN level_project lp ON pj.id_level_project = lp.id
            JOIN facility f2 ON sf.id_facility = f2.id
            JOIN user_student us ON usf.id_user_student = us.id
            JOIN user_student_factory usf2 ON usf2.id_user_student = us.id
            JOIN plan_factory pf2 ON pf2.id_factory = usf2.id_factory
            JOIN plan_date pd2 ON pd2.id_plan_factory = pf2.id
            WHERE
                s2.status = 1 AND 
                s.status = 1 AND 
                lp.status = 1 AND
                pd.status = 1 AND
                pf.status = 1 AND
                p.status = 1 AND
                f.status = 1 AND
                f2.status = 1 AND
                usf.status = 1 AND
                usf2.status = 1 AND
                us.status = 1 AND
                pd.id <> pd2.id AND
                (pd2.start_date < pd.start_date OR pd2.start_date > pd.end_date) AND
                pd.start_date >= (UNIX_TIMESTAMP(CURRENT_DATE) * 1000) AND
                pf.id = :idPlanFactory AND
                pf2.id <> :idPlanFactory
        ) usf_ids ON usf_ids.id = usf_target.id
        SET usf_target.status = 0
    """, nativeQuery = true)
    Integer disableAllStudentDuplicateShiftByIdPlanFactory(String idPlanFactory);

    @Transactional
    @Modifying
    @Query(value = """
        UPDATE user_student_factory usf_target
        JOIN (
            SELECT DISTINCT usf2.id
            FROM plan_date pd
            JOIN plan_factory pf ON pf.id = pd.id_plan_factory
            JOIN factory f ON pf.id_factory = f.id
            JOIN user_student_factory usf ON usf.id_factory = f.id
            JOIN plan p ON pf.id_plan = p.id
            JOIN project pj ON f.id_project = pj.id
            JOIN subject_facility sf ON sf.id = pj.id_subject_facility
            JOIN subject s2 ON sf.id_subject = s2.id
            JOIN semester s ON pj.id_semester = s.id
            JOIN level_project lp ON pj.id_level_project = lp.id
            JOIN facility f2 ON sf.id_facility = f2.id
            JOIN user_student us ON usf.id_user_student = us.id
            JOIN user_student_factory usf2 ON usf2.id_user_student = us.id
            JOIN plan_factory pf2 ON pf2.id_factory = usf2.id_factory
            JOIN plan_date pd2 ON pd2.id_plan_factory = pf2.id
            WHERE
                s2.status = 1 AND 
                s.status = 1 AND 
                lp.status = 1 AND
                pd.status = 1 AND
                pf.status = 1 AND
                p.status = 1 AND
                f.status = 1 AND
                f2.status = 1 AND
                usf.status = 1 AND
                usf2.status = 1 AND
                us.status = 1 AND
                pd.id <> pd2.id AND
                f.id <> :idFactory AND
                (pd2.start_date < pd.start_date OR pd2.start_date > pd.end_date) AND
                pd.start_date >= (UNIX_TIMESTAMP(CURRENT_DATE) * 1000) AND
                pd.start_date = :startDate AND
                usf2.id_factory = :idFactory
        ) usf_ids ON usf_ids.id = usf_target.id
        SET usf_target.status = 0
    """, nativeQuery = true)
    Integer disableAllStudentDuplicateShiftByStartDate(String idFactory, Long startDate);

}
