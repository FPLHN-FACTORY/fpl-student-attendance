package udpm.hn.studentattendance.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import udpm.hn.studentattendance.entities.Settings;
import udpm.hn.studentattendance.infrastructure.config.TestDatabaseConfig;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.context.annotation.Import;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
class SettingsRepositoryTest {

    @Autowired
    private SettingsRepository settingsRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        Settings settings = new Settings();
        settings.setKey(SettingKeys.SHIFT_MAX_LATE_ARRIVAL);
        settings.setValue("5");

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

        // When
        settingsRepository.save(settings1);
        settingsRepository.save(settings2);
        List<Settings> allSettings = settingsRepository.findAll();

        // Then
        assertTrue(allSettings.size() >= 2);
        assertTrue(allSettings.stream().anyMatch(s -> SettingKeys.SHIFT_MAX_LATE_ARRIVAL.equals(s.getKey())));
        assertTrue(allSettings.stream().anyMatch(s -> SettingKeys.SHIFT_MIN_DIFF.equals(s.getKey())));
    }

    @Test
    void testUpdateSettings() {
        // Given
        Settings settings = new Settings();
        settings.setKey(SettingKeys.SHIFT_MAX_LATE_ARRIVAL);
        settings.setValue("5");

        Settings savedSettings = settingsRepository.save(settings);

        // When
        savedSettings.setValue("10");
        Settings updatedSettings = settingsRepository.save(savedSettings);

        // Then
        assertEquals("10", updatedSettings.getValue());
    }

    @Test
    void testDeleteSettings() {
        // Given
        Settings settings = new Settings();
        settings.setKey(SettingKeys.ATTENDANCE_EARLY_CHECKIN);
        settings.setValue("delete");

        Settings savedSettings = settingsRepository.save(settings);
        SettingKeys settingsKey = savedSettings.getKey();

        // When
        settingsRepository.deleteById(settingsKey);
        Optional<Settings> deletedSettings = settingsRepository.findById(settingsKey);

        // Then
        assertFalse(deletedSettings.isPresent());
    }
}