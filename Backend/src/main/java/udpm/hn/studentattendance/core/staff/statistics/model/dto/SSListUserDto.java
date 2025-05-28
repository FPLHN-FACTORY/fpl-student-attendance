package udpm.hn.studentattendance.core.staff.statistics.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import udpm.hn.studentattendance.core.staff.statistics.model.response.SSUserResponse;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SSListUserDto implements Serializable {

    private List<SSUserResponse> admin;

    private List<SSUserResponse> staff;

    private List<SSUserResponse> teacher;

}
