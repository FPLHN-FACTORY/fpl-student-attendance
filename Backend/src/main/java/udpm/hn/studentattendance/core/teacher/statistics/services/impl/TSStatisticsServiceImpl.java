package udpm.hn.studentattendance.core.teacher.statistics.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import udpm.hn.studentattendance.core.teacher.statistics.model.dto.TSAllStatsAndChartDto;
import udpm.hn.studentattendance.core.teacher.statistics.model.dto.TSListUserDto;
import udpm.hn.studentattendance.core.teacher.statistics.model.request.TSFilterFactoryStatsRequest;
import udpm.hn.studentattendance.core.teacher.statistics.model.request.TSSendMailStatsRequest;
import udpm.hn.studentattendance.core.teacher.statistics.model.response.TSAllStatsResponse;
import udpm.hn.studentattendance.core.teacher.statistics.model.response.TSChartLevelProjectResponse;
import udpm.hn.studentattendance.core.teacher.statistics.model.response.TSChartSubjectFacilityResponse;
import udpm.hn.studentattendance.core.teacher.statistics.model.response.TSFactoryStatsResponse;
import udpm.hn.studentattendance.core.teacher.statistics.model.response.TSPlanDateStudentFactoryResponse;
import udpm.hn.studentattendance.core.teacher.statistics.model.response.TSUserResponse;
import udpm.hn.studentattendance.core.teacher.statistics.repositories.TSFactoryRepository;
import udpm.hn.studentattendance.core.teacher.statistics.repositories.TSLevelProjectRepository;
import udpm.hn.studentattendance.core.teacher.statistics.repositories.TSSemesterRepository;
import udpm.hn.studentattendance.core.teacher.statistics.repositories.TSSubjectFacilityRepository;
import udpm.hn.studentattendance.core.teacher.statistics.repositories.TSUserAdminRepository;
import udpm.hn.studentattendance.core.teacher.statistics.repositories.TSUserStaffRepository;
import udpm.hn.studentattendance.core.teacher.statistics.services.TSStatisticsService;
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
import udpm.hn.studentattendance.infrastructure.excel.model.dto.ExStudentModel;
import udpm.hn.studentattendance.repositories.FacilityRepository;
import udpm.hn.studentattendance.utils.CodeGeneratorUtils;
import udpm.hn.studentattendance.utils.DateTimeUtils;
import udpm.hn.studentattendance.utils.ExcelUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
public class TSStatisticsServiceImpl implements TSStatisticsService {

    private final TSSemesterRepository TSSemesterRepository;

    private final TSLevelProjectRepository TSLevelProjectRepository;

    private final TSSubjectFacilityRepository sSSubjectFacilityRepository;

    private final TSFactoryRepository TSFactoryRepository;

    private final TSUserAdminRepository TSUserAdminRepository;

    private final TSUserStaffRepository TSUserStaffRepository;

    private final FacilityRepository facilityRepository;

    private final SessionHelper sessionHelper;

    private final MailerHelper mailerHelper;

    @Value("${app.config.app-name}")
    private String appName;

