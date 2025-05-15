package udpm.hn.studentattendance.core.teacher.teachingschedule.model.request;

import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.common.PageableRequest;

@Getter
@Setter
public class TCTeachingScheduleRequest extends PageableRequest {

    private String idSubject;

    private String idFactory;

    private String idProject;

    private Integer shift;

    private Long startDate;

    private Long endDate;

    private Integer shiftType;

}
