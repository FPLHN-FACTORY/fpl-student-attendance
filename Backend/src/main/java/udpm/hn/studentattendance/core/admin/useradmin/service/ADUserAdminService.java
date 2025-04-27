package udpm.hn.studentattendance.core.admin.useradmin.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.useradmin.model.request.ADUserAdminChangePowerShiftRequest;
import udpm.hn.studentattendance.core.admin.useradmin.model.request.ADUserAdminCreateOrUpdateRequest;
import udpm.hn.studentattendance.core.admin.useradmin.model.request.ADUserAdminRequest;

public interface ADUserAdminService {
    ResponseEntity<?> getAllUserAdmin(ADUserAdminRequest request);

    ResponseEntity<?> getUserAdminById(String id);

    ResponseEntity<?> createUserAdmin(ADUserAdminCreateOrUpdateRequest createOrUpdateRequest);

    ResponseEntity<?> updateUserAdmin(ADUserAdminCreateOrUpdateRequest createOrUpdateRequest, String id);

    ResponseEntity<?> changeStatus(String id);

    ResponseEntity<?> isMySelf(String userAdminId);

    ResponseEntity<?> changePowerShift(ADUserAdminChangePowerShiftRequest adminUserAdminChangePowerShiftRequest);

    ResponseEntity<?> getAllUserStaff();


}
