package udpm.hn.studentattendance.core.admin.levelproject.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectCreateRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectSearchRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.request.ADLevelProjectUpdateRequest;
import udpm.hn.studentattendance.core.admin.levelproject.model.response.ADLevelProjectResponse;
import udpm.hn.studentattendance.core.admin.levelproject.repository.ADLevelProjectRepository;
import udpm.hn.studentattendance.entities.LevelProject;
import udpm.hn.studentattendance.helpers.RedisCacheHelper;
import udpm.hn.studentattendance.helpers.RedisInvalidationHelper;
import udpm.hn.studentattendance.helpers.UserActivityLogHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.config.redis.service.RedisService;
import udpm.hn.studentattendance.utils.CodeGeneratorUtils;
import udpm.hn.studentattendance.helpers.ValidateHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ADLevelProjectManagementServiceImplTest {

    @Mock
    private ADLevelProjectRepository repository;

    @Mock
    private CommonUserStudentRepository commonUserStudentRepository;

    @Mock
    private UserActivityLogHelper userActivityLogHelper;

    @Mock
    private RedisService redisService;

    @Mock
    private RedisInvalidationHelper redisInvalidationHelper;

    @Mock
    private RedisCacheHelper redisCacheHelper;

    @InjectMocks
    private ADLevelProjectManagementServiceImpl levelProjectService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(levelProjectService, "redisTTL", 3600L);
        // Removed unnecessary stubbing for redisCacheHelper.getOrSet
    }

    @Test
    @DisplayName("Test getListLevelProject should return data from cache if available")
    void testGetListLevelProjectFromCache() {
        // Given
        ADLevelProjectSearchRequest request = new ADLevelProjectSearchRequest();
        String cacheKey = RedisPrefixConstant.REDIS_PREFIX_LEVEL + "list_" +
                "page=" + request.getPage() +
                "_size=" + request.getSize() +
                "_orderBy=" + request.getOrderBy() +
                "_sortBy=" + request.getSortBy() +
                "_q=" + (request.getQ() != null ? request.getQ() : "") +
                "_name=" + (request.getName() != null ? request.getName() : "") +
                "_status=" + (request.getStatus() != null ? request.getStatus() : "");

        PageableObject mockData = mock(PageableObject.class);

        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong())).thenReturn(mockData);

        // When
        ResponseEntity<?> response = levelProjectService.getListLevelProject(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Hiển thị tất cả nhóm dự án thành công", apiResponse.getMessage());
        assertEquals(mockData, apiResponse.getData());

        // Verify repository was not called
        verify(redisCacheHelper).getOrSet(anyString(), any(), any(), anyLong());
        verify(repository, never()).getAll(any(Pageable.class), any(ADLevelProjectSearchRequest.class));
    }

    @Test
    @DisplayName("Test getListLevelProject should fetch and cache data if not in cache")
    void testGetListLevelProjectFromRepository() {
        // Given
        ADLevelProjectSearchRequest request = new ADLevelProjectSearchRequest();
        List<ADLevelProjectResponse> levelProjects = new ArrayList<>();
        ADLevelProjectResponse levelProjectResponse = mock(ADLevelProjectResponse.class);
        levelProjects.add(levelProjectResponse);
        Page<ADLevelProjectResponse> page = new PageImpl<>(levelProjects);
        when(redisCacheHelper.getOrSet(anyString(), any(), any(), anyLong()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(repository.getAll(any(Pageable.class), any(ADLevelProjectSearchRequest.class))).thenReturn(page);

        // When
        ResponseEntity<?> response = levelProjectService.getListLevelProject(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Hiển thị tất cả nhóm dự án thành công", apiResponse.getMessage());
        verify(redisCacheHelper).getOrSet(anyString(), any(), any(), anyLong());
        verify(repository).getAll(any(Pageable.class), any(ADLevelProjectSearchRequest.class));
    }

    @Test
    @DisplayName("Test createLevelProject should create level project successfully")
    void testCreateLevelProjectSuccess() {
        // Given
        ADLevelProjectCreateRequest request = new ADLevelProjectCreateRequest();
        request.setName("Advanced Project");
        request.setDescription("For advanced students");

        // Mock the static method call
        try (MockedStatic<CodeGeneratorUtils> mockedStatic = Mockito.mockStatic(CodeGeneratorUtils.class);
                MockedStatic<ValidateHelper> validateHelperMockedStatic = Mockito.mockStatic(ValidateHelper.class)) {
            mockedStatic.when(() -> CodeGeneratorUtils.generateCodeFromString("Advanced Project"))
                    .thenReturn("ADVANCED_PROJECT");
            validateHelperMockedStatic.when(() -> ValidateHelper.isValidCode("Advanced Project"))
                    .thenReturn(true);

            when(repository.isExistsLevelProject("ADVANCED_PROJECT", null)).thenReturn(false);

            LevelProject savedLevelProject = new LevelProject();
            savedLevelProject.setId("level-1");
            savedLevelProject.setName("Advanced Project");
            savedLevelProject.setCode("ADVANCED_PROJECT");
            savedLevelProject.setDescription("For advanced students");
            savedLevelProject.setStatus(EntityStatus.ACTIVE);

            when(repository.save(any(LevelProject.class))).thenReturn(savedLevelProject);

            // When
            ResponseEntity<?> response = levelProjectService.createLevelProject(request);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ApiResponse apiResponse = (ApiResponse) response.getBody();
            assertNotNull(apiResponse);
            assertEquals("Thêm nhóm dự án mới thành công", apiResponse.getMessage());
            assertEquals(savedLevelProject, apiResponse.getData());

            // Verify repository was called
            verify(repository).save(any(LevelProject.class));
            verify(userActivityLogHelper).saveLog("Vừa thêm nhóm dự án: Advanced Project");
            verify(redisInvalidationHelper).invalidateAllCaches();
        }
    }

    @Test
    @DisplayName("Test createLevelProject should return error if level project already exists")
    void testCreateLevelProjectAlreadyExists() {
        // Given
        ADLevelProjectCreateRequest request = new ADLevelProjectCreateRequest();
        request.setName("Advanced Project");
        request.setDescription("For advanced students");

        try (MockedStatic<CodeGeneratorUtils> mockedStatic = Mockito.mockStatic(CodeGeneratorUtils.class)) {
            mockedStatic.when(() -> CodeGeneratorUtils.generateCodeFromString("Advanced Project"))
                    .thenReturn("ADVANCED_PROJECT");

            when(repository.isExistsLevelProject("ADVANCED_PROJECT", null)).thenReturn(true);

            // When
            ResponseEntity<?> response = levelProjectService.createLevelProject(request);

            // Then
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            ApiResponse apiResponse = (ApiResponse) response.getBody();
            assertNotNull(apiResponse);
            assertEquals("Nhóm dự án đã tồn tại trong hệ thống", apiResponse.getMessage());

            // Verify repository was not called to save
            verify(repository, never()).save(any(LevelProject.class));
            verify(redisInvalidationHelper, never()).invalidateAllCaches();
        }
    }

    @Test
    @DisplayName("Test updateLevelProject should update level project successfully")
    void testUpdateLevelProjectSuccess() {
        // Given
        String levelId = "level-1";
        ADLevelProjectUpdateRequest request = new ADLevelProjectUpdateRequest();
        request.setName("Updated Project");
        request.setDescription("Updated description");

        LevelProject existingLevel = new LevelProject();
        existingLevel.setId(levelId);
        existingLevel.setName("Original Project");
        existingLevel.setCode("ORIGINAL_PROJECT");
        existingLevel.setDescription("Original description");
        existingLevel.setStatus(EntityStatus.ACTIVE);

        try (MockedStatic<CodeGeneratorUtils> mockedStatic = Mockito.mockStatic(CodeGeneratorUtils.class);
                MockedStatic<ValidateHelper> validateHelperMockedStatic = Mockito.mockStatic(ValidateHelper.class)) {
            mockedStatic.when(() -> CodeGeneratorUtils.generateCodeFromString("Updated Project"))
                    .thenReturn("UPDATED_PROJECT");
            validateHelperMockedStatic.when(() -> ValidateHelper.isValidCode("Updated Project"))
                    .thenReturn(true);

            when(repository.findById(levelId)).thenReturn(Optional.of(existingLevel));
            when(repository.isExistsLevelProject("UPDATED_PROJECT", levelId)).thenReturn(false);
            when(repository.save(any(LevelProject.class))).thenReturn(existingLevel);

            // When
            ResponseEntity<?> response = levelProjectService.updateLevelProject(levelId, request);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ApiResponse apiResponse = (ApiResponse) response.getBody();
            assertNotNull(apiResponse);
            assertEquals("Cập nhật nhóm dự án thành công", apiResponse.getMessage());

            // Verify repository was called
            verify(repository).save(existingLevel);
            verify(userActivityLogHelper).saveLog("Vừa cập nhật nhóm dự án Updated Project");
            verify(redisInvalidationHelper).invalidateAllCaches();
        }
    }

    @Test
    @DisplayName("Test updateLevelProject should return error if level project not found")
    void testUpdateLevelProjectNotFound() {
        // Given
        String levelId = "non-existent-id";
        ADLevelProjectUpdateRequest request = new ADLevelProjectUpdateRequest();
        request.setName("Updated Project");
        request.setDescription("Updated description");

        when(repository.findById(levelId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = levelProjectService.updateLevelProject(levelId, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Nhóm dự án không tồn tại", apiResponse.getMessage());

        // Verify repository was not called to save
        verify(repository, never()).save(any(LevelProject.class));
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test detailLevelProject should return level project if found")
    void testDetailLevelProjectFound() {
        // Given
        String levelId = "level-1";
        LevelProject foundLevel = new LevelProject();
        foundLevel.setId(levelId);
        when(repository.findById(levelId)).thenReturn(Optional.of(foundLevel));

        // When
        ResponseEntity<?> response = levelProjectService.detailLevelProject(levelId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Hiện thị chi tiết nhóm dự án thành công", apiResponse.getMessage());
        assertEquals(foundLevel, apiResponse.getData());
        verify(repository).findById(levelId);
    }

    @Test
    @DisplayName("Test detailLevelProject should return error if not found")
    void testDetailLevelProjectNotFound() {
        // Given
        String levelId = "not-exist";
        when(repository.findById(levelId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = levelProjectService.detailLevelProject(levelId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Nhóm dự án không tồn tại", apiResponse.getMessage());
        verify(repository).findById(levelId);
    }

    @Test
    @DisplayName("Test changeStatus should toggle level project status successfully")
    void testChangeStatusSuccess() {
        // Given
        String levelId = "level-1";
        LevelProject levelProject = new LevelProject();
        levelProject.setId(levelId);
        levelProject.setName("Test Level");
        levelProject.setStatus(EntityStatus.ACTIVE);
        when(repository.findById(levelId)).thenReturn(Optional.of(levelProject));
        when(repository.save(any(LevelProject.class))).thenReturn(levelProject);

        // When
        ResponseEntity<?> response = levelProjectService.changeStatus(levelId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Thay đổi trạng thái nhóm dự án thành công", apiResponse.getMessage());
        verify(repository).save(levelProject);
        verify(userActivityLogHelper).saveLog(contains("vừa thay đổi trạng thái nhóm dự án"));
        verify(redisInvalidationHelper).invalidateAllCaches();
    }

    @Test
    @DisplayName("Test changeStatus should return error if level project not found")
    void testChangeStatusNotFound() {
        // Given
        String levelId = "non-existent-id";

        when(repository.findById(levelId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = levelProjectService.changeStatus(levelId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Nhóm dự án không tồn tại", apiResponse.getMessage());

        // Verify repository was not called to save
        verify(repository, never()).save(any(LevelProject.class));
        verify(redisInvalidationHelper, never()).invalidateAllCaches();
    }
}