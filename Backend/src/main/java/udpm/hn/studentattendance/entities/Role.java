package udpm.hn.studentattendance.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Nationalized;
import udpm.hn.studentattendance.entities.base.PrimaryEntity;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;

import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "role")
@DynamicUpdate
public class Role extends PrimaryEntity implements Serializable {

    @Column(name = "code")
    @Nationalized
    private RoleConstant code;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "id_facility")
    private Facility facility;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "id_user_staff")
    private UserStaff userStaff;

}
