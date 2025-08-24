package udpm.hn.studentattendance.core.teacher.factory.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.teacher.factory.model.request.TCFilterShiftFactoryRequest;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCFacilityShiftRepository;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCPlanDateResponse;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCPlanFactoryRepository;
import udpm.hn.studentattendance.core.teacher.factory.model.response.TCPlanFactoryResponse;
import udpm.hn.studentattendance.core.teacher.factory.repository.TCPlanDateRepository;
import udpm.hn.studentattendance.core.teacher.factory.service.TCShiftFactoryService;
import udpm.hn.studentattendance.entities.FacilityShift;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TCShiftFactoryServiceImpl implements TCShiftFactoryService {

    private final TCPlanDateRepository tcPlanDateRepository;

    private final TCPlanFactoryRepository tcPlanFactoryRepository;

    private final TCFacilityShiftRepository tcFacilityShiftRepository;

    private final SessionHelper sessionHelper;

    @Override
    public ResponseEntity<?> getDetail(String idFactory) {
        Optional<TCPlanFactoryResponse> data = tcPlanFactoryRepository.getDetail(idFactory,
                sessionHelper.getFacilityId());
        return data
                .map(response -> RouterHelper.responseSuccess("Get dữ liệu thành công", response))
                .orElseGet(() -> RouterHelper.responseError("Không tìm thấy kế hoạch"));
    }

    @Override
    public ResponseEntity<?> getAllList(TCFilterShiftFactoryRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<TCPlanDateResponse> data = PageableObject
                .of(tcPlanDateRepository.getAllByFilter(pageable, request));
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> getListShift() {
        List<FacilityShift> data = tcFacilityShiftRepository.getAllList(sessionHelper.getFacilityId());
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

}
