package udpm.hn.studentattendance.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import udpm.hn.studentattendance.entities.Settings;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonSettingsRepository;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettingHelperTest {

    @Mock
    private CommonSettingsRepository repository;

    @InjectMocks
    private SettingHelper settingHelper;

    private Settings testSetting1;
    private Settings testSetting2;

    @BeforeEach
    void setUp() {
        testSetting1 = new Settings();
        testSetting1.setKey(SettingKeys.FACE_THRESHOLD_CHECKIN);
        testSetting1.setValue("0.8");

        testSetting2 = new Settings();
        testSetting2.setKey(SettingKeys.FACE_THRESHOLD_REGISTER);
        testSetting2.setValue("0.9");
    }

    @Test
    void getAllSettings_shouldReturnAllSettings() {
        // Arrange
        List<Settings> settings = Arrays.asList(testSetting1, testSetting2);
        when(repository.findAll()).thenReturn(settings);

        // Act
        Map<SettingKeys, Object> result = settingHelper.getAllSettings();

        // Assert
        assertEquals(2, result.size());
        assertEquals(0.8, result.get(SettingKeys.FACE_THRESHOLD_CHECKIN));
        assertEquals(0.9, result.get(SettingKeys.FACE_THRESHOLD_REGISTER));
        verify(repository).findAll();
    }

    @Test
    void getSetting_shouldReturnCorrectSetting() {
        // Arrange
        when(repository.getOneByKey(SettingKeys.FACE_THRESHOLD_CHECKIN)).thenReturn("0.8");

        // Act
        Object result = settingHelper.getSetting(SettingKeys.FACE_THRESHOLD_CHECKIN);

        // Assert
        assertEquals(0.8, result);
        verify(repository).getOneByKey(SettingKeys.FACE_THRESHOLD_CHECKIN);
    }

    @Test
    void getSettingWithType_shouldReturnCorrectTypedSetting() {
        // Arrange
        when(repository.getOneByKey(SettingKeys.FACE_THRESHOLD_CHECKIN)).thenReturn("0.8");

        // Act
        Double result = settingHelper.getSetting(SettingKeys.FACE_THRESHOLD_CHECKIN, Double.class);

        // Assert
        assertEquals(0.8, result);
        verify(repository).getOneByKey(SettingKeys.FACE_THRESHOLD_CHECKIN);
    }

    @Test
    void save_shouldCreateNewSettingIfNotExists() {
        // Arrange
        when(repository.findById(SettingKeys.FACE_THRESHOLD_CHECKIN)).thenReturn(Optional.empty());
        when(repository.save(any(Settings.class))).thenReturn(testSetting1);

        // Act
        Settings result = settingHelper.save(SettingKeys.FACE_THRESHOLD_CHECKIN, "0.8");

        // Assert
        assertEquals(SettingKeys.FACE_THRESHOLD_CHECKIN, result.getKey());
        assertEquals("0.8", result.getValue());
        verify(repository).findById(SettingKeys.FACE_THRESHOLD_CHECKIN);
        verify(repository).save(any(Settings.class));
    }

    @Test
    void save_shouldUpdateExistingSetting() {
        // Arrange
        when(repository.findById(SettingKeys.FACE_THRESHOLD_CHECKIN)).thenReturn(Optional.of(testSetting1));
        when(repository.save(any(Settings.class))).thenReturn(testSetting1);

        // Act
        Settings result = settingHelper.save(SettingKeys.FACE_THRESHOLD_CHECKIN, "0.85");

        // Assert
        assertEquals(SettingKeys.FACE_THRESHOLD_CHECKIN, result.getKey());
        assertEquals("0.85", result.getValue());
        verify(repository).findById(SettingKeys.FACE_THRESHOLD_CHECKIN);
        verify(repository).save(any(Settings.class));
    }

    @Test
    void parseValue_shouldParseBoolean() {
        assertEquals(true, SettingHelper.parseValue("true"));
        assertEquals(false, SettingHelper.parseValue("false"));
    }

    @Test
    void parseValue_shouldParseInteger() {
        assertEquals(42, SettingHelper.parseValue("42"));
    }

    @Test
    void parseValue_shouldParseDouble() {
        assertEquals(3.14, SettingHelper.parseValue("3.14"));
    }

    @Test
    void parseValue_shouldReturnOriginalStringIfNotParseable() {
        assertEquals("hello", SettingHelper.parseValue("hello"));
    }

    @Test
    void parseValue_shouldReturnNullForNullInput() {
        assertNull(SettingHelper.parseValue(null));
    }
}