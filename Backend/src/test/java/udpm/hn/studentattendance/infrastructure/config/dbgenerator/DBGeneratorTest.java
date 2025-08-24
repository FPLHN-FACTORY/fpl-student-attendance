package udpm.hn.studentattendance.infrastructure.config.dbgenerator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import udpm.hn.studentattendance.helpers.SettingHelper;
import udpm.hn.studentattendance.infrastructure.config.dbgenerator.repositories.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DBGeneratorTest {
    @Mock
    private DBGeneratorUserAdminRepository dbGeneratorUserAdminRepository;

    @Mock
    private DBGeneratorUserStaffRepository dbGeneratorUserStaffRepository;

    @Mock
    private DBGeneratorUserStudentRepository dbGeneratorUserStudentRepository;

    @Mock
    private DBGeneratorFacilityRepository dbGeneratorFacilityRepository;

    @Mock
    private DBGeneratorRoleRepository dbGeneratorRoleRepository;

    @Mock
    private SettingHelper settingHelper;

    @Test
    void testClassExists() {
        assertNotNull(new DBGenerator(
                dbGeneratorUserAdminRepository,
                dbGeneratorUserStaffRepository,
                dbGeneratorUserStudentRepository,
                dbGeneratorFacilityRepository,
                dbGeneratorRoleRepository,
                settingHelper));
    }
}
