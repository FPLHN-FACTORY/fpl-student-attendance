package udpm.hn.studentattendance.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
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

import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "facility_shift")
@DynamicUpdate
public class FacilityShift extends PrimaryEntity implements Serializable {

    @Column(name = "shift")
    private int shift;

    @Column(name = "from_hour")
    private int fromHour;

    @Column(name = "from_minute")
    private int fromMinute;

    @Column(name = "to_hour")
    private int toHour;

    @Column(name = "to_minute")
    private int toMinute;

    @ManyToOne
    @JoinColumn(name = "id_facility")
    @JsonIgnore
    private Facility facility;

}
