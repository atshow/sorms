package db.domain;

import sf.database.jdbc.sql.IdentifierGenerator;
import sf.database.meta.ColumnMapping;

import java.sql.Connection;
import java.sql.SQLException;

public class AtCustomKey implements IdentifierGenerator {
    @Override
    public Object generate(Connection conn, ColumnMapping columnMapping) throws SQLException {
        return ((Number)(Math.random()*100000)).longValue();
    }
}