    @Override
    public ResponseEntity<?> getAllStats(String idSemester) {
        TSAllStatsResponse stats = TSSemesterRepository.getAllStats(idSemester, sessionHelper.getFacilityId(), sessionHelper.getUserId()).orElse(null);
        if (stats == null) {
            return RouterHelper.responseError("Không thể lấy dữ liệu thống kê");
        }

        List<TSChartLevelProjectResponse> levelProjectStats = TSLevelProjectRepository.getStats(idSemester, sessionHelper.getFacilityId(), sessionHelper.getUserId());
        List<TSChartSubjectFacilityResponse> subjectFacilityResponses = sSSubjectFacilityRepository.getStats(idSemester, sessionHelper.getFacilityId(), sessionHelper.getUserId());

        TSAllStatsAndChartDto data = new TSAllStatsAndChartDto();
        data.setStats(stats);
        data.setLevelStats(levelProjectStats);
        data.setSubjectFacilityStats(subjectFacilityResponses);

        return RouterHelper.responseSuccess("Lấy dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> getListStatsFactory(TSFilterFactoryStatsRequest request) {
        request.setIdFacility(sessionHelper.getFacilityId());
        request.setIdUserStaff(sessionHelper.getUserId());
        Pageable pageable = PaginationHelper.createPageable(request);
        PageableObject<TSFactoryStatsResponse> data = PageableObject.of(TSFactoryRepository.getAllByFilter(pageable, request));
        return RouterHelper.responseSuccess("Lấy danh sách dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> getListUser(String idSemester) {
        List<TSUserResponse> lstAdmin = TSUserAdminRepository.getAllList(sessionHelper.getUserEmail());
        List<TSUserResponse> lstStaff = TSUserStaffRepository.getAllListStaff(sessionHelper.getFacilityId(), sessionHelper.getUserEmail());
        TSListUserDto data =  new TSListUserDto();
        data.setAdmin(lstAdmin);
        data.setStaff(lstStaff);
        return RouterHelper.responseSuccess("Lấy dữ liệu thành công", data);
    }

    @Override
    public ResponseEntity<?> sendMailStats(TSSendMailStatsRequest request) {
        Semester semester = TSSemesterRepository.findById(request.getIdSemester()).orElse(null);
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

        Set<String> lstEmails = Stream.of(request.getEmailAdmin(), request.getEmailStaff())
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        String to = sessionHelper.getUserEmail();

        MailerDefaultRequest mailerDefaultRequest = new MailerDefaultRequest();
        mailerDefaultRequest.setTemplate(null);
        mailerDefaultRequest.setTo(to);
        mailerDefaultRequest.setTitle("Báo cáo thống kê giảng viên " + sessionHelper.getUserCode() + " - " + sessionHelper.getUserName() + " - " + appName);

        if (!lstEmails.isEmpty()) {
            mailerDefaultRequest.setBcc(lstEmails.toArray(new String[0]));
        }

        Map<String, Object> dataMail = new HashMap<>();
        dataMail.put("TEACHER_NAME", sessionHelper.getUserCode() + " - " + sessionHelper.getUserName());
        dataMail.put("FROM_DATE", DateTimeUtils.convertMillisToDate(startDate));
        dataMail.put("TO_DATE", DateTimeUtils.convertMillisToDate(endDate));

        Map<String, byte[]> filesMail = new HashMap<>();
        filesMail.put(sessionHelper.getUserCode() + "_" + CodeGeneratorUtils.generateCodeFromString(sessionHelper.getUserName()) + "__" + DateTimeUtils.convertMillisToDate(startDate, "dd-MM-yyyy") + "_" + DateTimeUtils.convertMillisToDate(endDate, "dd-MM-yyyy") + "__report.xlsx", file);

        mailerDefaultRequest.setAttachments(filesMail);
        mailerDefaultRequest.setContent(MailerHelper.loadTemplate(MailerHelper.TEMPLATE_STATISTICS_TEACHER, dataMail));
        mailerHelper.send(mailerDefaultRequest);

        return RouterHelper.responseSuccess("Gửi báo cáo thống kê thành công");
    }

    private byte[] createFileStatistics(String idSemester, Long startDate, Long endDate) {

        List<Factory> lstFactory = TSFactoryRepository.getAllFactoryBySemester(idSemester, sessionHelper.getFacilityId(), sessionHelper.getUserId());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream data = new ByteArrayOutputStream()) {

            for (Factory factory: lstFactory) {
                List<TSPlanDateStudentFactoryResponse> lstData = TSFactoryRepository.getAllPlanDateAttendanceByIdFactory(factory.getId(), DateTimeUtils.toStartOfDay(startDate), DateTimeUtils.toEndOfDay(endDate));
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
                            return LocalDate.parse(datePart, DateTimeFormatter.ofPattern(DateTimeUtils.DATE_FORMAT.replace('/', '-')));
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
                for (ExStudentModel student: lstStudent) {
                    String studentCode = student.getCode();
                    String studentName = student.getName();

                    List<Object> dataCell = new ArrayList<>();
                    dataCell.add(studentCode);
                    dataCell.add(studentName);

                    double total_absent = 0;
                    int total_recovery = 0;
                    for(String namePlanDate: lstPlanDate) {
                        TSPlanDateStudentFactoryResponse planDate = lstData.stream().filter(s -> s.getCode().equals(studentCode) && buildCellPlanDate(s).equals(namePlanDate)).findFirst().orElse(null);
                        if (planDate == null || planDate.getStartDate() > DateTimeUtils.getCurrentTimeMillis()) {
                            dataCell.add(" - ");
                            continue;
                        }
                        if (planDate.getStatus() == AttendanceStatus.PRESENT.ordinal()) {
                            if (planDate.getLateCheckin() != null && planDate.getLateCheckin() > 0 || planDate.getLateCheckout() != null && planDate.getLateCheckout() > 0) {
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

    private String buildCellPlanDate(TSPlanDateStudentFactoryResponse o) {
        return DateTimeUtils.convertMillisToDate(o.getStartDate(), DateTimeUtils.DATE_FORMAT.replace('/', '-')) + " - Ca " + o.getShift();
    }

}
