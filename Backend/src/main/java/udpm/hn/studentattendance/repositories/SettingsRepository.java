package udpm.hn.studentattendance.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.entities.Settings;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;

@Repository
public interface SettingsRepository extends JpaRepository<Settings, SettingKeys> {

}
