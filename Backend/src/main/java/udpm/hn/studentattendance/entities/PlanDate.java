package udpm.hn.studentattendance.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import udpm.hn.studentattendance.entities.base.PrimaryEntity;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;
import udpm.hn.studentattendance.infrastructure.constants.ShiftType;
import udpm.hn.studentattendance.infrastructure.constants.StatusType;
import udpm.hn.studentattendance.infrastructure.converter.ListIntegerConverter;

import java.io.Serializable;
import java.util.List;

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

    @Column(name = "end_date")
    private Long endDate;

    @Column(name = "shift", length = EntityProperties.LENGTH_NAME)
    @Convert(converter = ListIntegerConverter.class)
    private List<Integer> shift;

    @Column(name = "late_arrival")
    private Integer lateArrival;

    @Column(name = "link", length = EntityProperties.LENGTH_TEXT)
    private String link;

    @Column(name = "room", length = EntityProperties.LENGTH_NAME)
    private String room;

    @Column(name = "type")
    private ShiftType type = ShiftType.OFFLINE;

    @Column(name = "required_location")
    private StatusType requiredLocation = StatusType.ENABLE;

    @Column(name = "required_ip")
    private StatusType requiredIp = StatusType.ENABLE;

    @Column(name = "required_checkin")
    private StatusType requiredCheckin = StatusType.ENABLE;

    @Column(name = "required_checkout")
    private StatusType requiredCheckout = StatusType.ENABLE;

    @ManyToOne
    @JoinColumn(name = "id_plan_factory")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PlanFactory planFactory;

}
