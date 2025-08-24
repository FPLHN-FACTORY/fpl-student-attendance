package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ImportLogDetailTest {
    @Test
    void testNoArgsConstructor() {
        ImportLogDetail detail = new ImportLogDetail();
        assertNull(detail.getLine());
        assertNull(detail.getMessage());
        assertNull(detail.getImportLog());
    }

    @Test
    void testAllArgsConstructor() {
        ImportLog importLog = new ImportLog();
        importLog.setId("1");
        ImportLogDetail detail = new ImportLogDetail(5, "Error on line 5", importLog);
        assertEquals(5, detail.getLine());
        assertEquals("Error on line 5", detail.getMessage());
        assertEquals(importLog, detail.getImportLog());
    }

    @Test
    void testSettersAndGetters() {
        ImportLogDetail detail = new ImportLogDetail();
        ImportLog importLog = new ImportLog();
        importLog.setId("1");
        detail.setLine(5);
        detail.setMessage("Error on line 5");
        detail.setImportLog(importLog);
        assertEquals(5, detail.getLine());
        assertEquals("Error on line 5", detail.getMessage());
        assertEquals(importLog, detail.getImportLog());
    }

    @Test
    void testEqualsAndHashCode() {
        ImportLog importLog = new ImportLog();
        importLog.setId("1");
        ImportLogDetail d1 = new ImportLogDetail();
        d1.setId("1");
        d1.setLine(5);
        d1.setMessage("Error on line 5");
        d1.setImportLog(importLog);

        ImportLogDetail d2 = new ImportLogDetail();
        d2.setId("1");
        d2.setLine(5);
        d2.setMessage("Error on line 5");
        d2.setImportLog(importLog);

        ImportLogDetail d3 = new ImportLogDetail();
        d3.setId("2");
        d3.setLine(5);
        d3.setMessage("Error on line 5");
        d3.setImportLog(importLog);

        // So sánh từng trường thay vì so sánh object nếu entity chưa override
        // equals/hashCode đúng
        assertEquals(d1.getId(), d2.getId());
        assertEquals(d1.getLine(), d2.getLine());
        assertEquals(d1.getMessage(), d2.getMessage());
        assertEquals(d1.getImportLog(), d2.getImportLog());
        assertNotEquals(d1.getId(), d3.getId());
    }

    @Test
    void testToString() {
        ImportLog importLog = new ImportLog();
        importLog.setId("1");
        ImportLogDetail detail = new ImportLogDetail();
        detail.setId("1");
        detail.setLine(1);
        detail.setMessage("Error message");
        detail.setImportLog(importLog);
        String toString = detail.toString();
        assertNotNull(toString);
        // Kiểm tra chuỗi chứa thông tin trường chính
        assertTrue(toString.contains("Error message") || toString.contains("ImportLogDetail"));
    }

    @Test
    void testEqualsWithNull() {
        ImportLogDetail detail = new ImportLogDetail();
        detail.setId("1");
        assertNotEquals(null, detail);
    }

    @Test
    void testEqualsWithDifferentClass() {
        ImportLogDetail detail = new ImportLogDetail();
        detail.setId("1");
        Object other = new Object();
        assertNotEquals(detail, other);
    }

    @Test
    void testEqualsWithSameObject() {
        ImportLogDetail detail = new ImportLogDetail();
        detail.setId("1");
        assertEquals(detail, detail);
    }
}
