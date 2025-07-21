package udpm.hn.studentattendance.core.staff.statistics.services;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import udpm.hn.studentattendance.infrastructure.excel.model.dto.EXStudentModel;
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

    private static final Logger logger = LoggerFactory.getLogger(AutomaticStatisticsEmailService.class);

    private final SSUserAdminRepository ssUserAdminRepository;

    private final SSSemesterRepository ssSemesterRepository;

    private final FacilityRepository facilityRepository;

    private final SSFactoryRepository ssFactoryRepository;

    private final MailerHelper mailerHelper;

    @Value("${app.config.app-name}")
    private String appName;

    @Value("${app.config.email.statistics.enabled:true}")
    private boolean statisticsEmailEnabled;

    @Scheduled(cron = "${app.config.cron.statistics-email}")
    public void sendDailyStatisticsEmail() {
        if (!statisticsEmailEnabled) {
            return;
        }

        Optional<Semester> currentSemester = ssSemesterRepository.findAll().stream()
                .filter(s -> s.getStatus() == EntityStatus.ACTIVE)
                .filter(s -> {
                    long now = System.currentTimeMillis();
                    return s.getFromDate() <= now && s.getToDate() >= now;
                })
                .findFirst();

        if (currentSemester.isEmpty()) {
            logger.info("No active semester found");
            return;
        }

        LocalDate yesterday = LocalDate.now().minusDays(1);
        long startOfYesterday = yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endOfYesterday = yesterday.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        List<String> adminEmails = ssUserAdminRepository.getAllUserAdmin();
        if (adminEmails.isEmpty()) {
            logger.info("No admin emails found");
            return;
        }
        List<Facility> facilities = facilityRepository.findAll().stream()
                .filter(f -> f.getStatus() == EntityStatus.ACTIVE)
                .toList();

        if (facilities.isEmpty()) {
            logger.info("No active facilities found");
            return;
        }

        String toEmail = adminEmails.get(0);
        String[] bccEmails = adminEmails.size() > 1 ?
                adminEmails.subList(1, adminEmails.size()).toArray(new String[0]) :
                new String[0];

        logger.info("Sending daily statistics for " + facilities.size() + " facilities");

        for (Facility facility : facilities) {
            try {
                byte[] file = createFileStatisticsForFacility(
                        currentSemester.get().getId(),
                        startOfYesterday,
                        endOfYesterday,
                        facility.getId());

                if (file == null || file.length == 0) {
                    logger.info("No data found for facility: " + facility.getName());
                    continue;
                }

                MailerDefaultRequest mailerRequest = new MailerDefaultRequest();
                mailerRequest.setTo(toEmail);

                if (bccEmails.length > 0) {
                    mailerRequest.setBcc(bccEmails);
                }

                mailerRequest.setTemplate(null);
                mailerRequest.setTitle("Báo cáo thống kê cơ sở " + facility.getName() + " - Ngày " +
                        DateTimeUtils.convertMillisToDate(startOfYesterday) + " - " + appName);

                Map<String, Object> dataMail = new HashMap<>();
                dataMail.put("SEMESTER", currentSemester.get().getCode());
                dataMail.put("FACILITY_NAME", facility.getName());
                dataMail.put("FROM_DATE", DateTimeUtils.convertMillisToDate(startOfYesterday));
                dataMail.put("TO_DATE", DateTimeUtils.convertMillisToDate(endOfYesterday));

                Map<String, byte[]> filesMail = new HashMap<>();
                String fileName = facility.getCode() + "_" +
                        DateTimeUtils.convertMillisToDate(startOfYesterday, "dd-MM-yyyy") + "_" +
                        DateTimeUtils.convertMillisToDate(endOfYesterday, "dd-MM-yyyy") + "_report.xlsx";

                filesMail.put(fileName, file);

                mailerRequest.setAttachments(filesMail);
                mailerRequest.setContent(MailerHelper.loadTemplate(MailerHelper.TEMPLATE_STATISTICS_STAFF, dataMail));

                mailerHelper.send(mailerRequest);
                logger.info("Sent daily statistics email for facility: " + facility.getName());

            } catch (Exception e) {
                logger.error("Error sending daily statistics email for facility: " + facility.getName());
            }
        }
    }

    private byte[] createFileStatisticsForFacility(String idSemester, Long startDate, Long endDate, String facilityId) {
        List<Factory> lstFactory = ssFactoryRepository.getAllFactoryBySemester(idSemester, facilityId);

        if (lstFactory.isEmpty()) {
            logger.info("No factories found for facility: " + facilityId);
            return null;
        }

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream data = new ByteArrayOutputStream()) {

            boolean hasData = false;

            for (Factory factory : lstFactory) {
                List<SSPlanDateStudentFactoryResponse> lstData = ssFactoryRepository
                        .getAllPlanDateAttendanceByIdFactory(
                                factory.getId(),
                                DateTimeUtils.toStartOfDay(startDate),
                                DateTimeUtils.toEndOfDay(endDate));

                if (lstData.isEmpty()) {
                    logger.info("No attendance data found for factory: " + factory.getName());
                    continue;
                }

                hasData = true;

                List<String> headers = new ArrayList<>(Arrays.asList("Mã sinh viên", "Họ tên sinh viên"));

                Set<String> stPlanDate = lstData.stream()
                        .map(this::buildCellPlanDate)
                        .collect(Collectors.toSet());

                List<String> lstPlanDate = stPlanDate.stream()
                        .sorted(Comparator.comparing(s -> {
                            try {
                                String datePart = s.split(" - ")[0];
                                return LocalDate.parse(datePart,
                                        DateTimeFormatter.ofPattern(DateTimeUtils.DATE_FORMAT.replace('/', '-')));
                            } catch (Exception e) {
                                logger.error("Error parsing date: " + s);
                                return LocalDate.MIN;
                            }
                        }))
                        .collect(Collectors.toList());

                Set<EXStudentModel> stPStudent = lstData.stream()
                        .map(o -> new EXStudentModel(o.getCode(), o.getName()))
                        .collect(Collectors.toCollection(LinkedHashSet::new));

                List<EXStudentModel> lstStudent = new ArrayList<>(stPStudent);

                headers.addAll(lstPlanDate);
                headers.addAll(Arrays.asList("Tổng", "Vắng(%)", "Điểm danh bù(lần)"));

                Sheet sheet = ExcelUtils.createTemplate(workbook, factory.getName(), headers, new ArrayList<>());

                Map<Object, String> colorMap = new HashMap<>();
                colorMap.put("Có mặt", "#a9d08e");
                colorMap.put("Có mặt (bù)", "#ffd966");
                colorMap.put("Vắng mặt", "#ff7d7d");

                int row = 1;
                for (EXStudentModel student : lstStudent) {
                    String studentCode = student.getCode();
                    String studentName = student.getName();

                    List<Object> dataCell = new ArrayList<>();
                    dataCell.add(studentCode);
                    dataCell.add(studentName);

                    double totalAbsent = 0;
                    int totalRecovery = 0;
                    int totalDays = 0;

                    for (String namePlanDate : lstPlanDate) {
                        SSPlanDateStudentFactoryResponse planDate = lstData.stream()
                                .filter(s -> s.getCode().equals(studentCode) &&
                                        buildCellPlanDate(s).equals(namePlanDate))
                                .findFirst()
                                .orElse(null);

                        if (planDate == null) {
                            dataCell.add(" - ");
                            continue;
                        }

                        if (planDate.getStartDate() > System.currentTimeMillis()) {
                            dataCell.add(" - ");
                            continue;
                        }

                        totalDays++;

                        if (planDate.getStatus() == AttendanceStatus.PRESENT.ordinal()) {
                            boolean isLate = (planDate.getLateCheckin() != null && planDate.getLateCheckin() > 0) ||
                                    (planDate.getLateCheckout() != null && planDate.getLateCheckout() > 0);

                            if (isLate) {
                                totalRecovery++;
                                totalAbsent += 0.5;
                                dataCell.add("Có mặt (bù)");
                            } else {
                                dataCell.add("Có mặt");
                            }
                        } else {
                            totalAbsent++;
                            dataCell.add("Vắng mặt");
                        }
                    }

                    if (totalDays > 0) {
                        dataCell.add(String.format("%.1f/%d", totalAbsent, totalDays));
                        dataCell.add(String.format("%.1f%%", (totalAbsent / totalDays) * 100));
                    } else {
                        dataCell.add("0/0");
                        dataCell.add("0%");
                    }
                    dataCell.add(totalRecovery);

                    ExcelUtils.insertRow(sheet, row, dataCell, colorMap);
                    row++;
                }
            }

            if (!hasData) {
                logger.info("No data found for any factory in facility: " + facilityId);
                return null;
            }

            workbook.write(data);
            logger.info("Created Excel file with size: " + data.size() + " bytes");
            return data.toByteArray();

        } catch (IOException e) {
            logger.error("Error creating Excel file for facility: " + facilityId);
            return null;
        }
    }

    private String buildCellPlanDate(SSPlanDateStudentFactoryResponse o) {
        try {
            return DateTimeUtils.convertMillisToDate(o.getStartDate(), DateTimeUtils.DATE_FORMAT.replace('/', '-'))
                    + " - Ca " + o.getShift();
        } catch (Exception e) {
            logger.error("Error building cell plan date for response: " + o);
            return "Invalid Date - Ca " + o.getShift();
        }
    }
}