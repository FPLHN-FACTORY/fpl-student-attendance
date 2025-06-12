package udpm.hn.studentattendance.core.teacher.factory.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCPlanDateStudentFactoryRequest;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCStudentFactoryRequest;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCStudentFactoryExtendRepository;
import udpm.hn.studentattendance.core.teacher.factory.service.TCStudentFactoryService;
import udpm.hn.studentattendance.entities.UserStudentFactory;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class TCStudentFactoryServiceImpl implements TCStudentFactoryService {

        private final TCStudentFactoryExtendRepository teacherStudentFactoryExtendRepository;

        @Override
        public ResponseEntity<?> getAllStudentFactory(TCStudentFactoryRequest studentRequest) {
                Pageable pageable = PaginationHelper.createPageable(studentRequest, "createdAt");
                PageableObject pageableObject = PageableObject
                                .of(teacherStudentFactoryExtendRepository
                                                .getUserStudentInFactory(pageable, studentRequest.getFactoryId(),
                                                                studentRequest));
                return RouterHelper.responseSuccess("Lấy tất cả học sinh trong nhóm xưởng thành công", pageableObject);
        }

        @Override
        public ResponseEntity<?> getDetailAttendance(TCPlanDateStudentFactoryRequest request) {
                return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công",
                                teacherStudentFactoryExtendRepository.getAllPlanDateAttendanceByIdStudent(request));
        }

        @Override
        public ResponseEntity<?> getAllAttendance(String idFactory) {
                return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công",
                                teacherStudentFactoryExtendRepository.getAllPlanDateAttendanceByIdFactory(idFactory));
        }

        @Override
        public ResponseEntity<?> deleteStudentFactoryById(String studentFactoryId) {
                Optional<UserStudentFactory> userStudentFactory = teacherStudentFactoryExtendRepository
                                .findById(studentFactoryId);                if (userStudentFactory.isPresent()) {
                        teacherStudentFactoryExtendRepository.deleteById(studentFactoryId);
                        return RouterHelper.responseSuccess("Xoá sinh viên có mã " + userStudentFactory.get()
                                        .getUserStudent().getCode() + " thành công", null);
                }
                return RouterHelper.responseError("Sinh viên không tồn tại");
        }
}
