package udpm.hn.studentattendance.core.staff.statistics.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.staff.statistics.model.dto.SSAllStatsAndChartDto;
import udpm.hn.studentattendance.core.staff.statistics.model.dto.SSListUserDto;
import udpm.hn.studentattendance.core.staff.statistics.model.request.SSFilterFactoryStatsRequest;
import udpm.hn.studentattendance.core.staff.statistics.model.request.SSSendMailStatsRequest;
import udpm.hn.studentattendance.core.staff.statistics.model.response.*;
import udpm.hn.studentattendance.core.staff.statistics.repositories.*;
import udpm.hn.studentattendance.core.staff.statistics.services.SSStatisticsService;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.Factory;
import udpm.hn.studentattendance.entities.Semester;
import udpm.hn.studentattendance.helpers.MailerHelper;
import udpm.hn.studentattendance.helpers.PaginationHelper;
import udpm.hn.studentattendance.helpers.RouterHelper;
import udpm.hn.studentattendance.helpers.SessionHelper;
import udpm.hn.studentattendance.infrastructure.common.PageableObject;
import udpm.hn.studentattendance.infrastructure.config.mailer.model.MailerDefaultRequest;
import udpm.hn.studentattendance.infrastructure.constants.AttendanceStatus;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.excel.model.dto.EXStudentModel;
import udpm.hn.studentattendance.repositories.FacilityRepository;
import udpm.hn.studentattendance.utils.DateTimeUtils;
import udpm.hn.studentattendance.utils.ExcelUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SSStatisticsServiceImpl implements SSStatisticsService {

    private final SSSemesterRepository ssSemesterRepository;

    private final SSLevelProjectRepository ssLevelProjectRepository;

    private final SSSubjectFacilityRepository sSSubjectFacilityRepository;

    private final SSFactoryRepository ssFactoryRepository;

    private final SSUserAdminRepository ssUserAdminRepository;

    private final SSUserStaffRepository ssUserStaffRepository;

    private final FacilityRepository facilityRepository;

    private final SessionHelper sessionHelper;

    private final MailerHelper mailerHelper;

    @Value("${app.config.app-name}")
    private String appName;

    @Override
    public ResponseEntity<?> getAllStats(String idSemester) {
        SSAllStatsResponse stats = ssSemesterRepository.getAllStats(idSemester, sessionHelper.getFacilityId())
                .orElse(null);
        if (stats == null) {
            return RouterHelper.responseError("Không thể lấy dữ liệu thống kê");
        }

        List<SSChartLevelProjectResponse> levelProjectStats = ssLevelProjectRepository.getStats(idSemester,
                sessionHelper.getFacilityId());
        List<SSChartSubjectFacilityResponse> subjectFacilityResponses = sSSubjectFacilityRepository.getStats(idSemester,
                sessionHelper.getFacilityId());

        SSAllStatsAndChartDto data = new SSAllStatsAndChartDto();
        data.setStats(stats);
        data.setLevelStats(levelProjectStats);
        data.setSubjectFacilityStats(subjectFacilityResponses);

        return RouterHelper.responseSuccess("Lấy dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> getListStatsFactory(SSFilterFactoryStatsRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<SSFactoryStatsResponse> data = PageableObject
                .of(ssFactoryRepository.getAllByFilter(pageable, request));
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> getListUser(String idSemester) {
        List<SSUserResponse> lstAdmin = ssUserAdminRepository.getAllList(sessionHelper.getUserEmail());
        List<SSUserResponse> lstStaff = ssUserStaffRepository.getAllListStaff(sessionHelper.getFacilityId(),
                sessionHelper.getUserEmail());
        List<SSUserResponse> lstTeacher = ssUserStaffRepository.getAllListTeacher(sessionHelper.getFacilityId(),
                idSemester, sessionHelper.getUserEmail());

        SSListUserDto data = new SSListUserDto();
        data.setAdmin(lstAdmin);
        data.setStaff(lstStaff);
        data.setTeacher(lstTeacher);
        return RouterHelper.responseSuccess("Lấy dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> sendMailStats(SSSendMailStatsRequest request) {
        Semester semester = ssSemesterRepository.findById(request.getIdSemester()).orElse(null);
        if (semester == null || semester.getStatus() != EntityStatus.ACTIVE) {
            return RouterHelper.responseError("Không tìm thấy học kỳ");
        }

        Facility facility = facilityRepository.findById(sessionHelper.getFacilityId()).orElse(null);
        if (facility == null || facility.getStatus() != EntityStatus.ACTIVE) {
            return RouterHelper.responseError("Cơ sở không tồn tại hoặc đã ngừng hoạt động");
        }

        Long startDate = request.getRangeDate().get(0);
        Long endDate = request.getRangeDate().get(1);
        if (startDate < semester.getFromDate() || endDate > semester.getToDate()) {
            return RouterHelper.responseError("Khoảng ngày thống kê phải nằm trong học kỳ");
        }

        byte[] file = createFileStatistics(semester.getId(), startDate, endDate);

        if (file == null) {
            return RouterHelper.responseError("Không thể tạo tệp tin thống kê");
        }

        Set<String> lstEmails = Stream.of(request.getEmailAdmin(), request.getEmailStaff(), request.getEmailTeacher())
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        String to = sessionHelper.getUserEmail();

        MailerDefaultRequest mailerDefaultRequest = new MailerDefaultRequest();
        mailerDefaultRequest.setTemplate(null);
        mailerDefaultRequest.setTo(to);
        mailerDefaultRequest.setTitle("Báo cáo thống kê cơ sở " + facility.getName() + " - " + appName);

        if (!lstEmails.isEmpty()) {
            mailerDefaultRequest.setBcc(lstEmails.toArray(new String[0]));
        }

        Map<String, Object> dataMail = new HashMap<>();
        dataMail.put("STAFF_NAME", sessionHelper.getUserCode() + " - " + sessionHelper.getUserName());
        dataMail.put("FROM_DATE", DateTimeUtils.convertMillisToDate(startDate));
        dataMail.put("TO_DATE", DateTimeUtils.convertMillisToDate(endDate));

        Map<String, byte[]> filesMail = new HashMap<>();
        filesMail.put(facility.getCode() + "__" + DateTimeUtils.convertMillisToDate(startDate, "dd-MM-yyyy") + "_"
                + DateTimeUtils.convertMillisToDate(endDate, "dd-MM-yyyy") + "__report.xlsx", file);

        mailerDefaultRequest.setAttachments(filesMail);
        mailerDefaultRequest.setContent(MailerHelper.loadTemplate(MailerHelper.TEMPLATE_STATISTICS_STAFF, dataMail));
        mailerHelper.send(mailerDefaultRequest);

        return RouterHelper.responseSuccess("Gửi báo cáo thống kê thành công");
    }

    private byte[] createFileStatistics(String idSemester, Long startDate, Long endDate) {

        List<Factory> lstFactory = ssFactoryRepository.getAllFactoryBySemester(idSemester,
                sessionHelper.getFacilityId());

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream data = new ByteArrayOutputStream()) {

            for (Factory factory : lstFactory) {
                List<SSPlanDateStudentFactoryResponse> lstData = ssFactoryRepository
                        .getAllPlanDateAttendanceByIdFactory(factory.getId(), DateTimeUtils.toStartOfDay(startDate),
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

                Set<EXStudentModel> stPStudent = lstData.stream()
                        .map(o -> new EXStudentModel(o.getCode(), o.getName()))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                List<EXStudentModel> lstStudent = stPStudent.stream()
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
                for (EXStudentModel student : lstStudent) {
                    String studentCode = student.getCode();
                    String studentName = student.getName();

                    List<Object> dataCell = new ArrayList<>();
                    dataCell.add(studentCode);
                    dataCell.add(studentName);

                    double total_absent = 0;
                    int total_recovery = 0;
                    for (String namePlanDate : lstPlanDate) {
                        SSPlanDateStudentFactoryResponse planDate = lstData.stream().filter(
                                s -> s.getCode().equals(studentCode) && buildCellPlanDate(s).equals(namePlanDate))
                                .findFirst().orElse(null);
                        if (planDate == null || planDate.getStartDate() > DateTimeUtils.getCurrentTimeMillis()) {
                            dataCell.add(" - ");
                            continue;
                        }
                        if (planDate.getStatus() == AttendanceStatus.PRESENT.ordinal()) {
                            if (planDate.getLateCheckin() != null && planDate.getLateCheckin() > 0
                                    || planDate.getLateCheckout() != null && planDate.getLateCheckout() > 0) {
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
                    dataCell.add(
                            (total_absent > 0 ? Math.round(total_absent / lstPlanDate.size() * 1000) / 10.0 : 0) + "%");
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
