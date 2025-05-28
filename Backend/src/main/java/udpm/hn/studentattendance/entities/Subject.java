package udpm.hn.studentattendance.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Nationalized;
import udpm.hn.studentattendance.entities.base.PrimaryEntity;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "subject")
@DynamicUpdate
@ToString
public class Subject extends PrimaryEntity implements Serializable {

    @Column(name = "code", length = EntityProperties.LENGTH_NAME)
    @Nationalized
    private String code;

    @Column(name = "name", length = EntityProperties.LENGTH_NAME)
    private String name;

}
