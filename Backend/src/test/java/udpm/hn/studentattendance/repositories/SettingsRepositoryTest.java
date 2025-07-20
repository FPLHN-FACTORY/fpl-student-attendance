package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import udpm.hn.studentattendance.entities.Settings;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettingsRepositoryTest {

    @Mock
    private SettingsRepository settingsRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        Settings settings = new Settings();
        settings.setKey(SettingKeys.SHIFT_MAX_LATE_ARRIVAL);
        settings.setValue("5");

        // Mock behavior
        when(settingsRepository.save(any(Settings.class))).thenReturn(settings);
        when(settingsRepository.findById(SettingKeys.SHIFT_MAX_LATE_ARRIVAL)).thenReturn(Optional.of(settings));

        // When
        Settings savedSettings = settingsRepository.save(settings);
        Optional<Settings> foundSettings = settingsRepository.findById(SettingKeys.SHIFT_MAX_LATE_ARRIVAL);

        // Then
        assertTrue(foundSettings.isPresent());
        assertEquals(SettingKeys.SHIFT_MAX_LATE_ARRIVAL, foundSettings.get().getKey());
        assertEquals("5", foundSettings.get().getValue());
    }

    @Test
    void testSaveAndFindAll() {
        // Given
        Settings settings1 = new Settings();
        settings1.setKey(SettingKeys.SHIFT_MAX_LATE_ARRIVAL);
        settings1.setValue("5");

        Settings settings2 = new Settings();
        settings2.setKey(SettingKeys.SHIFT_MIN_DIFF);
        settings2.setValue("30");

        List<Settings> settingsList = new ArrayList<>();
        settingsList.add(settings1);
        settingsList.add(settings2);

        // Mock behavior
        when(settingsRepository.save(any(Settings.class))).thenReturn(settings1).thenReturn(settings2);
        when(settingsRepository.findAll()).thenReturn(settingsList);

        // When
        Settings saved1 = settingsRepository.save(settings1);
        Settings saved2 = settingsRepository.save(settings2);
        List<Settings> allSettings = settingsRepository.findAll();

        // Then
        assertEquals(2, allSettings.size());
        assertTrue(allSettings.stream().anyMatch(s -> SettingKeys.SHIFT_MAX_LATE_ARRIVAL.equals(s.getKey())));
        assertTrue(allSettings.stream().anyMatch(s -> SettingKeys.SHIFT_MIN_DIFF.equals(s.getKey())));
    }

    @Test
    void testUpdateSettings() {
        // Given
        Settings settings = new Settings();
        settings.setKey(SettingKeys.SHIFT_MAX_LATE_ARRIVAL);
        settings.setValue("5");

        Settings updatedSettings = new Settings();
        updatedSettings.setKey(SettingKeys.SHIFT_MAX_LATE_ARRIVAL);
        updatedSettings.setValue("10");

        // Mock behavior
        when(settingsRepository.save(any(Settings.class))).thenReturn(settings).thenReturn(updatedSettings);

        // When
        Settings savedSettings = settingsRepository.save(settings);
        savedSettings.setValue("10");
        Settings result = settingsRepository.save(savedSettings);

        // Then
        assertEquals("10", result.getValue());
    }

    @Test
    void testDeleteSettings() {
        // Given
        Settings settings = new Settings();
        settings.setKey(SettingKeys.ATTENDANCE_EARLY_CHECKIN);
        settings.setValue("delete");

        // Mock behavior
        when(settingsRepository.save(any(Settings.class))).thenReturn(settings);
        doNothing().when(settingsRepository).deleteById(any());
        when(settingsRepository.findById(any())).thenReturn(Optional.empty());

        // When
        Settings savedSettings = settingsRepository.save(settings);
        SettingKeys settingsKey = savedSettings.getKey();
        settingsRepository.deleteById(settingsKey);
        Optional<Settings> deletedSettings = settingsRepository.findById(settingsKey);

        // Then
        assertFalse(deletedSettings.isPresent());
    }
}
