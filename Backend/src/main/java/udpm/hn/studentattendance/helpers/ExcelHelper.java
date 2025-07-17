package udpm.hn.studentattendance.helpers;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.ImportLog;
import udpm.hn.studentattendance.entities.ImportLogDetail;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.ImportLogType;
import udpm.hn.studentattendance.infrastructure.excel.model.request.EXImportRequest;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogDetailRepository;
import udpm.hn.studentattendance.infrastructure.excel.repositories.EXImportLogRepository;
import udpm.hn.studentattendance.utils.CodeGeneratorUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@RequiredArgsConstructor
public class ExcelHelper {

    private static final Logger logger = LoggerFactory.getLogger(ExcelHelper.class);

    public final static EntityStatus STATUS_SUCCESS = EntityStatus.ACTIVE;

    public final static EntityStatus STATUS_ERROR = EntityStatus.INACTIVE;

    private final EXImportLogRepository importLogRepository;

    private final EXImportLogDetailRepository importLogDetailRepository;

    private final SessionHelper sessionHelper;

    public void saveLogSuccess(ImportLogType type, String message, EXImportRequest request) {
        saveLog(type, STATUS_SUCCESS, message, request);
    }

    public void saveLogError(ImportLogType type, String message, EXImportRequest request) {
        saveLog(type, STATUS_ERROR, message, request);
    }

    private void saveLog(ImportLogType type, EntityStatus status, String message, EXImportRequest request) {
        ImportLog importLog = importLogRepository.findByIdUserAndCodeAndFileNameAndFacility_Id(sessionHelper.getUserId(), request.getCode(), request.getFileName(), sessionHelper.getFacilityId()).orElse(null);
        if (importLog == null) {
            Facility facility = new Facility();
            facility.setId(sessionHelper.getFacilityId());

            if (facility.getId() == null) {
                facility = null;
            }

            ImportLog newImportLog = new ImportLog();
            newImportLog.setIdUser(sessionHelper.getUserId());
            newImportLog.setFacility(facility);
            newImportLog.setCode(request.getCode());
            newImportLog.setType(type.ordinal());
            newImportLog.setFileName(request.getFileName());
            importLog = importLogRepository.save(newImportLog);
        }

        ImportLogDetail importLogDetail = new ImportLogDetail();
        importLogDetail.setImportLog(importLog);
        importLogDetail.setLine(request.getLine());
        importLogDetail.setMessage(message);
        importLogDetail.setStatus(status);

        importLogDetailRepository.save(importLogDetail);
    }

    public static List<Map<String, String>> readFile(MultipartFile file) throws IOException {
        List<Map<String, String>> result = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        if (sheet == null) {
            throw new IllegalArgumentException("File Excel không có dữ liệu!");
        }

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalArgumentException("File Excel không có tiêu đề cột!");
        }

        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            headers.add(cell.getStringCellValue().trim());
        }

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Map<String, String> rowData = new HashMap<>();
            rowData.put("_LINE", String.valueOf(i));
            for (int j = 0; j < headers.size(); j++) {
                Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                rowData.put(CodeGeneratorUtils.generateCodeFromString(headers.get(j)), getCellValue(cell));
            }
            result.add(rowData);
        }

        workbook.close();

        Set<Map<String, String>> uniqueSet = new LinkedHashSet<>(result);
        return new ArrayList<>(uniqueSet);
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";

        CellType type = cell.getCellType();

        if (type == CellType.FORMULA) {
            FormulaEvaluator evaluator = cell.getSheet().getWorkbook()
                    .getCreationHelper().createFormulaEvaluator();
            CellValue evaluated = evaluator.evaluate(cell);
            return getEvaluatedValue(cell, evaluated);
        }

        return getEvaluatedValue(cell, cell.getCellType(), cell);
    }

    private static String getEvaluatedValue(Cell cell, CellValue cellValue) {
        return switch (cellValue.getCellType()) {
            case STRING -> cellValue.getStringValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? new SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue())
                    : formatNumeric(cellValue.getNumberValue());
            case BOOLEAN -> String.valueOf(cellValue.getBooleanValue());
            default -> "";
        };
    }

    private static String getEvaluatedValue(Cell cell, CellType type, Cell originCell) {
        return switch (type) {
            case STRING -> originCell.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? new SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue())
                    : formatNumeric(originCell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(originCell.getBooleanCellValue());
            default -> "";
        };
    }

    private static String formatNumeric(double value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    public static byte[] createExcelStream(String sheetName, List<String> headers, List<List<Object>> data) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);
            int rowIndex = 0;

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setWrapText(true);
            Row headerRow = sheet.createRow(rowIndex++);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            for (List<Object> rowData : data) {
                Row row = sheet.createRow(rowIndex++);
                for (int colIndex = 0; colIndex < rowData.size(); colIndex++) {
                    Cell cell = row.createCell(colIndex);
                    Object value = rowData.get(colIndex);
                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else {
                        cell.setCellValue(value != null ? value.toString() : "");
                    }
                }
            }

            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

}
