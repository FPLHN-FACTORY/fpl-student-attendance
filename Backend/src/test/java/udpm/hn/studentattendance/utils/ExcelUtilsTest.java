package udpm.hn.studentattendance.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class ExcelUtilsTest {

    private Workbook workbook;
    private Sheet sheet;

    @BeforeEach
    void setUp() {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("TestSheet");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (workbook != null) {
            workbook.close();
        }
    }

    @Test
    @DisplayName("Test createTemplate with valid data")
    void testCreateTemplateWithValidData() {
        List<String> headers = Arrays.asList("Name", "Age", "Email");
        List<List<Object>> data = Arrays.asList(
                Arrays.asList("John", 25, "john@example.com"),
                Arrays.asList("Jane", 30, "jane@example.com"));

        Sheet result = ExcelUtils.createTemplate(workbook, "TestTemplate", headers, data);

        assertThat(result).isNotNull();
        assertThat(result.getSheetName()).isEqualTo("TestTemplate");
        assertThat(result.getRow(0)).isNotNull();
        assertThat(result.getRow(1)).isNotNull();
        assertThat(result.getRow(2)).isNotNull();
    }

    @Test
    @DisplayName("Test createTemplate with empty data")
    void testCreateTemplateWithEmptyData() {
        List<String> headers = Arrays.asList("Name", "Age");
        List<List<Object>> data = new ArrayList<>();

        Sheet result = ExcelUtils.createTemplate(workbook, "EmptyTemplate", headers, data);

        assertThat(result).isNotNull();
        assertThat(result.getRow(0)).isNotNull(); // Header row
        assertThat(result.getRow(1)).isNull(); // No data rows
    }

    @Test
    @DisplayName("Test createTemplate with null data")
    void testCreateTemplateWithNullData() {
        List<String> headers = Arrays.asList("Name", "Age");
        List<List<Object>> data = Arrays.asList(
                Arrays.asList("John", null),
                Arrays.asList(null, 30));

        Sheet result = ExcelUtils.createTemplate(workbook, "NullDataTemplate", headers, data);

        assertThat(result).isNotNull();
        assertThat(result.getRow(1)).isNotNull();
        assertThat(result.getRow(2)).isNotNull();
    }

    @Test
    @DisplayName("Test createHiddenSheet without data")
    void testCreateHiddenSheetWithoutData() {
        Sheet result = ExcelUtils.createHiddenSheet(workbook, "HiddenSheet");

        assertThat(result).isNotNull();
        assertThat(result.getSheetName()).isEqualTo("HiddenSheet");
        assertThat(workbook.isSheetHidden(workbook.getSheetIndex(result))).isTrue();
    }

    @Test
    @DisplayName("Test createHiddenSheet with data")
    void testCreateHiddenSheetWithData() {
        List<List<String>> data = Arrays.asList(
                Arrays.asList("Option1", "Option2", "Option3"),
                Arrays.asList("Value1", "Value2", "Value3"));

        Sheet result = ExcelUtils.createHiddenSheet(workbook, "HiddenDataSheet", data);

        assertThat(result).isNotNull();
        assertThat(result.getSheetName()).isEqualTo("HiddenDataSheet");
        assertThat(workbook.isSheetHidden(workbook.getSheetIndex(result))).isTrue();
    }

    @Test
    @DisplayName("Test setRangeDataColumn with string data")
    void testSetRangeDataColumnWithStringData() {
        List<String> data = Arrays.asList("Value1", "Value2", "Value3");
        int columnIndex = 0;

        ExcelUtils.setRangeDataColumn(sheet, columnIndex, data);

        assertThat(sheet.getRow(0)).isNotNull();
        assertThat(sheet.getRow(1)).isNotNull();
        assertThat(sheet.getRow(2)).isNotNull();
    }

    @Test
    @DisplayName("Test setRangeDataColumn with numeric data")
    void testSetRangeDataColumnWithNumericData() {
        List<Integer> data = Arrays.asList(1, 2, 3);
        int columnIndex = 0;

        ExcelUtils.setRangeDataColumn(sheet, columnIndex, data);

        assertThat(sheet.getRow(0)).isNotNull();
        assertThat(sheet.getRow(1)).isNotNull();
        assertThat(sheet.getRow(2)).isNotNull();
    }

    @Test
    @DisplayName("Test setRangeDataColumn with boolean data")
    void testSetRangeDataColumnWithBooleanData() {
        List<Boolean> data = Arrays.asList(true, false, true);
        int columnIndex = 0;

        ExcelUtils.setRangeDataColumn(sheet, columnIndex, data);

        assertThat(sheet.getRow(0)).isNotNull();
        assertThat(sheet.getRow(1)).isNotNull();
        assertThat(sheet.getRow(2)).isNotNull();
    }

    @Test
    @DisplayName("Test setRangeDataColumn with date data")
    void testSetRangeDataColumnWithDateData() {
        List<Date> data = Arrays.asList(new Date(), new Date(), new Date());
        int columnIndex = 0;

        ExcelUtils.setRangeDataColumn(sheet, columnIndex, data);

        assertThat(sheet.getRow(0)).isNotNull();
        assertThat(sheet.getRow(1)).isNotNull();
        assertThat(sheet.getRow(2)).isNotNull();
    }

    @Test
    @DisplayName("Test setRangeDataColumn with null data")
    void testSetRangeDataColumnWithNullData() {
        List<Object> data = Arrays.asList("Value1", null, "Value3");
        int columnIndex = 0;

        ExcelUtils.setRangeDataColumn(sheet, columnIndex, data);

        assertThat(sheet.getRow(0)).isNotNull();
        assertThat(sheet.getRow(1)).isNotNull();
        assertThat(sheet.getRow(2)).isNotNull();
    }

    @Test
    @DisplayName("Test addDateValidation with valid parameters")
    void testAddDateValidationWithValidParameters() throws ParseException {
        String dateFormat = "dd/MM/yyyy";
        String startDate = "01/01/2023";
        String endDate = "31/12/2023";
        int firstRow = 1;
        int lastRow = 10;
        int columnIndex = 0;

        assertDoesNotThrow(() -> {
            ExcelUtils.addDateValidation(sheet, firstRow, lastRow, columnIndex, dateFormat, startDate, endDate);
        });
    }

    @Test
    @DisplayName("Test addDateValidation with invalid date format")
    void testAddDateValidationWithInvalidDateFormat() {
        String dateFormat = "invalid-format";
        String startDate = "01/01/2023";
        String endDate = "31/12/2023";
        int firstRow = 1;
        int lastRow = 10;
        int columnIndex = 0;

        assertThrows(IllegalArgumentException.class, () -> {
            ExcelUtils.addDateValidation(sheet, firstRow, lastRow, columnIndex, dateFormat, startDate, endDate);
        });
    }

    @Test
    @DisplayName("Test addIntegerValidation")
    void testAddIntegerValidation() {
        int firstRow = 1;
        int lastRow = 10;
        int columnIndex = 0;

        assertDoesNotThrow(() -> {
            ExcelUtils.addIntegerValidation(sheet, firstRow, lastRow, columnIndex);
        });
    }

    @Test
    @DisplayName("Test addListValidation with valid data")
    void testAddListValidationWithValidData() {
        List<String> dataValid = Arrays.asList("Option1", "Option2", "Option3");
        int firstRow = 1;
        int lastRow = 10;
        int columnIndex = 0;

        assertDoesNotThrow(() -> {
            ExcelUtils.addListValidation(sheet, firstRow, lastRow, columnIndex, dataValid);
        });
    }

    @Test
    @DisplayName("Test addListValidation with empty data")
    void testAddListValidationWithEmptyData() {
        List<String> dataValid = new ArrayList<>();
        int firstRow = 1;
        int lastRow = 10;
        int columnIndex = 0;

        assertDoesNotThrow(() -> {
            ExcelUtils.addListValidation(sheet, firstRow, lastRow, columnIndex, dataValid);
        });
    }

    @Test
    @DisplayName("Test insertRow without color mapping")
    void testInsertRowWithoutColorMapping() {
        List<Object> cellData = Arrays.asList("Name", 25, "email@example.com");
        int rowIndex = 1;

        Row result = ExcelUtils.insertRow(sheet, rowIndex, cellData);

        assertThat(result).isNotNull();
        assertThat(result.getRowNum()).isEqualTo(rowIndex);
    }

    @Test
    @DisplayName("Test insertRow with color mapping")
    void testInsertRowWithColorMapping() {
        List<Object> cellData = Arrays.asList("Name", 25, "email@example.com");
        Map<Object, String> colorMapping = new HashMap<>();
        colorMapping.put("Name", "#FF0000");
        colorMapping.put(25, "#00FF00");
        int rowIndex = 1;

        Row result = ExcelUtils.insertRow(sheet, rowIndex, cellData, colorMapping);

        assertThat(result).isNotNull();
        assertThat(result.getRowNum()).isEqualTo(rowIndex);
    }

    @Test
    @DisplayName("Test insertRow at beginning")
    void testInsertRowAtBeginning() {
        // Create some existing rows
        sheet.createRow(0);
        sheet.createRow(1);
        sheet.createRow(2);

        List<Object> cellData = Arrays.asList("NewRow");
        int rowIndex = 0;

        Row result = ExcelUtils.insertRow(sheet, rowIndex, cellData);

        assertThat(result).isNotNull();
        assertThat(result.getRowNum()).isEqualTo(rowIndex);
    }

    @Test
    @DisplayName("Test insertRow in middle")
    void testInsertRowInMiddle() {
        // Create some existing rows
        sheet.createRow(0);
        sheet.createRow(1);
        sheet.createRow(2);

        List<Object> cellData = Arrays.asList("NewRow");
        int rowIndex = 1;

        Row result = ExcelUtils.insertRow(sheet, rowIndex, cellData);

        assertThat(result).isNotNull();
        assertThat(result.getRowNum()).isEqualTo(rowIndex);
    }

    @Test
    @DisplayName("Test applyHexColor with valid hex")
    void testApplyHexColorWithValidHex() {
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);

        assertDoesNotThrow(() -> {
            ExcelUtils.applyHexColor(cell, "#FF0000");
        });
    }

    @Test
    @DisplayName("Test applyHexColor with invalid hex")
    void testApplyHexColorWithInvalidHex() {
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);

        assertThrows(NumberFormatException.class, () -> {
            ExcelUtils.applyHexColor(cell, "invalid-hex");
        });
    }

    @Test
    @DisplayName("Test hexToColor with valid hex")
    void testHexToColorWithValidHex() {
        XSSFColor result = ExcelUtils.hexToColor("#FF0000");
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Test hexToColor with invalid hex")
    void testHexToColorWithInvalidHex() {
        assertThrows(NumberFormatException.class, () -> {
            ExcelUtils.hexToColor("invalid-hex");
        });
    }

    @Test
    @DisplayName("Test edge case with null workbook")
    void testEdgeCaseWithNullWorkbook() {
        assertThrows(NullPointerException.class, () -> {
            ExcelUtils.createTemplate(null, "Test", Arrays.asList("Header"), new ArrayList<>());
        });
    }

    @Test
    @DisplayName("Test edge case with null headers")
    void testEdgeCaseWithNullHeaders() {
        assertThrows(NullPointerException.class, () -> {
            ExcelUtils.createTemplate(workbook, "Test", null, new ArrayList<>());
        });
    }

    @Test
    @DisplayName("Test edge case with null data")
    void testEdgeCaseWithNullData() {
        assertThrows(NullPointerException.class, () -> {
            ExcelUtils.createTemplate(workbook, "Test", Arrays.asList("Header"), null);
        });
    }

    @Test
    @DisplayName("Test edge case with negative row index")
    void testEdgeCaseWithNegativeRowIndex() {
        List<Object> cellData = Arrays.asList("Test");
        int rowIndex = -1;

        assertThrows(IllegalArgumentException.class, () -> {
            ExcelUtils.insertRow(sheet, rowIndex, cellData);
        });
    }

    @Test
    @DisplayName("Test edge case with null cell data")
    void testEdgeCaseWithNullCellData() {
        int rowIndex = 1;

        assertThrows(NullPointerException.class, () -> {
            ExcelUtils.insertRow(sheet, rowIndex, null);
        });
    }
}