package sf.database.jdbc.type;

import java.sql.*;

public class StringVARCHARType implements TypeHandler<String> {
    public String get(ResultSet rs, String colName) throws SQLException {
        return rs.getString(colName);
    }

    public String get(ResultSet rs, int index) throws SQLException {
        return rs.getString(index);
    }

    @Override
    public String get(CallableStatement cs, int index) throws SQLException {
        return cs.getString(index);
    }

    @Override
    public String get(CallableStatement cs, String parameterName) throws SQLException {
        return cs.getString(parameterName);
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        rs.updateString(columnLabel, (String) value);
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        rs.updateString(columnIndex, (String) value);
    }

    public Object set(PreparedStatement stat, Object obj, int i) throws SQLException {
        if (null == obj) {
            stat.setNull(i, getSqlType());
        } else {
            stat.setString(i, (String) obj);
        }
        return null;
    }

    @Override
    public int getSqlType() {
        return Types.VARCHAR;
    }

    @Override
    public Class<String> getDefaultJavaType() {
        return String.class;
    }
}
