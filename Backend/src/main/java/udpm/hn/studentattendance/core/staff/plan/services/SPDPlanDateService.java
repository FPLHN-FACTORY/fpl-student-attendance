package udpm.hn.studentattendance.core.staff.plan.services;

import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDAddOrUpdatePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDDeletePlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDFilterPlanDateRequest;
import udpm.hn.studentattendance.core.staff.plan.model.request.SPDUpdateLinkMeetRequest;

public interface SPDPlanDateService {

    ResponseEntity<?> getDetail(String idPlanFactory);

    ResponseEntity<?> getAllList(SPDFilterPlanDateRequest request);

    ResponseEntity<?> deletePlanDate(String idPlanDate);

    ResponseEntity<?> deleteMultiplePlanDate(SPDDeletePlanDateRequest request);

    ResponseEntity<?> updatePlanDate(SPDAddOrUpdatePlanDateRequest request);

    ResponseEntity<?> addPlanDate(SPDAddOrUpdatePlanDateRequest request);

    ResponseEntity<?> updateLinkMeet(SPDUpdateLinkMeetRequest request);

    ResponseEntity<?> sendMail(String idPlanFactory);

}
