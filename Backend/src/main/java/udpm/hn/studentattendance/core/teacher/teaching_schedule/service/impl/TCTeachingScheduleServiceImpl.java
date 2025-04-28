package udpm.hn.studentattendance.core.teacher.teaching_schedule.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.model.request.TCTSPlanDateUpdateRequest;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.model.request.TCTeachingScheduleRequest;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.model.response.TCTSDetailPlanDateResponse;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.model.response.TCTeachingScheduleResponse;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.repository.TCTSFactoryExtendRepository;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.repository.TCTSProjectExtendRepository;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.repository.TCTSSubjectExtendRepository;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.repository.TCTeachingScheduleExtendRepository;
import udpm.hn.studentattendance.core.teacher.teaching_schedule.service.TCTeachingScheduleService;
import udpm.hn.studentattendance.entities.*;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;
import udpm.hn.studentattendance.infrastructure.constants.ShiftType;
import udpm.hn.studentattendance.infrastructure.constants.StatusType;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Validated
public class TCTeachingScheduleServiceImpl implements TCTeachingScheduleService {

    private final TCTeachingScheduleExtendRepository teacherTeachingScheduleExtendRepository;

    private final TCTSProjectExtendRepository teacherTsProjectExtendRepository;

    private final TCTSSubjectExtendRepository teacherTsSubjectExtendRepository;

    private final TCTSFactoryExtendRepository teacherTsFactoryExtendRepository;

    private final SessionHelper sessionHelper;

