package udpm.hn.studentattendance.core.staff.factory.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.staff.factory.model.request.StaffFactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.repository.StaffFactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.factory.service.StaffFactoryService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;

@Service
@RequiredArgsConstructor
@Validated
public class StaffFactoryServiceImpl implements StaffFactoryService {

    private final StaffFactoryExtendRepository factoryRepository;

    @Override
    public ResponseEntity<?> getAllFactory(StaffFactoryRequest staffFactoryRequest) {
        Pageable pageable = PaginationHelper.createPageable(staffFactoryRequest, "createdAt");
        PageableObject factories = PageableObject.of(factoryRepository.getAllFactory(pageable, staffFactoryRequest));
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Hiển thị tất cả nhóm xưởng thành công",
                        factories
                ),
                HttpStatus.OK);
    }
}
