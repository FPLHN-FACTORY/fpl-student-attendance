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
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import udpm.hn.studentattendance.entities.base.PrimaryEntity;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "import_log_detail")
@DynamicUpdate
public class ImportLogDetail extends PrimaryEntity implements Serializable {

    @Column(name = "line")
    private Integer line;

    @Column(name = "message", length = EntityProperties.LENGTH_TEXT)
    private String message;

    @ManyToOne
    @JoinColumn(name = "id_import_log", nullable = false)
    private ImportLog importLog;

}
