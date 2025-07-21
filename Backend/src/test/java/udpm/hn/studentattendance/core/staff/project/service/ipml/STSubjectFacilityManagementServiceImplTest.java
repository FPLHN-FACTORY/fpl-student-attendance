package udpm.hn.studentattendance.core.staff.project.service.ipml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import udpm.hn.studentattendance.core.staff.project.model.response.USSubjectResponse;
import udpm.hn.studentattendance.core.staff.project.repository.STProjectSubjectFacilityExtendRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class STSubjectFacilityManagementServiceImplTest {
    @Mock
    private STProjectSubjectFacilityExtendRepository repository;
    @InjectMocks
    private STSubjectFacilityManagementServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetComboboxSubjectFacility() {
        String facilityId = "facility1";
        List<USSubjectResponse> mockSubjects = Collections.emptyList();
        when(repository.getSubjectFacility(facilityId)).thenReturn(mockSubjects);
        List<USSubjectResponse> result = service.getComboboxSubjectFacility(facilityId);
        assertNotNull(result);
        verify(repository, times(1)).getSubjectFacility(facilityId);
    }
}
