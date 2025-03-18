package udpm.hn.studentattendance.core.staff.plan.services;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDAddOrUpdatePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDCreatePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterCreatePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateDetailRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateRequest;

public interface SPDPlanDateService {

    ResponseEntity<?> getAllSubject();

    ResponseEntity<?> getAllLevel();

    ResponseEntity<?> getListSemester();

    ResponseEntity<?> getAllYear();

    ResponseEntity<?> getAllList(SPDFilterPlanDateRequest request);

    ResponseEntity<?> getDetail(String idFactory);

    ResponseEntity<?> getAllDetailList(SPDFilterPlanDateDetailRequest request);

    ResponseEntity<?> deletePlanDate(String idPlanDate);

    ResponseEntity<?> updatePlanDate(SPDAddOrUpdatePlanDateRequest request);

    ResponseEntity<?> addPlanDate(SPDAddOrUpdatePlanDateRequest request);

    ResponseEntity<?> getListFactory(SPDFilterCreatePlanDateRequest request);

    ResponseEntity<?> createPlanDate(SPDCreatePlanDateRequest request);

}
