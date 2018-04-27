package sf.database.jdbc.type;

import java.sql.*;

public class NullType implements TypeHandler {

    public Object get(ResultSet rs, String colName) throws SQLException {
        return null;
    }

    public Object get(ResultSet rs, int index) throws SQLException {
        return null;
    }

    @Override
    public Object get(CallableStatement cs, int index) throws SQLException {
        return null;
    }

    @Override
    public Object get(CallableStatement cs, String parameterName) throws SQLException {
        return null;
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        rs.updateNull(columnLabel);
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        rs.updateNull(columnIndex);
    }

    public Object set(PreparedStatement stat, Object obj, int i) throws SQLException {
        stat.setNull(i, Types.NULL);
        return null;
    }

    @Override
    public int getSqlType() {
        return Types.NULL;
    }

    @Override
    public Class<?> getDefaultJavaType() {
        return null;
    }
}
