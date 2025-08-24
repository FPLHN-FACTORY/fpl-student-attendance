package udpm.hn.studentattendance.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import udpm.hn.studentattendance.entities.base.PrimaryEntity;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;

import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_activity_log")
@DynamicUpdate
public class UserActivityLog extends PrimaryEntity implements Serializable {

    @Column(name = "id_user", length = EntityProperties.LENGTH_CODE)
    private String idUser;

    @Column(name = "role")
    private RoleConstant role;

    @Column(name = "message", length = EntityProperties.LENGTH_TEXT)
    private String message;

    @ManyToOne
    @JoinColumn(name = "id_facility")
    private Facility facility;

}
