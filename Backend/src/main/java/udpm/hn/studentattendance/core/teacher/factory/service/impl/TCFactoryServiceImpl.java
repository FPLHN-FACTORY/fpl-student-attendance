package udpm.hn.studentattendance.core.teacher.factory.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCFactoryRequest;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCFactoryExtendRepository;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCProjectExtendRepository;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCSemesterExtendRepository;
import udpm.hn.studentattendance.core.teacher.factory.service.TCFactoryService;
import udpm.hn.studentattendance.entities.Project;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class TCFactoryServiceImpl implements TCFactoryService {

        private final TCFactoryExtendRepository teacherStudentFactoryExtendRepository;

        private final SessionHelper sessionHelper;

        private final TCProjectExtendRepository teacherStudentProjectExtendRepository;

        private final TCSemesterExtendRepository semesterExtendRepository;

        private final RedisService redisService;

        private final RedisInvalidationHelper redisInvalidationHelper;

        @Override
        public ResponseEntity<?> getAllFactoryByTeacher(TCFactoryRequest teacherStudentRequest) {
                String cacheKey = RedisPrefixConstant.REDIS_PREFIX_TEACHER_FACTORY + "factory_"
                                + sessionHelper.getUserCode()
                                + "_" + sessionHelper.getFacilityId()
                                + "_" + teacherStudentRequest.toString();

                Object cachedData = redisService.get(cacheKey);
                if (cachedData != null) {
                        return RouterHelper.responseSuccess(
                                        "Lấy tất cả nhóm xưởng do giảng viên " + sessionHelper.getUserCode()
                                                        + " thành công (cached)",
                                        cachedData);
                }

                Pageable pageable = PaginationHelper.createPageable(teacherStudentRequest, "createdAt");
                PageableObject listFactoryByTeacher = PageableObject
                                .of(teacherStudentFactoryExtendRepository.getAllFactoryByTeacher(pageable,
                                                sessionHelper.getFacilityId(), sessionHelper.getUserCode(),
                                                teacherStudentRequest));

                redisService.setObject(cacheKey, listFactoryByTeacher);

                return RouterHelper.responseSuccess("Lấy tất cả nhóm xưởng do giảng viên " + sessionHelper.getUserCode()
                                + " thành công", listFactoryByTeacher);
        }

        @Override
        public ResponseEntity<?> getAllProjectByFacility() {
                String cacheKey = RedisPrefixConstant.REDIS_PREFIX_TEACHER_FACTORY + "projects_"
                                + sessionHelper.getFacilityId();

                Object cachedData = redisService.get(cacheKey);
                if (cachedData != null) {
                        return RouterHelper.responseSuccess("Lấy tất cả dự án theo cơ sở thành công (cached)",
                                        cachedData);
                }

                List<Project> projects = teacherStudentProjectExtendRepository
                                .getAllProjectName(sessionHelper.getFacilityId());

                redisService.setObject(cacheKey, projects);

                return RouterHelper.responseSuccess("Lấy tất cả dự án theo cơ sở thành công", projects);
        }

        @Override
        public ResponseEntity<?> getAllPlanDateByFactory() {
                return null;
        }

        @Override
        public ResponseEntity<?> getAllSemester() {
                String cacheKey = RedisPrefixConstant.REDIS_PREFIX_TEACHER_FACTORY + "semesters_active";

                Object cachedData = redisService.get(cacheKey);
                if (cachedData != null) {
                        return RouterHelper.responseSuccess("Lấy tất cả học kỳ thành công (cached)", cachedData);
                }

                List<Semester> semesters = semesterExtendRepository.getAllSemester(EntityStatus.ACTIVE);

                redisService.setObject(cacheKey, semesters);

                return RouterHelper.responseSuccess("Lấy tất cả học kỳ thành công", semesters);
        }

        public void invalidateTeacherFactoryCaches() {
                redisInvalidationHelper.invalidateAllCaches();
        }

}
