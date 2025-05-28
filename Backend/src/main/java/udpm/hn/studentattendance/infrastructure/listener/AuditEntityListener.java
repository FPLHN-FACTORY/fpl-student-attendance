package udpm.hn.studentattendance.infrastructure.listener;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import udpm.hn.studentattendance.entities.base.AuditEntity;

import java.util.Calendar;

public class AuditEntityListener {

    @PrePersist
    private void onCreate(AuditEntity entity) {
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(getCurrentTime());
        }
        if (entity.getUpdatedAt() == null) {
            entity.setUpdatedAt(getCurrentTime());
        }
    }

    @PreUpdate
    private void onUpdate(AuditEntity entity) {
        entity.setUpdatedAt(getCurrentTime());
    }

    private long getCurrentTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

}
