package udpm.hn.studentattendance.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;
import udpm.hn.studentattendance.infrastructure.constants.SettingKeys;

import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "settings")
@DynamicUpdate
public class Settings implements Serializable {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(length = EntityProperties.LENGTH_ID, updatable = false)
    private SettingKeys key;

    @Column(name = "value", length = EntityProperties.LENGTH_TEXT)
    private String value;

}
