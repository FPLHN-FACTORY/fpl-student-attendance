package udpm.hn.studentattendance.core.admin.semester.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Setter
public class AdCreateUpdateSemesterRequest {

    private String semesterId;

    private String facilytiId;

    @NotBlank(message = "Tên học kỳ không được để trống")
    private String semesterName;

    @NotNull(message = "Thời gian không được để trống")
    private Long fromDate;

    @NotNull(message = "Thời gian không được để trống")
    private Long toDate;


    public Long getStartTimeCustom() {
        // Chuyển đổi giá trị từ this.fromDate (epoch milli) thành đối tượng LocalDateTime dựa trên múi giờ mặc định của hệ thống
        LocalDateTime startDateSemester = Instant.ofEpochMilli(this.fromDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        // Chuẩn hóa thời gian của ngày bằng cách đặt giờ, phút, giây và nano về 0 (00:00:00.000)
        startDateSemester = startDateSemester.withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        // Chuyển đổi LocalDateTime đã chuẩn hóa trở lại thành epoch milli và trả về kết quả
        return startDateSemester.atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    public Long getEndTimeCustom() {
        // Chuyển đổi giá trị từ this.toDate (epoch milli) thành đối tượng LocalDateTime dựa trên múi giờ mặc định của hệ thống
        LocalDateTime endDateSemester = Instant.ofEpochMilli(this.toDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        // Chuẩn hóa thời gian của ngày bằng cách đặt giờ là 23, phút 59, giây 59 và nano 0 (23:59:59.000)
        endDateSemester = endDateSemester.withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(0);

        // Chuyển đổi LocalDateTime đã chuẩn hóa trở lại thành epoch milli và trả về kết quả
        return endDateSemester.atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }
    // ví dụ chọn ngày 1/4/2024 -> 1/8/2024
    // thì sẽ bắt đầu 0:00 ngày 1/4 đến ngày 23:59 ngày 1/8

}
