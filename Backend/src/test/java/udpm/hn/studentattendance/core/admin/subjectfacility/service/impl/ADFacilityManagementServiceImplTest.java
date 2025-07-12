package udpm.hn.studentattendance.core.admin.subjectfacility.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.subjectfacility.repository.ADFacilityRepository;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ADFacilityManagementServiceImplTest {
    @Mock
    private ADFacilityRepository repository;
    @Mock
    private RedisService redisService;
    @Mock
    private RedisInvalidationHelper redisInvalidationHelper;
    @InjectMocks
    private ADFacilityManagementServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetComboboxFacility() {
        String idSubject = "subject1";
        when(redisService.get(anyString())).thenReturn(null);
        when(repository.getFacility(idSubject)).thenReturn(Collections.emptyList());
        ResponseEntity<?> response = service.getComboboxFacility(idSubject);
        assertNotNull(response);
        verify(repository, times(1)).getFacility(idSubject);
    }

    @Test
    void testGetListFacility() {
        when(redisService.get(anyString())).thenReturn(null);
        when(repository.getFacilities(EntityStatus.ACTIVE)).thenReturn(Collections.emptyList());
        ResponseEntity<?> response = service.getListFacility();
        assertNotNull(response);
        verify(repository, times(1)).getFacilities(EntityStatus.ACTIVE);
    }
}