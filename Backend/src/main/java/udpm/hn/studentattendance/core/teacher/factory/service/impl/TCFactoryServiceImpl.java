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
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
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

        @Value("${spring.cache.redis.time-to-live:3600}")
        private long redisTTL;

        @Override
        public ResponseEntity<?> getAllFactoryByTeacher(TCFactoryRequest teacherStudentRequest) {
                String cacheKey = "teacher:factory:" + sessionHelper.getUserCode() + ":" + sessionHelper.getFacilityId()
                                + ":" + teacherStudentRequest.toString();

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

                redisService.set(cacheKey, listFactoryByTeacher, redisTTL);

                return RouterHelper.responseSuccess("Lấy tất cả nhóm xưởng do giảng viên " + sessionHelper.getUserCode()
                                + " thành công", listFactoryByTeacher);
        }

        @Override
        public ResponseEntity<?> getAllProjectByFacility() {
                String cacheKey = "teacher:projects:" + sessionHelper.getFacilityId();

                Object cachedData = redisService.get(cacheKey);
                if (cachedData != null) {
                        return RouterHelper.responseSuccess("Lấy tất cả dự án theo cơ sở thành công (cached)",
                                        cachedData);
                }

                List<Project> projects = teacherStudentProjectExtendRepository
                                .getAllProjectName(sessionHelper.getFacilityId());

                redisService.set(cacheKey, projects, redisTTL * 2); // Cache for longer since projects don't change
                                                                    // often

                return RouterHelper.responseSuccess("Lấy tất cả dự án theo cơ sở thành công", projects);
        }

        @Override
        public ResponseEntity<?> getAllPlanDateByFactory() {
                return null;
        }

        @Override
        public ResponseEntity<?> getAllSemester() {
                String cacheKey = "teacher:semesters:active";

                Object cachedData = redisService.get(cacheKey);
                if (cachedData != null) {
                        return RouterHelper.responseSuccess("Lấy tất cả học kỳ thành công (cached)", cachedData);
                }

                List<Semester> semesters = semesterExtendRepository.getAllSemester(EntityStatus.ACTIVE);

                redisService.set(cacheKey, semesters, redisTTL * 4); // Cache for longer since semesters don't change
                                                                     // often

                return RouterHelper.responseSuccess("Lấy tất cả học kỳ thành công", semesters);
        }

        /**
         * Helper method to invalidate teacher factory-related caches for a specific
         * teacher
         */
        public void invalidateTeacherFactoryCaches() {
                String teacherCode = sessionHelper.getUserCode();
                String facilityId = sessionHelper.getFacilityId();

                // Delete all factory lists for this teacher
                redisService.deletePattern("teacher:factory:" + teacherCode + ":" + facilityId + ":*");
        }

        /**
         * Helper method to invalidate project-related caches for a specific facility
         */
        public void invalidateProjectCaches() {
                String facilityId = sessionHelper.getFacilityId();

                // Delete all project lists for this facility
                redisService.deletePattern("teacher:projects:" + facilityId);
        }
}
