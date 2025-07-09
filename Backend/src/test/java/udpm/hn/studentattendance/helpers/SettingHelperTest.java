package udpm.hn.studentattendance.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import udpm.hn.studentattendance.entities.Settings;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonSettingsRepository;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettingHelperTest {

    @Mock
    private CommonSettingsRepository repository;

    @InjectMocks
    private SettingHelper settingHelper;

    @BeforeEach
    void setUp() {
        // Setup lenient stubbing to avoid unnecessary stubbing warnings
        lenient().when(repository.findAll()).thenReturn(Arrays.asList());
    }

    @Test
    void canInstantiate() {
        CommonSettingsRepository mockRepo = Mockito.mock(CommonSettingsRepository.class);
        SettingHelper helper = new SettingHelper(mockRepo);
        assertThat(helper).isNotNull();
    }

    @Test
    void testGetAllSettings() {
        // Given
        Settings setting1 = new Settings();
        setting1.setKey(SettingKeys.SHIFT_MIN_DIFF);
        setting1.setValue("30");

        Settings setting2 = new Settings();
        setting2.setKey(SettingKeys.FACE_THRESHOLD_CHECKIN);
        setting2.setValue("0.8");

        List<Settings> settings = Arrays.asList(setting1, setting2);
        when(repository.findAll()).thenReturn(settings);

        // When
        Map<SettingKeys, Object> result = settingHelper.getAllSettings();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(30, result.get(SettingKeys.SHIFT_MIN_DIFF));
        assertEquals(0.8, result.get(SettingKeys.FACE_THRESHOLD_CHECKIN));
        verify(repository).findAll();
    }

    @Test
    void testGetAllSettingsWithEmptyList() {
        // Given
        when(repository.findAll()).thenReturn(Arrays.asList());

        // When
        Map<SettingKeys, Object> result = settingHelper.getAllSettings();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).findAll();
    }

    @Test
    void testGetSetting() {
        // Given
        String value = "25";
        when(repository.getOneByKey(SettingKeys.SHIFT_MIN_DIFF)).thenReturn(value);

        // When
        Object result = settingHelper.getSetting(SettingKeys.SHIFT_MIN_DIFF);

        // Then
        assertEquals(25, result);
        verify(repository).getOneByKey(SettingKeys.SHIFT_MIN_DIFF);
    }

    @Test
    void testGetSettingWithNullValue() {
        // Given
        when(repository.getOneByKey(SettingKeys.SHIFT_MIN_DIFF)).thenReturn(null);

        // When
        Object result = settingHelper.getSetting(SettingKeys.SHIFT_MIN_DIFF);

        // Then
        assertNull(result);
        verify(repository).getOneByKey(SettingKeys.SHIFT_MIN_DIFF);
    }

    @Test
    void testGetSettingWithType() {
        // Given
        String value = "0.9";
        when(repository.getOneByKey(SettingKeys.FACE_THRESHOLD_CHECKIN)).thenReturn(value);

        // When
        Double result = settingHelper.getSetting(SettingKeys.FACE_THRESHOLD_CHECKIN, Double.class);

        // Then
        assertEquals(0.9, result);
        verify(repository).getOneByKey(SettingKeys.FACE_THRESHOLD_CHECKIN);
    }

    @Test
    void testGetSettingWithStringType() {
        // Given
        String value = "test value";
        when(repository.getOneByKey(SettingKeys.SHIFT_MIN_DIFF)).thenReturn(value);

        // When
        String result = settingHelper.getSetting(SettingKeys.SHIFT_MIN_DIFF, String.class);

        // Then
        assertEquals("test value", result);
        verify(repository).getOneByKey(SettingKeys.SHIFT_MIN_DIFF);
    }

    @Test
    void testSaveNewSetting() {
        // Given
        SettingKeys key = SettingKeys.SHIFT_MIN_DIFF;
        String value = "45";
        Settings savedSetting = new Settings();
        savedSetting.setKey(key);
        savedSetting.setValue(value);

        when(repository.findById(key)).thenReturn(Optional.empty());
        when(repository.save(any(Settings.class))).thenReturn(savedSetting);

        // When
        Settings result = settingHelper.save(key, value);

        // Then
        assertNotNull(result);
        assertEquals(key, result.getKey());
        assertEquals(value, result.getValue());
        verify(repository).findById(key);
        verify(repository).save(any(Settings.class));
    }

    @Test
    void testSaveExistingSetting() {
        // Given
        SettingKeys key = SettingKeys.SHIFT_MIN_DIFF;
        String value = "60";
        Settings existingSetting = new Settings();
        existingSetting.setKey(key);
        existingSetting.setValue("30");

        Settings updatedSetting = new Settings();
        updatedSetting.setKey(key);
        updatedSetting.setValue(value);

        when(repository.findById(key)).thenReturn(Optional.of(existingSetting));
        when(repository.save(any(Settings.class))).thenReturn(updatedSetting);

        // When
        Settings result = settingHelper.save(key, value);

        // Then
        assertNotNull(result);
        assertEquals(key, result.getKey());
        assertEquals(value, result.getValue());
        verify(repository).findById(key);
        verify(repository).save(any(Settings.class));
    }

    @Test
    void testSaveWithNullValue() {
        // Given
        SettingKeys key = SettingKeys.SHIFT_MIN_DIFF;
        String value = null;
        Settings savedSetting = new Settings();
        savedSetting.setKey(key);
        savedSetting.setValue(null);

        when(repository.findById(key)).thenReturn(Optional.empty());
        when(repository.save(any(Settings.class))).thenReturn(savedSetting);

        // When
        Settings result = settingHelper.save(key, value);

        // Then
        assertNotNull(result);
        assertEquals(key, result.getKey());
        assertNull(result.getValue());
        verify(repository).findById(key);
        verify(repository).save(any(Settings.class));
    }

    @Test
    void testParseValueWithBooleanTrue() {
        // Given
        String value = "true";

        // When
        Object result = SettingHelper.parseValue(value);

        // Then
        assertEquals(true, result);
    }

    @Test
    void testParseValueWithBooleanFalse() {
        // Given
        String value = "false";

        // When
        Object result = SettingHelper.parseValue(value);

        // Then
        assertEquals(false, result);
    }

    @Test
    void testParseValueWithInteger() {
        // Given
        String value = "42";

        // When
        Object result = SettingHelper.parseValue(value);

        // Then
        assertEquals(42, result);
    }

    @Test
    void testParseValueWithDouble() {
        // Given
        String value = "3.14";

        // When
        Object result = SettingHelper.parseValue(value);

        // Then
        assertEquals(3.14, result);
    }

    @Test
    void testParseValueWithString() {
        // Given
        String value = "hello world";

        // When
        Object result = SettingHelper.parseValue(value);

        // Then
        assertEquals("hello world", result);
    }

    @Test
    void testParseValueWithNull() {
        // Given
        String value = null;

        // When
        Object result = SettingHelper.parseValue(value);

        // Then
        assertNull(result);
    }

    @Test
    void testParseValueWithEmptyString() {
        // Given
        String value = "";

        // When
        Object result = SettingHelper.parseValue(value);

        // Then
        assertEquals("", result);
    }

    @Test
    void testParseValueWithWhitespace() {
        // Given
        String value = "   ";

        // When
        Object result = SettingHelper.parseValue(value);

        // Then
        assertEquals("   ", result);
    }

    @Test
    void testParseValueWithMixedCase() {
        // Given
        String value = "TRUE";

        // When
        Object result = SettingHelper.parseValue(value);

        // Then
        assertEquals(true, result);
    }

    @Test
    void testParseValueWithNegativeInteger() {
        // Given
        String value = "-42";

        // When
        Object result = SettingHelper.parseValue(value);

        // Then
        assertEquals(-42, result);
    }

    @Test
    void testParseValueWithNegativeDouble() {
        // Given
        String value = "-3.14";

        // When
        Object result = SettingHelper.parseValue(value);

        // Then
        assertEquals(-3.14, result);
    }

    @Test
    void testParseValueWithZero() {
        // Given
        String value = "0";

        // When
        Object result = SettingHelper.parseValue(value);

        // Then
        assertEquals(0, result);
    }

    @Test
    void testParseValueWithDecimalZero() {
        // Given
        String value = "0.0";

        // When
        Object result = SettingHelper.parseValue(value);

        // Then
        assertEquals(0.0, result);
    }

    @Test
    void testParseValueWithInvalidNumber() {
        // Given
        String value = "not a number";

        // When
        Object result = SettingHelper.parseValue(value);

        // Then
        assertEquals("not a number", result);
    }

    @Test
    void testParseValueWithSpecialCharacters() {
        // Given
        String value = "!@#$%^&*()";

        // When
        Object result = SettingHelper.parseValue(value);

        // Then
        assertEquals("!@#$%^&*()", result);
    }
}