package udpm.hn.studentattendance.infrastructure.common.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;
import udpm.hn.studentattendance.repositories.SettingsRepository;


@Repository
public interface CommonSettingsRepository extends SettingsRepository {

    @Query("SELECT s.value FROM Settings s WHERE s.key = :key")
    String getOneByKey(@Param("key") SettingKeys key);

}
