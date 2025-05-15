package udpm.hn.studentattendance.core.staff.project.service;

import udpm.hn.studentattendance.core.staff.project.model.response.USSubjectResponse;

import java.util.List;

public interface STSubjectFacilityManagementService {

    List<USSubjectResponse> getComboboxSubjectFacility(String facilityId);

}
