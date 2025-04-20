package udpm.hn.studentattendance.infrastructure.listener;

import jakarta.persistence.PrePersist;
import udpm.hn.studentattendance.entities.base.PrimaryEntity;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;

import java.util.UUID;

public class PrimaryEntityListener {

    @PrePersist
    private void onCreate(PrimaryEntity entity) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID().toString());
        }
        if (entity.getStatus() == null) {
            entity.setStatus(EntityStatus.ACTIVE);
        }
    }

}
