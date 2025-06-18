package udpm.hn.studentattendance.core.staff.statistics.services;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.staff.statistics.model.response.SSPlanDateStudentFactoryResponse;
import udpm.hn.studentattendance.core.staff.statistics.repositories.SSFactoryRepository;
import udpm.hn.studentattendance.core.staff.statistics.repositories.SSUserAdminRepository;
import udpm.hn.studentattendance.core.staff.statistics.repositories.SSSemesterRepository;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.helpers.MailerHelper;
import udpm.hn.studentattendance.infrastructure.excel.model.dto.ExStudentModel;
import udpm.hn.studentattendance.utils.DateTimeUtils;
import udpm.hn.studentattendance.utils.ExcelUtils;
import udpm.hn.studentattendance.infrastructure.config.mailer.model.MailerDefaultRequest;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.AttendanceStatus;
import udpm.hn.studentattendance.repositories.FacilityRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AutomaticStatisticsEmailService {

    private final SSUserAdminRepository ssUserAdminRepository;
    private final SSSemesterRepository ssSemesterRepository;
    private final FacilityRepository facilityRepository;
    private final SSFactoryRepository ssFactoryRepository;
    private final MailerHelper mailerHelper;

    @Value("${app.config.app-name}")
    private String appName;

    @Value("${app.config.cron.statistics-email:0 8 * * *}")
    private String statisticsEmailCron;

    @Value("${app.config.email.statistics.enabled:true}")
    private boolean statisticsEmailEnabled;

    @Scheduled(cron = "${app.config.cron.statistics-email:0 8 * * *}")
    public void sendDailyStatisticsEmail() {
        if (!statisticsEmailEnabled) {
            return;
        }

        // Lấy học kỳ hiện tại
        Optional<Semester> currentSemester = ssSemesterRepository.findAll().stream()
                .filter(s -> s.getStatus() == EntityStatus.ACTIVE)
                .filter(s -> {
                    long now = System.currentTimeMillis();
                    return s.getFromDate() <= now && s.getToDate() >= now;
                })
                .findFirst();

        if (currentSemester.isEmpty()) {
            return;
        }

        // Tính toán thời gian cho ngày hôm qua
        LocalDate yesterday = LocalDate.now().minusDays(1);
        long startOfYesterday = yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endOfYesterday = yesterday.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        // Lấy danh sách email của tất cả admin
        List<String> adminEmails = ssUserAdminRepository.getAllAdminEmails();
        if (adminEmails.isEmpty()) {
            return;
        }

        // Lấy danh sách tất cả cơ sở đang hoạt động
        List<Facility> facilities = facilityRepository.findAll().stream()
                .filter(f -> f.getStatus() == EntityStatus.ACTIVE)
                .toList();

        // Gửi email cho admin đầu tiên với BCC cho các admin khác
        String toEmail = adminEmails.remove(0);

        for (Facility facility : facilities) {
            byte[] file = createFileStatisticsForFacility(
                    currentSemester.get().getId(),
                    startOfYesterday,
                    endOfYesterday,
                    facility.getId());

            if (file == null) {
                continue;
            }

            MailerDefaultRequest mailerRequest = new MailerDefaultRequest();
            mailerRequest.setTo(toEmail);
            if (!adminEmails.isEmpty()) {
                mailerRequest.setBcc(adminEmails.toArray(new String[0]));
            }

            mailerRequest.setTemplate(null);
            mailerRequest.setTitle("Báo cáo thống kê cơ sở " + facility.getName() + " - Ngày " +
                    DateTimeUtils.convertMillisToDate(startOfYesterday) + " - " + appName);

            Map<String, Object> dataMail = new HashMap<>();
            dataMail.put("FROM_DATE", DateTimeUtils.convertMillisToDate(startOfYesterday));
            dataMail.put("TO_DATE", DateTimeUtils.convertMillisToDate(endOfYesterday));

            Map<String, byte[]> filesMail = new HashMap<>();
            filesMail.put(facility.getCode() + "__" +
                    DateTimeUtils.convertMillisToDate(startOfYesterday, "dd-MM-yyyy") + "_" +
                    DateTimeUtils.convertMillisToDate(endOfYesterday, "dd-MM-yyyy") + "__report.xlsx", file);

            mailerRequest.setAttachments(filesMail);
            mailerRequest.setContent(MailerHelper.loadTemplate(MailerHelper.TEMPLATE_STATISTICS_STAFF, dataMail));

            // Gửi email bất đồng bộ
            mailerHelper.send(mailerRequest);
        }
    }

    private byte[] createFileStatisticsForFacility(String idSemester, Long startDate, Long endDate, String facilityId) {
        List<Factory> lstFactory = ssFactoryRepository.getAllFactoryBySemester(idSemester, facilityId);

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream data = new ByteArrayOutputStream()) {

            for (Factory factory : lstFactory) {
                List<SSPlanDateStudentFactoryResponse> lstData = ssFactoryRepository
                        .getAllPlanDateAttendanceByIdFactory(
                                factory.getId(),
                                DateTimeUtils.toStartOfDay(startDate),
                                DateTimeUtils.toEndOfDay(endDate));
                if (lstData.isEmpty()) {
                    continue;
                }

                List<String> headers = new ArrayList<>(List.of("Mã sinh viên", "Họ tên sinh viên"));

                Set<String> stPlanDate = lstData.stream()
                        .map(this::buildCellPlanDate)
                        .collect(Collectors.toSet());
                List<String> lstPlanDate = stPlanDate.stream()
                        .sorted(Comparator.comparing(s -> {
                            String datePart = s.split(" - ")[0];
                            return LocalDate.parse(datePart,
                                    DateTimeFormatter.ofPattern(DateTimeUtils.DATE_FORMAT.replace('/', '-')));
                        }))
                        .toList();

                Set<ExStudentModel> stPStudent = lstData.stream()
                        .map(o -> new ExStudentModel(o.getCode(), o.getName()))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                List<ExStudentModel> lstStudent = stPStudent.stream()
                        .toList();

                headers.addAll(lstPlanDate);
                headers.add("Tổng");
                headers.add("Vắng(%)");
                headers.add("Điểm danh bù(lần)");

                Sheet sheet = ExcelUtils.createTemplate(workbook, factory.getName(), headers, new ArrayList<>());

                Map<Object, String> colorMap = new HashMap<>();
                colorMap.put("Có mặt", "#a9d08e");
                colorMap.put("Có mặt (bù)", "#ffd966");
                colorMap.put("Vắng mặt", "#ff7d7d");

                int row = 1;
                for (ExStudentModel student : lstStudent) {
                    String studentCode = student.getCode();
                    String studentName = student.getName();

                    List<Object> dataCell = new ArrayList<>();
                    dataCell.add(studentCode);
                    dataCell.add(studentName);

                    double total_absent = 0;
                    int total_recovery = 0;
                    for (String namePlanDate : lstPlanDate) {
                        SSPlanDateStudentFactoryResponse planDate = lstData.stream()
                                .filter(s -> s.getCode().equals(studentCode)
                                        && buildCellPlanDate(s).equals(namePlanDate))
                                .findFirst()
                                .orElse(null);
                        if (planDate == null || planDate.getStartDate() > DateTimeUtils.getCurrentTimeMillis()) {
                            dataCell.add(" - ");
                            continue;
                        }
                        if (planDate.getStatus() == AttendanceStatus.PRESENT.ordinal()) {
                            if (planDate.getLateCheckin() != null && planDate.getLateCheckin() > 0 ||
                                    planDate.getLateCheckout() != null && planDate.getLateCheckout() > 0) {
                                total_recovery++;
                                total_absent += 0.5;
                                dataCell.add("Có mặt (bù)");
                            } else {
                                dataCell.add("Có mặt");
                            }
                        } else {
                            total_absent++;
                            dataCell.add("Vắng mặt");
                        }
                    }

                    dataCell.add(total_absent + "/" + lstPlanDate.size());
                    dataCell.add(Math.round(total_absent / lstPlanDate.size() * 1000) / 10.0 + "%");
                    dataCell.add(total_recovery);

                    ExcelUtils.insertRow(sheet, row, dataCell, colorMap);
                    row++;
                }
            }

            workbook.write(data);
            return data.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    private String buildCellPlanDate(SSPlanDateStudentFactoryResponse o) {
        return DateTimeUtils.convertMillisToDate(o.getStartDate(), DateTimeUtils.DATE_FORMAT.replace('/', '-'))
                + " - Ca " + o.getShift();
    }
}