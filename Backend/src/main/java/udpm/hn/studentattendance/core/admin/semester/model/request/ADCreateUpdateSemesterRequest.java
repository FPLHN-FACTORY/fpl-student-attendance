package udpm.hn.studentattendance.core.admin.semester.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import udpm.hn.studentattendance.infrastructure.constants.EntityProperties;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Setter
public class ADCreateUpdateSemesterRequest {

        private String semesterId;

        private String facilityId;

        @NotBlank(message = "Tên học kỳ không được để trống")
        @Size(min = 2, max = EntityProperties.LENGTH_NAME, message = "Tên học kỳ phải có ít nhất 2 ký tự và không được quá:"
                        + EntityProperties.LENGTH_NAME)
        private String semesterName;

        @NotNull(message = "Thời gian không được để trống")
        private Long fromDate;

        @NotNull(message = "Thời gian không được để trống")
        private Long toDate;

        public Long getStartTimeCustom() {
                // Chuyển đổi giá trị từ this.fromDate (epoch milli) thành đối tượng
                // LocalDateTime dựa trên múi giờ mặc định của hệ thống
                LocalDateTime startDateSemester = Instant.ofEpochMilli(this.fromDate)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime();

                // Chuẩn hóa thời gian của ngày bằng cách đặt giờ, phút, giây và nano về 0
                // (00:00:00.000)
                startDateSemester = startDateSemester.withHour(0)
                                .withMinute(0)
                                .withSecond(0)
                                .withNano(0);

                // Chuyển đổi LocalDateTime đã chuẩn hóa trở lại thành epoch milli và trả về kết
                // quả
                return startDateSemester.atZone(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli();
        }

        public Long getEndTimeCustom() {
                // Chuyển đổi giá trị từ this.toDate (epoch milli) thành đối tượng LocalDateTime
                // dựa trên múi giờ mặc định của hệ thống
                LocalDateTime endDateSemester = Instant.ofEpochMilli(this.toDate)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime();

                // Chuẩn hóa thời gian của ngày bằng cách đặt giờ là 23, phút 59, giây 59 và
                // nano 0 (23:59:59.000)
                endDateSemester = endDateSemester.withHour(23)
                                .withMinute(59)
                                .withSecond(59)
                                .withNano(0);

                // Chuyển đổi LocalDateTime đã chuẩn hóa trở lại thành epoch milli và trả về kết
                // quả
                return endDateSemester.atZone(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli();
        }

}
