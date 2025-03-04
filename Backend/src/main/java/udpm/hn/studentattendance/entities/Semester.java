package udpm.hn.studentattendance.entities;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.ManyToAny;
import org.hibernate.annotations.Nationalized;
import udpm.hn.studentattendance.entities.base.PrimaryEntity;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;
import udpm.hn.studentattendance.infrastructure.constants.SemesterName;

import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "semester")
@DynamicUpdate
public class Semester extends PrimaryEntity implements Serializable {

    @Column(name = "code", length = EntityProperties.LENGTH_NAME)
    @Nationalized
    private String code;

    @Column(name = "name", length = EntityProperties.LENGTH_NAME)
    @Nationalized
    @Enumerated(EnumType.STRING)
    private SemesterName semesterName;

    @Column(name = "from_date")
    private Long fromDate;

    @Column(name = "to_date")
    private Long toDate;
    @Column(name = "year")
    private Integer year;
    @ManyToOne
    @JoinColumn(name = "id_facility")
    private Facility facility;

}
