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
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "facility_location")
@DynamicUpdate
public class FacilityLocation extends PrimaryEntity implements Serializable {

    @Column(name = "name", length = EntityProperties.LENGTH_NAME)
    private String name;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "radius")
    private Integer radius;

    @ManyToOne
    @JoinColumn(name = "id_facility", nullable = false)
    @JsonIgnore
    private Facility facility;

}
