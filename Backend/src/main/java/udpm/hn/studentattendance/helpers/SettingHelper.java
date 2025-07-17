package udpm.hn.studentattendance.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import udpm.hn.studentattendance.entities.Settings;
import udpm.hn.studentattendance.infrastructure.common.repositories.CommonSettingsRepository;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SettingHelper {

    private final CommonSettingsRepository repository;

    public Map<SettingKeys, Object> getAllSettings() {
        Map<SettingKeys, Object> results = new HashMap<>();
        List<Settings> lstSettings = repository.findAll();
        for (Settings s: lstSettings) {
            results.put(s.getKey(), parseValue(s.getValue()));
        }
        return results;
    }

    public Object getSetting(SettingKeys key) {
        return parseValue(repository.getOneByKey(key));
    }

    @SuppressWarnings("unchecked")
    public <T> T getSetting(SettingKeys key, Class<T> type) {
        return (T) getSetting(key);
    }

    public Settings save(SettingKeys key, String value) {
        Settings setting = repository.findById(key).orElse(null);
        if (setting == null) {
            setting = new Settings();
            setting.setKey(key);
        }
        setting.setValue(value);
        return repository.save(setting);
    }

    public static Object parseValue(String value) {
        if (value == null) {
            return null;
        }

        String val = value.trim().toLowerCase();

        if (val.equals("true") || val.equals("false")) {
            return Boolean.parseBoolean(val);
        }

        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException ignored) {}

        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException ignored) {}

        return value;
    }

}
