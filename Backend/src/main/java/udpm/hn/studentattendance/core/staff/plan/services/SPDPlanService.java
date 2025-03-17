package udpm.hn.studentattendance.core.staff.plan.services;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDCreatePlanRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterCreatePlanRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanRequest;

public interface SPDPlanService {

    ResponseEntity<?> getAllSubject();

    ResponseEntity<?> getAllLevel();

    ResponseEntity<?> getListSemester();

    ResponseEntity<?> getAllYear();

    ResponseEntity<?> getAllList(SPDFilterPlanRequest request);

    ResponseEntity<?> getPlan(String idPlan);

    ResponseEntity<?> getListProject(SPDFilterCreatePlanRequest request);

    ResponseEntity<?> getListFactory(String idPlan);

    ResponseEntity<?> createPlan(SPDCreatePlanRequest request);

}
