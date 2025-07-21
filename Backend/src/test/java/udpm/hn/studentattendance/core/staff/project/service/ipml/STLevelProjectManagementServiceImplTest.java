package udpm.hn.studentattendance.core.staff.project.service.ipml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import udpm.hn.studentattendance.core.staff.project.model.response.USLevelProjectResponse;
import udpm.hn.studentattendance.core.staff.project.repository.STLevelProjectExtendRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class STLevelProjectManagementServiceImplTest {
    @Mock
    private STLevelProjectExtendRepository repository;
    @InjectMocks
    private STLevelProjectManagementServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetComboboxLevelProject() {
        List<USLevelProjectResponse> mockLevelProjects = Collections.emptyList();
        when(repository.getLevelProject()).thenReturn(mockLevelProjects);
        List<USLevelProjectResponse> result = service.getComboboxLevelProject();
        assertNotNull(result);
        verify(repository, times(1)).getLevelProject();
    }
}
