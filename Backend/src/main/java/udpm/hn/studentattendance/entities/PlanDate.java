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
import org.hibernate.annotations.DynamicUpdate;
import udpm.hn.studentattendance.entities.base.PrimaryEntity;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "plan_date")
@DynamicUpdate
public class PlanDate extends PrimaryEntity implements Serializable {

    @Column(name = "description", length = EntityProperties.LENGTH_TEXT)
    private String description;

    @Column(name = "start_date")
    private Long startDate;

    @Column(name = "shift")
    private Integer shift;

    @Column(name = "late_arrival")
    private Integer lateArrival;

    @ManyToOne
    @JoinColumn(name = "id_plan_factory")
    private PlanFactory planFactory;

}
