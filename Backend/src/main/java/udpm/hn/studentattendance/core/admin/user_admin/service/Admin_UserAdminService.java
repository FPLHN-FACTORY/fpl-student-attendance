package udpm.hn.studentattendance.core.admin.user_admin.service;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.user_admin.model.request.Admin_UserAdminCreateOrUpdateRequest;
import udpm.hn.studentattendance.core.admin.user_admin.model.request.Admin_UserAdminRequest;

public interface Admin_UserAdminService {
    ResponseEntity<?> getAllUserAdmin(Admin_UserAdminRequest request);

    ResponseEntity<?> getUserAdminById(String id);

    ResponseEntity<?> createUserAdmin(Admin_UserAdminCreateOrUpdateRequest createOrUpdateRequest);

    ResponseEntity<?> updateUserAdmin(Admin_UserAdminCreateOrUpdateRequest createOrUpdateRequest, String id);

    ResponseEntity<?> changeStatus(String id);


}
