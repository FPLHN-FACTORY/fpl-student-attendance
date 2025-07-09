package udpm.hn.studentattendance.entities;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ImportLogTest {
    @Test
    void testNoArgsConstructor() {
        ImportLog importLog = new ImportLog();
        assertNull(importLog.getIdUser());
        assertNull(importLog.getCode());
        assertNull(importLog.getFileName());
        assertNull(importLog.getType());
        assertNull(importLog.getImportLogDetails());
        assertNull(importLog.getFacility());
    }

    @Test
    void testAllArgsConstructor() {
        Facility facility = new Facility();
        facility.setId("1");
        ImportLogDetail detail = new ImportLogDetail();
        detail.setId("2");
        List<ImportLogDetail> details = List.of(detail);
        ImportLog importLog = new ImportLog(
                "user1",
                "code1",
                "file.xlsx",
                2,
                details,
                facility);
        assertEquals("user1", importLog.getIdUser());
        assertEquals("code1", importLog.getCode());
        assertEquals("file.xlsx", importLog.getFileName());
        assertEquals(2, importLog.getType());
        assertEquals(details, importLog.getImportLogDetails());
        assertEquals(facility, importLog.getFacility());
    }

    @Test
    void testSettersAndGetters() {
        ImportLog importLog = new ImportLog();
        Facility facility = new Facility();
        facility.setId("1");
        ImportLogDetail detail = new ImportLogDetail();
        detail.setId("2");
        List<ImportLogDetail> details = List.of(detail);
        importLog.setIdUser("user1");
        importLog.setCode("code1");
        importLog.setFileName("file.xlsx");
        importLog.setType(2);
        importLog.setImportLogDetails(details);
        importLog.setFacility(facility);
        assertEquals("user1", importLog.getIdUser());
        assertEquals("code1", importLog.getCode());
        assertEquals("file.xlsx", importLog.getFileName());
        assertEquals(2, importLog.getType());
        assertEquals(details, importLog.getImportLogDetails());
        assertEquals(facility, importLog.getFacility());
    }

    @Test
    void testEqualsAndHashCode() {
        Facility facility = new Facility();
        facility.setId("1");
        ImportLogDetail detail = new ImportLogDetail();
        detail.setId("2");
        List<ImportLogDetail> details = List.of(detail);
        ImportLog il1 = new ImportLog();
        il1.setId("1");
        il1.setIdUser("user1");
        il1.setCode("code1");
        il1.setFileName("file.xlsx");
        il1.setType(2);
        il1.setImportLogDetails(details);
        il1.setFacility(facility);

        ImportLog il2 = new ImportLog();
        il2.setId("1");
        il2.setIdUser("user1");
        il2.setCode("code1");
        il2.setFileName("file.xlsx");
        il2.setType(2);
        il2.setImportLogDetails(details);
        il2.setFacility(facility);

        ImportLog il3 = new ImportLog();
        il3.setId("2");
        il3.setIdUser("user1");
        il3.setCode("code1");
        il3.setFileName("file.xlsx");
        il3.setType(2);
        il3.setImportLogDetails(details);
        il3.setFacility(facility);

        assertEquals(il1, il2);
        assertNotEquals(il1, il3);
        assertEquals(il1.hashCode(), il2.hashCode());
        assertNotEquals(il1.hashCode(), il3.hashCode());
    }

    @Test
    void testToString() {
        Facility facility = new Facility();
        facility.setId("1");
        ImportLogDetail detail = new ImportLogDetail();
        detail.setId("2");
        List<ImportLogDetail> details = List.of(detail);
        ImportLog importLog = new ImportLog();
        importLog.setId("1");
        importLog.setIdUser("user1");
        importLog.setCode("code1");
        importLog.setFileName("file.xlsx");
        importLog.setType(2);
        importLog.setImportLogDetails(details);
        importLog.setFacility(facility);
        String toString = importLog.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("ImportLog"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("idUser=user1"));
        assertTrue(toString.contains("code=code1"));
        assertTrue(toString.contains("fileName=file.xlsx"));
        assertTrue(toString.contains("type=2"));
    }

    @Test
    void testEqualsWithNull() {
        ImportLog importLog = new ImportLog();
        importLog.setId("1");
        assertNotEquals(null, importLog);
    }

    @Test
    void testEqualsWithDifferentClass() {
        ImportLog importLog = new ImportLog();
        importLog.setId("1");
        Object other = new Object();
        assertNotEquals(importLog, other);
    }

    @Test
    void testEqualsWithSameObject() {
        ImportLog importLog = new ImportLog();
        importLog.setId("1");
        assertEquals(importLog, importLog);
    }
}