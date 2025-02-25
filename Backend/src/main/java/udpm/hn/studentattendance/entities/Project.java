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
import org.hibernate.annotations.Nationalized;
import udpm.hn.studentattendance.entities.base.PrimaryEntity;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "project")
@DynamicUpdate
public class Project extends PrimaryEntity implements Serializable {

    @Column(name = "name", length = EntityProperties.LENGTH_NAME)
    private String name;

    @Column(name = "description", length = EntityProperties.LENGTH_TEXT)
    private String description;

    @ManyToOne
    @JoinColumn(name = "id_level_project")
    private LevelProject levelProject;

    @ManyToOne
    @JoinColumn(name = "id_subject_facility")
    private SubjectFacility subjectFacility;

    @ManyToOne
    @JoinColumn(name = "id_semester")
    private Semester semester;

}
