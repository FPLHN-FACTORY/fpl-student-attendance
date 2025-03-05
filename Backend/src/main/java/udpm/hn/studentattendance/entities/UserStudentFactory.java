package udpm.hn.studentattendance.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import udpm.hn.studentattendance.entities.base.PrimaryEntity;

import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_student_factory")
@DynamicUpdate
public class UserStudentFactory extends PrimaryEntity implements Serializable {

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "id_factory")
    private Factory factory;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "id_user_student")
    private UserStudent userStudent;

}
