package udpm.hn.studentattendance.infrastructure.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import udpm.hn.studentattendance.infrastructure.constants.EntityStatus;
import udpm.hn.studentattendance.infrastructure.constants.ImportLogType;

class ImportLogRequestTest {
    @Test
    void testAllArgsConstructorAndGetters() {
        ImportLogRequest req = new ImportLogRequest(ImportLogType.PLAN_DATE, "C01", "file.xlsx", EntityStatus.ACTIVE,
                10, "msg");
        assertEquals(ImportLogType.PLAN_DATE, req.getType());
        assertEquals("C01", req.getCode());
        assertEquals("file.xlsx", req.getFileName());
        assertEquals(EntityStatus.ACTIVE, req.getStatus());
        assertEquals(10, req.getLine());
        assertEquals("msg", req.getMessage());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        ImportLogRequest req = new ImportLogRequest();
        req.setType(ImportLogType.STAFF);
        req.setCode("C02");
        req.setFileName("file2.xlsx");
        req.setStatus(EntityStatus.INACTIVE);
        req.setLine(20);
        req.setMessage("err");
        assertEquals(ImportLogType.STAFF, req.getType());
        assertEquals("C02", req.getCode());
        assertEquals("file2.xlsx", req.getFileName());
        assertEquals(EntityStatus.INACTIVE, req.getStatus());
        assertEquals(20, req.getLine());
        assertEquals("err", req.getMessage());
    }
}