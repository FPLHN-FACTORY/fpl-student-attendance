package udpm.hn.studentattendance.entities;

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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import udpm.hn.studentattendance.entities.base.PrimaryEntity;

import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "plan_factory")
@DynamicUpdate
@ToString
public class PlanFactory extends PrimaryEntity implements Serializable {

    @ManyToOne
    @JoinColumn(name = "id_plan")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Plan plan;

    @ManyToOne
    @JoinColumn(name = "id_factory")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Factory factory;

}
