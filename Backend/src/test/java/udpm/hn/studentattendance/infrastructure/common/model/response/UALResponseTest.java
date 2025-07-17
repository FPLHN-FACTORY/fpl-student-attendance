package udpm.hn.studentattendance.infrastructure.common.model.response;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;

class UALResponseTest {
    static class TestUALResponse implements UALResponse {
        @Override
        public String getId() {
            return "id1";
        }

        @Override
        public Long getCreatedAt() {
            return 10L;
        }

        @Override
        public Long getUpdatedAt() {
            return 20L;
        }

        @Override
        public String getUserName() {
            return "user";
        }

        @Override
        public String getFacilityName() {
            return "facility";
        }

        @Override
        public String getUserCode() {
            return "code";
        }

        @Override
        public String getMessage() {
            return "msg";
        }

        @Override
        public Integer getRole() {
            return 2;
        }
    }

    @Test
    void testUALResponseMethods() {
        UALResponse obj = new TestUALResponse();
        assertEquals("id1", obj.getId());
        assertEquals(10L, obj.getCreatedAt());
        assertEquals(20L, obj.getUpdatedAt());
        assertEquals("user", obj.getUserName());
        assertEquals("facility", obj.getFacilityName());
        assertEquals("code", obj.getUserCode());
        assertEquals("msg", obj.getMessage());
        assertEquals(2, obj.getRole());
    }
}