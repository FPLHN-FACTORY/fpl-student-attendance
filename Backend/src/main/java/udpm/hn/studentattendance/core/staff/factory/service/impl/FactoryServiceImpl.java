package udpm.hn.studentattendance.core.staff.factory.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.staff.factory.model.request.FactoryRequest;
import udpm.hn.studentattendance.core.staff.factory.repository.FactoryExtendRepository;
import udpm.hn.studentattendance.core.staff.factory.service.FactoryService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.repositories.FactoryRepository;

@Service
@RequiredArgsConstructor
@Validated
public class FactoryServiceImpl implements FactoryService {

    private final FactoryExtendRepository factoryRepository;

    @Override
    public ResponseEntity<?> getAllFactory(FactoryRequest factoryRequest) {
        Pageable pageable = PaginationHelper.createPageable(factoryRequest, "createdAt");
        PageableObject factories = PageableObject.of(factoryRepository.getAllFactory(pageable, factoryRequest));
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Hiển thị tất cả nhóm xưởng thành công",
                        factories
                ),
                HttpStatus.OK);
    }
}
