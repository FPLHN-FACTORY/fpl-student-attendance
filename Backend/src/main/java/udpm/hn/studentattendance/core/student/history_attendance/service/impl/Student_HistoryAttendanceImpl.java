package udpm.hn.studentattendance.core.student.history_attendance.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.student.history_attendance.model.request.Student_HistoryAttendanceRequest;
import udpm.hn.studentattendance.core.student.history_attendance.model.response.Student_HistoryAttendanceResponse;
import udpm.hn.studentattendance.core.student.history_attendance.repository.Student_HistoryAttendanceExtendRepository;
import udpm.hn.studentattendance.core.student.history_attendance.repository.Student_HistoryAttendanceFactoryExtendRepository;
import udpm.hn.studentattendance.core.student.history_attendance.repository.Student_HistoryAttendanceSemesterExtendRepository;
import udpm.hn.studentattendance.core.student.history_attendance.service.Student_HistoryAttendanceService;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.ApiResponse;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.RestApiStatus;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Validated
public class Student_HistoryAttendanceImpl implements Student_HistoryAttendanceService {
    private final Student_HistoryAttendanceExtendRepository historyAttendanceExtendRepository;

    private final SessionHelper sessionHelper;

    private final Student_HistoryAttendanceSemesterExtendRepository historyAttendanceSemesterExtendRepository;

    private final Student_HistoryAttendanceFactoryExtendRepository historyAttendanceFactoryExtendRepository;

    @Override
    public ResponseEntity<?> getAllHistoryAttendanceByStudent(
            Student_HistoryAttendanceRequest historyAttendanceRequest) {
        Pageable pageable = PaginationHelper.createPageable(historyAttendanceRequest, "createdAt");
        PageableObject list = PageableObject.of(historyAttendanceExtendRepository
                .getAllFactoryAttendance(sessionHelper.getCurrentUser().getId(), pageable, historyAttendanceRequest));
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả lịch sử điểm danh của sinh viên " + sessionHelper.getCurrentUser().getCode()
                                + " thành công",
                        list),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllSemester() {
        List<Semester> semesters = historyAttendanceSemesterExtendRepository.getAllSemesterByCode(EntityStatus.ACTIVE);
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả học kỳ thành công",
                        semesters),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllFactoryByUserStudent() {
        List<Factory> factories = historyAttendanceFactoryExtendRepository.getAllFactoryByUser(EntityStatus.ACTIVE,
                sessionHelper.getUserId());
        return new ResponseEntity<>(
                new ApiResponse(
                        RestApiStatus.SUCCESS,
                        "Lấy tất cả nhóm xưởng của sinh viên " + sessionHelper.getUserCode() + " thành công",
                        factories),
                HttpStatus.OK);
    }

    @Override
    public ByteArrayInputStream exportHistoryAttendance(List<Student_HistoryAttendanceResponse> attendanceResponses,
            String factoryName) {
        Document document = new Document();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, byteArrayOutputStream);
            document.open();

            // Nhúng font hỗ trợ tiếng Việt (Arial Unicode MS)
            BaseFont unicodeFont = BaseFont.createFont("font/Arial Unicode.ttf", BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED);
            Font fontHeaders = new Font(unicodeFont, 15, Font.BOLD);
            Font headFont = new Font(unicodeFont, 12, Font.SYMBOL, new Color(239, 235, 235));
            Font cellFont = new Font(unicodeFont, 12);

            Paragraph paragraph = new Paragraph(
                    "Lịch sử điểm danh nhóm " + factoryName + " của sinh viên: "
                            + sessionHelper.getUserCode()
                            + " - "
                            + sessionHelper.getUserName(),
                    fontHeaders);
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);
            document.add(Chunk.NEWLINE);

            // Tạo bảng với 7 cột
            PdfPTable pdfTable = new PdfPTable(6);
            pdfTable.setWidthPercentage(100);
            pdfTable.setSpacingBefore(10f);
            pdfTable.setSpacingAfter(10f);
            pdfTable.setWidths(new float[] { 20, 40, 20, 20, 30, 30 });

            // Header: sử dụng màu cam đậm từ ảnh mẫu (ví dụ: RGB 237,125,49)
            Color headerColor = new Color(2, 3, 51);

            // Màu nền so le cho các dòng dữ liệu
            Color rowColor1 = new Color(255, 255, 255);
            Color rowColor2 = new Color(245, 245, 245);

            // Thêm header cho bảng
            Stream.of("Bài học", "Ngày học", "Ca học", "Điểm danh muộn tối đa (phút)", "Nội dung", "Trạng thái đi học")
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
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE - dd/MM/yyyy HH:mm", new Locale("vi", "VN"));

            int rowIndex = 0;
            for (Student_HistoryAttendanceResponse attendanceResponse : attendanceResponses) {
                Color backgroundColor = (rowIndex % 2 == 0) ? rowColor1 : rowColor2;

                PdfPCell rowNumberCell = new PdfPCell(
                        new Phrase(String.valueOf(attendanceResponse.getRowNumber()), cellFont));
                styleCell(rowNumberCell, backgroundColor);
                pdfTable.addCell(rowNumberCell);

                String learningDay = dateFormat.format(new Date(attendanceResponse.getPlanDateStartDate()));
                PdfPCell learningDayCell = new PdfPCell(new Phrase(learningDay, cellFont));
                styleCell(learningDayCell, backgroundColor);
                pdfTable.addCell(learningDayCell);

                PdfPCell shiftCell = new PdfPCell(
                        new Phrase(String.valueOf(attendanceResponse.getPlanDateShift()), cellFont));
                styleCell(shiftCell, backgroundColor);
                pdfTable.addCell(shiftCell);

                // Cột "Điểm danh muộn tối đa (phút)"
                PdfPCell lateArrivalCell = new PdfPCell(
                        new Phrase(String.valueOf(attendanceResponse.getLateArrival()), cellFont));
                styleCell(lateArrivalCell, backgroundColor);
                pdfTable.addCell(lateArrivalCell);

                // Cột "Mô tả"
                PdfPCell descriptionCell = new PdfPCell(new Phrase(
                        attendanceResponse.getPlanDateDescription() != null
                                ? attendanceResponse.getPlanDateDescription()
                                : "",
                        cellFont));

                PdfPCell statusAttendanceCell = new PdfPCell(
                        new Phrase(String.valueOf(attendanceResponse.getStatusAttendance()), cellFont));
                styleCell(statusAttendanceCell, backgroundColor);
                pdfTable.addCell(statusAttendanceCell);

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
}
