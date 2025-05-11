package udpm.hn.studentattendance.core.admin.project.controller;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.core.admin.project.model.request.ProjectAddImport;
import udpm.hn.studentattendance.core.admin.project.repository.AdminLevelProjectManagementRepository;
import udpm.hn.studentattendance.core.admin.project.repository.AdminSemesterManagementRepository;
import udpm.hn.studentattendance.core.admin.project.repository.AdminSubjectManagementRepository;
import udpm.hn.studentattendance.entities.Project;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.router.RouteAdminConstant;
import udpm.hn.studentattendance.repositories.*;

import java.io.IOException;
import java.util.Iterator;

@RestController
@RequestMapping(RouteAdminConstant.URL_API_PROJECT_MANAGEMENT)
public class AdminProjectExcel {

    @Autowired
    private AdminLevelProjectManagementRepository levelProjectRepository;

    @Autowired
    private AdminSemesterManagementRepository semesterRepository;

    @Autowired
    private AdminSubjectManagementRepository subjectManagementRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SubjectFacilityRepository subjectFacilityRepository;

    private final ResourceLoader resourceLoader;

    public AdminProjectExcel(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping("/download-template")
    public ResponseEntity<InputStreamResource> downloadTemplate() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:templates/excel/TemplateImportProjectExcel.xlsx");

        if (!resource.exists()) {
            throw new RuntimeException("File template not found!");
        }

        InputStreamResource inputStreamResource = new InputStreamResource(resource.getInputStream());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=TemplateImportProjectExcel.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(inputStreamResource);
    }

    @PostMapping("/import-excel")
    public void importTemplate(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Tệp không được để trống");
        }

        // Đọc tệp Excel
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0); // Lấy sheet đầu tiên

        Iterator<Row> rowIterator = sheet.iterator();
        // Bỏ qua dòng tiêu đề, bắt đầu từ dòng thứ 2
        if (rowIterator.hasNext()) {
            rowIterator.next(); // Bỏ qua dòng tiêu đề
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            ProjectAddImport project = new ProjectAddImport();

            // Kiểm tra và gán giá trị cho từng ô
            project.setName(getCellStringValue(row.getCell(1)));
            project.setDescription(getCellStringValue(row.getCell(2)));
            project.setLevelProjectCode(getCellStringValue(row.getCell(3)));
            project.setSemesterCode(getCellStringValue(row.getCell(5)));
            project.setSubjectCode(getCellStringValue(row.getCell(4)));
            project.setFacilityCode(getCellStringValue(row.getCell(6)));
            projectRepository.save(convertProjectImport(project));
        }

        workbook.close(); // Đóng workbook
    }

    // Phương thức hỗ trợ để lấy giá trị chuỗi từ ô
    private String getCellStringValue(Cell cell) {
        if (cell != null) {
            return cell.getCellType() == CellType.STRING ? cell.getStringCellValue()
                    : String.valueOf(cell.getNumericCellValue());
        }
        return null; // Hoặc có thể trả về một giá trị mặc định như ""
    }

    private Project convertProjectImport(ProjectAddImport projectAddImport) {
        Project project = new Project();
        project.setName(projectAddImport.getName());
        project.setDescription(projectAddImport.getDescription());
        project.setLevelProject(levelProjectRepository.getLevelProject(projectAddImport.getLevelProjectCode()));
        project.setSemester(semesterRepository.getSemester(projectAddImport.getSemesterCode()));
        project.setSubjectFacility(subjectFacilityRepository.findById(
                subjectManagementRepository.getSubjectFacilityImport(projectAddImport.getSubjectCode(),
                        projectAddImport.getFacilityCode()))
                .get());
        project.setStatus(EntityStatus.ACTIVE);
        return project;
    }
}
