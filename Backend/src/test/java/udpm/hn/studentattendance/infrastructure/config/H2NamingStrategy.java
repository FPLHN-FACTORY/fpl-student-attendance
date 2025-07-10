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
        // Giữ nguyên tên bảng, không chuyển thành uppercase
        return Identifier.toIdentifier(name.getText(), name.isQuoted());
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        if (name == null) {
            return null;
        }
        // Giữ nguyên tên cột, không chuyển thành uppercase
        return Identifier.toIdentifier(name.getText(), name.isQuoted());
    }
}