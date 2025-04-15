package udpm.hn.studentattendance.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import udpm.hn.studentattendance.entities.base.PrimaryEntity;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "notification")
@DynamicUpdate
public class Notification extends PrimaryEntity implements Serializable {

    @Column(name = "id_user", length = EntityProperties.LENGTH_CODE)
    private String idUser;

    @Column(name = "type")
    private Integer type;

    @Column(name = "data", length = EntityProperties.LENGTH_TEXT)
    private String data;

}
