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
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import udpm.hn.studentattendance.entities.base.PrimaryEntity;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;
import udpm.hn.studentattendance.infrastructure.constants.IPType;

import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "facility_ip")
@DynamicUpdate
public class FacilityIP extends PrimaryEntity implements Serializable {

    @Column(name = "type")
    private IPType type;

    @Column(name = "ip", length = EntityProperties.LENGTH_NAME)
    private String ip;

    @ManyToOne
    @JoinColumn(name = "id_facility", nullable = false)
    @JsonIgnore
    private Facility facility;

}
