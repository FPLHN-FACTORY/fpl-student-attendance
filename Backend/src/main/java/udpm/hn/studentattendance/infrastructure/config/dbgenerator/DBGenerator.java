package udpm.hn.studentattendance.infrastructure.config.dbgenerator;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import udpm.hn.studentattendance.entities.Facility;
import udpm.hn.studentattendance.entities.Role;
import udpm.hn.studentattendance.entities.UserAdmin;
import udpm.hn.studentattendance.entities.UserStaff;
import udpm.hn.studentattendance.entities.UserStudent;
import udpm.hn.studentattendance.infrastructure.config.dbgenerator.repositories.DBGeneratorFacilityRepository;
import udpm.hn.studentattendance.infrastructure.config.dbgenerator.repositories.DBGeneratorRoleRepository;
import udpm.hn.studentattendance.infrastructure.config.dbgenerator.repositories.DBGeneratorUserAdminRepository;
import udpm.hn.studentattendance.infrastructure.config.dbgenerator.repositories.DBGeneratorUserStaffRepository;
import udpm.hn.studentattendance.infrastructure.config.dbgenerator.repositories.DBGeneratorUserStudentRepository;
import udpm.hn.studentattendance.infrastructure.constants.RoleConstant;
import udpm.hn.studentattendance.utils.CodeGeneratorUtils;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class DBGenerator {

    private final DBGeneratorUserAdminRepository dbGeneratorUserAdminRepository;

    private final DBGeneratorUserStaffRepository dbGeneratorUserStaffRepository;

    private final DBGeneratorUserStudentRepository dbGeneratorUserStudentRepository;

    private final DBGeneratorFacilityRepository dbGeneratorFacilityRepository;

    private final DBGeneratorRoleRepository dbGeneratorRoleRepository;


    @Value("${db.fake.isGenerated}")
    private String isGenerated;

    @Value("${db.fake-user-name}")
    private String userName;

    @Value("${db.fake-user-code}")
    private String userCode;

    @Value("${db.fake-user-email}")
    private String userEmail;

    @Value("${db.fake-facility}")
    private String facilityName;

    private Facility facility;

    @PostConstruct
    public void init() {
        if (isGenerated.equals("true")) {
            if (StringUtils.hasText(facilityName)) {
                generateFacility();
            }
            generateUserAdmin();
            //generateUserStaff();
            //generateUserStudent();
        }
    }

    private void generateFacility() {
        String[] list = facilityName.split(",");
        for (String name : list) {
            String code = CodeGeneratorUtils.generateCodeFromString(name);
            Facility getFacility = dbGeneratorFacilityRepository.findByCode(code).orElse(null);

            if (Objects.isNull(getFacility)) {
                Facility dataFacility = new Facility();
                dataFacility.setName(name);
                dataFacility.setCode(code);
                dataFacility.setPosition(dbGeneratorFacilityRepository.getLastPosition() + 1);

                Facility saveFacility = dbGeneratorFacilityRepository.save(dataFacility);

                if (facility == null) {
                    facility = saveFacility;
                }

            } else {

                if (facility == null) {
                    facility = getFacility;
                }

            }

        }
    }

    private void generateUserAdmin() {

        if (!StringUtils.hasText(userName) || !StringUtils.hasText(userCode) || !StringUtils.hasText(userEmail)) {
            return;
        }

        UserAdmin user = dbGeneratorUserAdminRepository.findByEmail(userEmail).orElse(null);

        if (Objects.isNull(user)) {
            UserAdmin dataUser = new UserAdmin();
            dataUser.setName(userName);
            dataUser.setCode(userCode);
            dataUser.setEmail(userEmail);

            dbGeneratorUserAdminRepository.save(dataUser);
        }

    }


    private void generateUserStaff() {

        if (!StringUtils.hasText(userName) || !StringUtils.hasText(userCode) || !StringUtils.hasText(userEmail)) {
            return;
        }

        UserStaff user = dbGeneratorUserStaffRepository.findByEmailFpt(userEmail).orElse(null);

        if (Objects.isNull(user)) {
            UserStaff dataUser = new UserStaff();
            dataUser.setName(userName);
            dataUser.setCode(userCode);
            dataUser.setEmailFe(userEmail);
            dataUser.setEmailFpt(userEmail);

            UserStaff saveUser = dbGeneratorUserStaffRepository.save(dataUser);

            if (saveUser.getId() != null && facility != null) {
                Role role = new Role();
                role.setCode(RoleConstant.STAFF);
                role.setUserStaff(saveUser);
                role.setFacility(facility);

                Role role2 = new Role();
                role2.setCode(RoleConstant.TEACHER);
                role2.setUserStaff(saveUser);
                role2.setFacility(facility);

                dbGeneratorRoleRepository.save(role);
                dbGeneratorRoleRepository.save(role2);
            }
        }

    }

    private void generateUserStudent() {

        if (!StringUtils.hasText(userName) || !StringUtils.hasText(userCode) || !StringUtils.hasText(userEmail)) {
            return;
        }

        UserStudent user = dbGeneratorUserStudentRepository.findByEmail(userEmail).orElse(null);

        if (Objects.isNull(user) && facility != null) {
            UserStudent dataUser = new UserStudent();
            dataUser.setName(userName);
            dataUser.setCode(userCode);
            dataUser.setEmail(userEmail);
            dataUser.setFacility(facility);

            dbGeneratorUserStudentRepository.save(dataUser);
        }

    }

}
