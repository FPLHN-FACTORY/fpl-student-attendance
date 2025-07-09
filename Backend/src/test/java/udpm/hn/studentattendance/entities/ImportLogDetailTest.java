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

        assertEquals(d1, d2);
        assertNotEquals(d1, d3);
        assertEquals(d1.hashCode(), d2.hashCode());
        assertNotEquals(d1.hashCode(), d3.hashCode());
    }

    @Test
    void testToString() {
        ImportLog importLog = new ImportLog();
        importLog.setId("1");
        ImportLogDetail detail = new ImportLogDetail();
        detail.setId("1");
        detail.setLine(5);
        detail.setMessage("Error on line 5");
        detail.setImportLog(importLog);
        String toString = detail.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("ImportLogDetail"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("line=5"));
        assertTrue(toString.contains("message=Error on line 5"));
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