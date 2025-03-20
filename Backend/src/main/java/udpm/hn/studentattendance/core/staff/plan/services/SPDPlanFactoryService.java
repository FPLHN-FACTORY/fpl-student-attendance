package udpm.hn.studentattendance.core.staff.plan.services;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDAddPlanFactoryRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanFactoryRequest;

public interface SPDPlanFactoryService {

    ResponseEntity<?> getAllList(SPDFilterPlanFactoryRequest request);

    ResponseEntity<?> getListFactory(String idPlan);

    ResponseEntity<?> createPlanFactory(SPDAddPlanFactoryRequest request);

    ResponseEntity<?> changeStatus(String idPlanFactory);

    ResponseEntity<?> deletePlanFactory(String idPlanFactory);

}
