package udpm.hn.studentattendance.core.admin.staff.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import udpm.hn.studentattendance.core.admin.staff.model.request.AdChangeStaffRoleRequest;
import udpm.hn.studentattendance.core.admin.staff.model.request.AdStaffRoleRequest;
import udpm.hn.studentattendance.core.admin.staff.service.AdStaffRoleService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/staffs")
public class AdStaffRoleRestController {

    private AdStaffRoleService adStaffRoleService;
    @GetMapping("/role/{staffId}")
    public ResponseEntity<?> getAllRole(String staffId){
        return adStaffRoleService.getAllRole(staffId);
    }
    @GetMapping("/role-check")
    public ResponseEntity<?> getRoleCheck(AdStaffRoleRequest adStaffRoleRequest){
        return adStaffRoleService.getRoleChecked(adStaffRoleRequest);
    }

    @PutMapping("/change-role")
    public ResponseEntity<?> changeStaffRole(AdChangeStaffRoleRequest adChangeStaffRoleRequest){
        return adStaffRoleService.changeStaffRole(adChangeStaffRoleRequest);
    }

    @GetMapping("/facilities")
    public ResponseEntity<?> getFacilities(){
        return adStaffRoleService.getFacilities();
    }
}
