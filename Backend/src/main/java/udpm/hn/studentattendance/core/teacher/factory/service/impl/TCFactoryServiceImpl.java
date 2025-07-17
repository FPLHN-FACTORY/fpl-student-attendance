package udpm.hn.studentattendance.core.teacher.factory.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import com.fasterxml.jackson.core.type.TypeReference;
import udpm.hn.studentattendance.helpers.RedisCacheHelper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class TCFactoryServiceImpl implements TCFactoryService {

        private final TCFactoryExtendRepository teacherStudentFactoryExtendRepository;

        private final SessionHelper sessionHelper;

        private final TCProjectExtendRepository teacherStudentProjectExtendRepository;

        private final TCSemesterExtendRepository semesterExtendRepository;

        private final RedisCacheHelper redisCacheHelper;

        private final RedisInvalidationHelper redisInvalidationHelper;

        @Value("${spring.cache.redis.time-to-live}")
        private long redisTTL;

        @Override
        public ResponseEntity<?> getAllFactoryByTeacher(TCFactoryRequest teacherStudentRequest) {
                String key = RedisPrefixConstant.REDIS_PREFIX_TEACHER_FACTORY + "factory_"
                                + sessionHelper.getUserCode()
                                + "_" + sessionHelper.getFacilityId()
                                + "_" + teacherStudentRequest.toString();
                PageableObject listFactoryByTeacher = redisCacheHelper.getOrSet(
                                key,
                                () -> PageableObject.of(teacherStudentFactoryExtendRepository.getAllFactoryByTeacher(
                                                PaginationHelper.createPageable(teacherStudentRequest, "createdAt"),
                                                sessionHelper.getFacilityId(), sessionHelper.getUserCode(),
                                                teacherStudentRequest)),
                                new TypeReference<PageableObject<?>>() {
                                },
                                redisTTL);
                return RouterHelper.responseSuccess(
                                "Lấy tất cả nhóm xưởng do giảng viên " + sessionHelper.getUserCode() + " thành công",
                                listFactoryByTeacher);
        }

        @Override
        public ResponseEntity<?> getAllProjectByFacility() {
                String key = RedisPrefixConstant.REDIS_PREFIX_TEACHER_FACTORY + "projects_"
                                + sessionHelper.getFacilityId();
                List<Project> projects = redisCacheHelper.getOrSet(
                                key,
                                () -> teacherStudentProjectExtendRepository
                                                .getAllProjectName(sessionHelper.getFacilityId()),
                                new TypeReference<List<Project>>() {
                                },
                                redisTTL);
                return RouterHelper.responseSuccess("Lấy tất cả dự án theo cơ sở thành công", projects);
        }

        @Override
        public ResponseEntity<?> getAllPlanDateByFactory() {
                return null;
        }

        @Override
        public ResponseEntity<?> getAllSemester() {
                String key = RedisPrefixConstant.REDIS_PREFIX_TEACHER_FACTORY + "semesters_active";
                List<Semester> semesters = redisCacheHelper.getOrSet(
                                key,
                                () -> semesterExtendRepository.getAllSemester(EntityStatus.ACTIVE),
                                new TypeReference<List<Semester>>() {
                                },
                                redisTTL);
                return RouterHelper.responseSuccess("Lấy tất cả học kỳ thành công", semesters);
        }

}
