package udpm.hn.studentattendance.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ExcelUtils {

    private final static String SHEET_DATA = "HiddenDataSheet";

    public static Sheet createTemplate(Workbook workbook, String sheetName, List<String> headers, List<List<Object>> data) {
        Sheet sheet = workbook.createSheet(sheetName);
        int rowIndex = 0;
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

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
        return sheet;
    }

    public static Sheet createHiddenSheet(Workbook workbook, String sheetName) {
        return createHiddenSheet(workbook, sheetName, new ArrayList<>());
    }

    public static Sheet createHiddenSheet(Workbook workbook, String sheetName, List<List<String>> data) {
        Sheet hiddenSheet = workbook.createSheet(sheetName);
        workbook.setSheetHidden(workbook.getSheetIndex(hiddenSheet), true);
        if (!data.isEmpty()) {
            for (int column = 0; column < data.size(); column++) {
                setRangeDataColumn(hiddenSheet, column, data.get(column));
            }
        }
        return hiddenSheet;
    }

    public static void setRangeDataColumn(Sheet sheet, int columnIndex, List<?> data) {
        for(int row = 0; row < data.size(); row++) {
            Row r = sheet.getRow(row);
            if (r == null) {
                r = sheet.createRow(row);
            }
            Cell cell = r.createCell(columnIndex);
            Object value = data.get(row);
            if (value instanceof String) {
                cell.setCellValue((String) value);
            } else if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else if (value instanceof Boolean) {
                cell.setCellValue((Boolean) value);
            } else if (value instanceof Date) {
                cell.setCellValue((Date) value);
            } else if (value != null) {
                cell.setCellValue(value.toString());
            }
        }
    }

    public static void addDateValidation(Sheet sheet, int firstRow, int lastRow, int columnIndex, String dateFormat, String startDate, String endDate) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        CellStyle dateStyle = sheet.getWorkbook().createCellStyle();
        CreationHelper createHelper = sheet.getWorkbook().getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat(dateFormat));
        sheet.setDefaultColumnStyle(columnIndex, dateStyle);
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = validationHelper.createDateConstraint(
                DataValidationConstraint.OperatorType.BETWEEN,
                String.valueOf(DateUtil.getExcelDate(simpleDateFormat.parse(startDate))),
                String.valueOf(DateUtil.getExcelDate(simpleDateFormat.parse(endDate))),
                dateFormat);
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, columnIndex, columnIndex);
        DataValidation validation = validationHelper.createValidation(constraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        validation.createErrorBox("Sai dữ liệu", "Hãy nhập đúng định dạng ngày (" +dateFormat + ")");
        validation.createPromptBox("Nhập dữ liệu", "Hãy nhập ngày (" +dateFormat + ")");
        sheet.addValidationData(validation);
    }

    public static void addIntegerValidation(Sheet sheet, int firstRow, int lastRow, int columnIndex) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createNumericConstraint(
                DataValidationConstraint.ValidationType.INTEGER,
                DataValidationConstraint.OperatorType.GREATER_OR_EQUAL,
                "0",
                null
        );
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, columnIndex, columnIndex);
        DataValidation validation = helper.createValidation(constraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        validation.createErrorBox("Sai dữ liệu", "Hãy nhập đúng định dạng số nguyên");
        validation.createPromptBox("Nhập dữ liệu", "Hãy nhập dữ liệu là số nguyên");
        sheet.addValidationData(validation);
    }

    public static void addListValidation(Sheet sheet, int firstRow, int lastRow, int columnIndex, List<?> dataValid) {
        if (dataValid.isEmpty()) {
            return;
        }

        int index = sheet.getWorkbook().getSheetIndex(SHEET_DATA);

        Sheet hiddenSheet = index == -1 ? createHiddenSheet(sheet.getWorkbook(), SHEET_DATA) : sheet.getWorkbook().getSheetAt(index);
        setRangeDataColumn(hiddenSheet, columnIndex, dataValid);

        String colLetter = CellReference.convertNumToColString(columnIndex);
        String formula = SHEET_DATA + "!" + colLetter + "$1:" + colLetter + "$" + dataValid.size();

        DataValidationHelper helper = sheet.getDataValidationHelper();

        CellRangeAddressList dataCell = new CellRangeAddressList(firstRow, lastRow, columnIndex, columnIndex);

        DataValidationConstraint constraint = helper.createFormulaListConstraint(formula);
        DataValidation validation = helper.createValidation(constraint, dataCell);
        validation.setShowErrorBox(true);
        validation.setSuppressDropDownArrow(true);
        validation.createErrorBox("Sai dữ liệu", "Hãy chọn dữ liệu cho sẵn");
        validation.createPromptBox("Chọn dữ liệu", "Hãy chọn dữ liệu cho sẵn");
        sheet.addValidationData(validation);
    }

    public static Row insertRow(Sheet sheet, int rowIndex, List<Object> cellData) {
        return insertRow(sheet, rowIndex, cellData, null);
    }

    public static Row insertRow(Sheet sheet, int rowIndex, List<Object> cellData, Map<Object, String> colorMapping) {
        int lastRowNum = sheet.getLastRowNum();

        if (rowIndex <= lastRowNum) {
            sheet.shiftRows(rowIndex, lastRowNum, 1, true, false);
        }

        Row newRow = sheet.createRow(rowIndex);

        for (int i = 0; i < cellData.size(); i++) {
            Cell cell = newRow.createCell(i);
            Object value = cellData.get(i);
            setCellValue(cell, value);
            if (colorMapping != null) {
                applyCellStyleByValue(cell, value, colorMapping);
            }
        }

        return newRow;
    }

    private static void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof java.util.Date) {
            CellStyle style = cell.getSheet().getWorkbook().createCellStyle();
            CreationHelper createHelper = cell.getSheet().getWorkbook().getCreationHelper();
            style.setDataFormat(createHelper.createDataFormat().getFormat("dd-mm-yyyy"));
            cell.setCellStyle(style);
            cell.setCellValue((java.util.Date) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private static void applyCellStyleByValue(Cell cell, Object value, Map<Object, String> colorMapping) {
        if (colorMapping == null || colorMapping.isEmpty()) return;

        String hexColor = colorMapping.get(value);
        if (hexColor == null) return;

        applyHexColor(cell, hexColor);
    }

    public static void applyHexColor(Cell cell, String hex) {
        XSSFColor color = hexToColor(hex);
        CellStyle style = cell.getSheet().getWorkbook().createCellStyle();
        style.setFillForegroundColor(color);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cell.setCellStyle(style);
    }

    public static XSSFColor hexToColor(String hex) {
        if (hex == null || hex.isEmpty()) {
            return null;
        }
        if (hex.charAt(0) == '#') {
            hex = hex.substring(1);
        }

        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);

        byte[] rgb = new byte[3];
        rgb[0] = (byte) r;
        rgb[1] = (byte) g;
        rgb[2] = (byte) b;

        return new XSSFColor(rgb);
    }

}