    @Override
    public ResponseEntity<?> getAllTeachingScheduleByStaff(
            TCTeachingScheduleRequest teachingScheduleRequest) {
        Pageable pageable = PaginationHelper.createPageable(teachingScheduleRequest);
        PageableObject list = PageableObject
                .of(teacherTeachingScheduleExtendRepository.getAllTeachingScheduleByStaff(
                        sessionHelper.getUserId(), pageable, teachingScheduleRequest));
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả lịch dạy của " + sessionHelper.getUserId() + "thành công",
                        list),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllFactoryByStaff() {
        List<Factory> factories = teacherTsFactoryExtendRepository
                .getAllFactoryByStaff(sessionHelper.getUserId(), EntityStatus.ACTIVE);
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả nhóm xửng của " + sessionHelper.getUserId()
                                + " dạy thành công",
                        factories),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllProjectByStaff() {
        List<Project> projects = teacherTsProjectExtendRepository.getAllProject(sessionHelper.getUserId(),
                EntityStatus.ACTIVE);
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả dự án đang dạy của " + sessionHelper.getUserId()
                                + " thành công",
                        projects),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllSubjectByStaff() {
        List<Subject> subjects = teacherTsSubjectExtendRepository
                .getAllSubjectByStaff(sessionHelper.getUserId(), EntityStatus.ACTIVE);
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả môn học của " + sessionHelper.getUserId() + " thành công",
                        subjects),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllType() {
        List<PlanDate> shifts = teacherTeachingScheduleExtendRepository.getAllType();
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả hình thức học thành công",
                        shifts),
                HttpStatus.OK);
    }

    @Override
    public ByteArrayInputStream exportTeachingSchedule(
            List<TCTeachingScheduleResponse> teachingScheduleResponseList) {
        Document document = new Document();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, byteArrayOutputStream);
            document.open();

            // Nhúng font hỗ trợ tiếng Việt (Arial Unicode MS)
            BaseFont unicodeFont = BaseFont.createFont("font/Arial Unicode.ttf", BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED);
            Font fontHeaders = new Font(unicodeFont, 15, Font.BOLD);
            // Định nghĩa font cho header với màu chữ đen (không dùng màu trắng)
            Font headFont = new Font(unicodeFont, 12, Font.SYMBOL, new Color(239, 235, 235));
            Font cellFont = new Font(unicodeFont, 12);

            // Tiêu đề "Lịch dạy"
            Paragraph paragraph = new Paragraph(
                    "Lịch dạy giảng viên: "
                            + sessionHelper.getUserCode()
                            + " - "
                            + sessionHelper.getUserName(),
                    fontHeaders);
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);
            document.add(Chunk.NEWLINE);

            // Tạo bảng với 7 cột
            PdfPTable pdfTable = new PdfPTable(8);
            pdfTable.setWidthPercentage(100);
            pdfTable.setSpacingBefore(10f);
            pdfTable.setSpacingAfter(10f);
            pdfTable.setWidths(new float[]{30, 20, 20, 20, 30, 50, 25, 30});

            // Header: sử dụng màu cam đậm từ ảnh mẫu (ví dụ: RGB 237,125,49)
            Color headerColor = new Color(2, 3, 51);

            // Màu nền so le cho các dòng dữ liệu
            Color rowColor1 = new Color(255, 255, 255);
            Color rowColor2 = new Color(245, 245, 245);

            // Thêm header cho bảng
            Stream.of("Ngày dạy", "Ca học", "Điểm danh muộn", "Mã môn", "Xưởng", "Dự án", "Hình thức",
                            "Mô tả")
                    .forEach(headerTitle -> {
                        PdfPCell headerCell = new PdfPCell();
                        headerCell.setBackgroundColor(headerColor);
                        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        headerCell.setBorderWidth(1);
                        headerCell.setPadding(8);
                        headerCell.setPhrase(new Phrase(headerTitle, headFont));
                        pdfTable.addCell(headerCell);
                    });

            // Định dạng ngày dạy
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE - dd/MM/yyyy HH:mm",
                    new Locale("vi", "VN"));

            int rowIndex = 0;
            for (TCTeachingScheduleResponse teachingScheduleResponse : teachingScheduleResponseList) {
                Color backgroundColor = (rowIndex % 2 == 0) ? rowColor1 : rowColor2;

                // Cột "Ngày dạy"
                String formattedTeachingDay = dateFormat
                        .format(new Date(teachingScheduleResponse.getStartTeaching()));
                PdfPCell teachingDayCell = new PdfPCell(new Phrase(formattedTeachingDay, cellFont));
                styleCell(teachingDayCell, backgroundColor);
                pdfTable.addCell(teachingDayCell);

                // Cột "Ca học"
                PdfPCell shiftCell = new PdfPCell(new Phrase(
                        String.valueOf(teachingScheduleResponse.getShift()), cellFont));
                styleCell(shiftCell, backgroundColor);
                pdfTable.addCell(shiftCell);

                // Cột "Điểm danh muộn tối đa (phút)"
                PdfPCell lateArrivalCell = new PdfPCell(new Phrase(
                        String.valueOf(teachingScheduleResponse.getLateArrival() + " phút"), cellFont));
                styleCell(lateArrivalCell, backgroundColor);
                pdfTable.addCell(lateArrivalCell);

                // Cột "Mã môn"
                PdfPCell subjectCell = new PdfPCell(new Phrase(
                        String.valueOf(teachingScheduleResponse.getSubjectCode()), cellFont));
                styleCell(subjectCell, backgroundColor);
                pdfTable.addCell(subjectCell);

                // Cột "Xưởng"
                PdfPCell factoryCell = new PdfPCell(new Phrase(
                        String.valueOf(teachingScheduleResponse.getFactoryName()), cellFont));
                styleCell(factoryCell, backgroundColor);
                pdfTable.addCell(factoryCell);

                // Cột "Dự án"
                PdfPCell projectCell = new PdfPCell(new Phrase(
                        String.valueOf(teachingScheduleResponse.getProjectName()), cellFont));
                styleCell(projectCell, backgroundColor);
                pdfTable.addCell(projectCell);

                // Cột "Dự án"
                PdfPCell typeCell = new PdfPCell(new Phrase(
                        String.valueOf(teachingScheduleResponse.getType() == 0 ? "Offline" : "Online"), cellFont));
                styleCell(typeCell, backgroundColor);
                pdfTable.addCell(typeCell);
                // Cột "Mô tả"
                PdfPCell descriptionCell = new PdfPCell(new Phrase(
                        teachingScheduleResponse.getDescription() != null
                                ? teachingScheduleResponse.getDescription()
                                : "",
                        cellFont));
                styleCell(descriptionCell, backgroundColor);
                pdfTable.addCell(descriptionCell);

                rowIndex++;
            }

            document.add(pdfTable);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    /**
     * Hàm tiện ích để style cho từng cell:
     * - Set nền (background)
     * - Canh giữa nội dung
     * - Padding và viền
     */
    private void styleCell(PdfPCell cell, Color backgroundColor) {
        cell.setBackgroundColor(backgroundColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderWidth(1);
        cell.setPadding(8);
        cell.setBorderColor(new Color(200, 200, 200));
    }

    @Override
    public ResponseEntity<?> updatePlanDate(TCTSPlanDateUpdateRequest planDateUpdateRequest) {
        Optional<PlanDate> existPlanDate = teacherTeachingScheduleExtendRepository
                .findById(planDateUpdateRequest.getIdPlanDate());
        boolean isOutOfTime = teacherTeachingScheduleExtendRepository.isOutOfTime(existPlanDate.get().getId());
        if (isOutOfTime) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.ERROR,
                            "Đã quá giờ cập nhật ngày dạy",
                            isOutOfTime),
                    HttpStatus.BAD_REQUEST);
        }
        if (existPlanDate.isPresent()) {
            PlanDate planDate = existPlanDate.get();
            planDate.setDescription(planDateUpdateRequest.getDescription());
            planDate.setLateArrival(planDateUpdateRequest.getLateArrival());
            planDate.setLink(planDateUpdateRequest.getLink());
            teacherTeachingScheduleExtendRepository.save(planDate);
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Cập nhật thông tin buổi học thành công",
                            planDate),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Kế hoạch không tồn tại",
                        null),
                HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> getDetailPlanDate(String planDateId) {
        Optional<TCTSDetailPlanDateResponse> getDetailPlanDateResponse = teacherTeachingScheduleExtendRepository
                .getPlanDateById(planDateId);
        if (getDetailPlanDateResponse.isPresent()) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Lấy chi tiết kế hoạch thành công",
                            getDetailPlanDateResponse),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Kế hoạch không tồn tại",
                        null),
                HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> getAllTeachingSchedulePresent(
            TCTeachingScheduleRequest teachingScheduleRequest) {
        Pageable pageable = PaginationHelper.createPageable(teachingScheduleRequest);
        PageableObject list = PageableObject.of(teacherTeachingScheduleExtendRepository
                .getAllTeachingSchedulePresent(sessionHelper.getUserId(), pageable,
                        teachingScheduleRequest));
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy lịch dạy hôm nay của giảng viên " + sessionHelper.getUserId()
                                + " thành công",
                        list),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> changeTypePlanDate(String planDateId) {
        Optional<PlanDate> planDate = teacherTeachingScheduleExtendRepository.findById(planDateId);
        if (planDate.isPresent()) {
            PlanDate updatePlanDate = planDate.get();
            updatePlanDate.setType(updatePlanDate.getType() == ShiftType.ONLINE ? ShiftType.OFFLINE : ShiftType.ONLINE);
            updatePlanDate.setRequiredIp(updatePlanDate.getRequiredIp() == StatusType.DISABLE ? StatusType.ENABLE : StatusType.DISABLE);
            updatePlanDate.setRequiredLocation(updatePlanDate.getRequiredLocation() == StatusType.DISABLE ? StatusType.ENABLE : StatusType.DISABLE);
            teacherTeachingScheduleExtendRepository.save(updatePlanDate);
            return new ResponseEntity<>(
                    new ApiResponse(
                            RestApiStatus.SUCCESS,
                            "Lấy chi tiết kế hoạch thành công",
                            updatePlanDate),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.ERROR,
                        "Kế hoạch không tồn tại",
                        null),
                HttpStatus.BAD_REQUEST);
    }


}
