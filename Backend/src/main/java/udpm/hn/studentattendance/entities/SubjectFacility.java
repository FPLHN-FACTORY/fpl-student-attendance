package udpm.hn.studentattendance.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "subject_facility")
@DynamicUpdate
public class SubjectFacility extends PrimaryEntity implements Serializable {

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "id_facility")
    private Facility facility;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "id_subject")
    private Subject subject;

}
