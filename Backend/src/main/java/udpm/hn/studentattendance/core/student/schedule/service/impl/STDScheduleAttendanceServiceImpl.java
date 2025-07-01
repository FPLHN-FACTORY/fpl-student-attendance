package udpm.hn.studentattendance.core.student.schedule.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import udpm.hn.studentattendance.core.student.schedule.model.request.STDScheduleAttendanceSearchRequest;
import udpm.hn.studentattendance.core.student.schedule.model.response.STDScheduleAttendanceResponse;
import udpm.hn.studentattendance.core.student.schedule.repository.STDScheduleAttendanceRepository;
import udpm.hn.studentattendance.core.student.schedule.service.STDScheduleAttendanceService;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.constants.RedisPrefixConstant;
import udpm.hn.studentattendance.infrastructure.redis.service.RedisService;

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
public class STDScheduleAttendanceServiceImpl implements STDScheduleAttendanceService {

        private final STDScheduleAttendanceRepository repository;

        private final SessionHelper sessionHelper;

        private final RedisService redisService;

        @Value("${spring.cache.redis.time-to-live}")
        private long redisTTL;

        public PageableObject<?> getCachedScheduleList(STDScheduleAttendanceSearchRequest request) {
                String cacheKey = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_STUDENT + "list_"
                                + sessionHelper.getUserId() + "_"
                                + request.toString();

                Object cachedData = redisService.get(cacheKey);
                if (cachedData != null) {
                        try {
                                return redisService.getObject(cacheKey, PageableObject.class);
                        } catch (Exception e) {
                                redisService.delete(cacheKey);
                        }
                }

                Pageable pageable = PaginationHelper.createPageable(request, "id");
                PageableObject<?> result = PageableObject.of(repository.getAllListAttendanceByUser(pageable, request));

                try {
                        redisService.set(cacheKey, result, redisTTL);
                } catch (Exception ignored) {
                }

                return result;
        }

        @Override
        public ResponseEntity<?> getList(STDScheduleAttendanceSearchRequest request) {
                PageableObject<?> result = getCachedScheduleList(request);
                return RouterHelper.responseSuccess("Lấy danh sách điểm danh thành công", result);
        }

        @Override
        public ByteArrayInputStream exportScheduleAttendance(
                        List<STDScheduleAttendanceResponse> scheduleAttendanceResponses) {
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
                                        "Lịch học của sinh viên: "
                                                        + sessionHelper.getUserCode()
                                                        + " - "
                                                        + sessionHelper.getUserName(),
                                        fontHeaders);
                        paragraph.setAlignment(Element.ALIGN_CENTER);
                        document.add(paragraph);
                        document.add(Chunk.NEWLINE);

                        // Tạo bảng với 7 cột
                        PdfPTable pdfTable = new PdfPTable(8);
                        pdfTable.setWidthPercentage(110);
                        pdfTable.setSpacingBefore(10f);
                        pdfTable.setSpacingAfter(10f);
                        pdfTable.setWidths(new float[] { 20, 40, 20, 20, 30, 30, 40, 20 });

                        // Header: sử dụng màu cam đậm từ ảnh mẫu (ví dụ: RGB 237,125,49)
                        Color headerColor = new Color(2, 3, 51);

                        // Màu nền so le cho các dòng dữ liệu
                        Color rowColor1 = new Color(255, 255, 255);
                        Color rowColor2 = new Color(245, 245, 245);

                        // Thêm header cho bảng
                        Stream.of("Bài học", "Ngày học", "Ca học", "Nhóm xưởng", "Link học", "Địa điểm",
                                        "Tên giảng viên", "Mô tả")
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
                        for (STDScheduleAttendanceResponse scheduleAttendanceResponse : scheduleAttendanceResponses) {
                                Color backgroundColor = (rowIndex % 2 == 0) ? rowColor1 : rowColor2;

                                PdfPCell rowNumberCell = new PdfPCell(
                                                new Phrase(String.valueOf(scheduleAttendanceResponse.getIndexs()),
                                                                cellFont));
                                styleCell(rowNumberCell, backgroundColor);
                                pdfTable.addCell(rowNumberCell);

                                String attendanceDayStart = dateFormat
                                                .format(new Date(scheduleAttendanceResponse.getAttendanceDayStart()));
                                PdfPCell learningDayCell = new PdfPCell(new Phrase(attendanceDayStart, cellFont));
                                styleCell(learningDayCell, backgroundColor);
                                pdfTable.addCell(learningDayCell);

                                PdfPCell shiftCell = new PdfPCell(
                                                new Phrase(String.valueOf(scheduleAttendanceResponse.getShift()),
                                                                cellFont));
                                styleCell(shiftCell, backgroundColor);
                                pdfTable.addCell(shiftCell);

                                // Cột "Điểm danh muộn tối đa (phút)"
                                PdfPCell factoryNameCell = new PdfPCell(
                                                new Phrase(String.valueOf(scheduleAttendanceResponse.getFactoryName()),
                                                                cellFont));
                                styleCell(factoryNameCell, backgroundColor);
                                pdfTable.addCell(factoryNameCell);

                                PdfPCell linkNameCell = new PdfPCell(
                                                new Phrase(String.valueOf(scheduleAttendanceResponse.getLink() != null
                                                                ? scheduleAttendanceResponse.getLink()
                                                                : ""), cellFont));
                                styleCell(linkNameCell, backgroundColor);
                                pdfTable.addCell(linkNameCell);

                                PdfPCell roomNameCell = new PdfPCell(
                                                new Phrase(String.valueOf(
                                                                scheduleAttendanceResponse.getLocation() != null
                                                                                ? scheduleAttendanceResponse
                                                                                                .getLocation()
                                                                                : ""),
                                                                cellFont));
                                styleCell(roomNameCell, backgroundColor);
                                pdfTable.addCell(roomNameCell);

                                PdfPCell staffNameCell = new PdfPCell(
                                                new Phrase(String.valueOf(scheduleAttendanceResponse.getStaffName()),
                                                                cellFont));
                                styleCell(staffNameCell, backgroundColor);
                                pdfTable.addCell(staffNameCell);
                                // Cột "Mô tả"
                                PdfPCell descriptionCell = new PdfPCell(new Phrase(
                                                scheduleAttendanceResponse.getDescription() != null
                                                                ? scheduleAttendanceResponse.getDescription()
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

        /**
         * Xóa cache lịch học của một user
         */
        public void invalidateScheduleCache(String userId) {
                String cachePattern = RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_STUDENT + "list_" + userId + "_*";
                redisService.deletePattern(cachePattern);
        }

        /**
         * Xóa tất cả các cache lịch học
         */
        public void invalidateAllScheduleCaches() {
                redisService.deletePattern(RedisPrefixConstant.REDIS_PREFIX_SCHEDULE_STUDENT + "list_*");
        }
}
