package udpm.hn.studentattendance.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import udpm.hn.studentattendance.entities.base.PrimaryEntity;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "import_log")
@DynamicUpdate
public class ImportLog extends PrimaryEntity implements Serializable {

    @Column(name = "id_user", length = EntityProperties.LENGTH_CODE)
    private String idUser;

    @Column(name = "code", length = EntityProperties.LENGTH_NAME)
    private String code;

    @Column(name = "file_name", length = EntityProperties.LENGTH_NAME)
    private String fileName;

    @Column(name = "type")
    private Integer type;

    @OneToMany(mappedBy = "importLog")
    private List<ImportLogDetail> importLogDetails;

    @ManyToOne
    @JoinColumn(name = "id_facility")
    private Facility facility;

}
