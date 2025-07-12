package udpm.hn.studentattendance.infrastructure.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class H2NamingStrategy extends PhysicalNamingStrategyStandardImpl {

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        if (name == null) {
            return null;
        }
        // Luôn trả về tên bảng với quote để đảm bảo giữ nguyên case
        return Identifier.toIdentifier(name.getText(), true);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        if (name == null) {
            return null;
        }
        // Luôn trả về tên cột với quote để đảm bảo giữ nguyên case
        return Identifier.toIdentifier(name.getText(), true);
    }
}