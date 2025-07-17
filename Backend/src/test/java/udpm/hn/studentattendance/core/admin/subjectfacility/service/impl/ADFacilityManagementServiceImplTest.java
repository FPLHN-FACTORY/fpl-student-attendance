package udpm.hn.studentattendance.core.admin.subjectfacility.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import udpm.hn.studentattendance.core.admin.subjectfacility.repository.ADFacilityRepository;
import udpm.hn.studentattendance.helpers.RedisCacheHelper;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.config.redis.service.RedisService;

import java.util.Collections;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

public class ADFacilityManagementServiceImplTest {
    @Mock
    private ADFacilityRepository repository;
    @Mock
    private RedisService redisService;
    @Mock
    private RedisCacheHelper redisCacheHelper;
    @InjectMocks
    private ADFacilityManagementServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetComboboxFacility() {
        String idSubject = "subject1";
        // Capture the supplier and manually invoke it to simulate cache miss
        ArgumentCaptor<Supplier> supplierCaptor = ArgumentCaptor.forClass(Supplier.class);
        when(redisCacheHelper.getOrSet(anyString(), supplierCaptor.capture(), any(), anyLong())).thenReturn(null);
        when(repository.getFacility(idSubject)).thenReturn(Collections.emptyList());
        ResponseEntity<?> response = service.getComboboxFacility(idSubject);
        // Simulate cache miss by invoking the supplier
        supplierCaptor.getValue().get();
        assertNotNull(response);
        verify(repository, atLeastOnce()).getFacility(idSubject);
    }

    @Test
    void testGetListFacility() {
        // Capture the supplier and manually invoke it to simulate cache miss
        ArgumentCaptor<Supplier> supplierCaptor = ArgumentCaptor.forClass(Supplier.class);
        when(redisCacheHelper.getOrSet(anyString(), supplierCaptor.capture(), any(), anyLong())).thenReturn(null);
        when(repository.getFacilities(EntityStatus.ACTIVE)).thenReturn(Collections.emptyList());
        ResponseEntity<?> response = service.getListFacility();
        // Simulate cache miss by invoking the supplier
        supplierCaptor.getValue().get();
        assertNotNull(response);
        verify(repository, atLeastOnce()).getFacilities(EntityStatus.ACTIVE);
    }
}