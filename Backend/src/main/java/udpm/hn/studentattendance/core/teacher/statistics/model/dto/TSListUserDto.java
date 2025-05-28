package udpm.hn.studentattendance.core.teacher.statistics.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.core.teacher.statistics.model.response.TSUserResponse;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TSListUserDto implements Serializable {

    private List<TSUserResponse> admin;

    private List<TSUserResponse> staff;

}
