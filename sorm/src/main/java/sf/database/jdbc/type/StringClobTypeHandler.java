package sf.database.jdbc.type;

import java.io.StringReader;
import java.sql.*;

public class StringClobTypeHandler implements TypeHandler<String> {
    String suffix;

    public StringClobTypeHandler() {
        suffix = ".clob";
    }

    public String get(ResultSet rs, String colName) throws SQLException {
        String value = "";
        Clob clob = rs.getClob(colName);
        if (clob != null) {
            int size = (int) clob.length();
            value = clob.getSubString(1, size);
        }
        return value;
    }

    public String get(ResultSet rs, int index) throws SQLException {
        String value = "";
        Clob clob = rs.getClob(index);
        if (clob != null) {
            int size = (int) clob.length();
            value = clob.getSubString(1, size);
        }
        return value;
    }

    public String get(CallableStatement rs, int index) throws SQLException {
        String value = "";
        Clob clob = rs.getClob(index);
        if (clob != null) {
            int size = (int) clob.length();
            value = clob.getSubString(1, size);
        }
        return value;
    }

    @Override
    public String get(CallableStatement cs, String parameterName) throws SQLException {
        String value = "";
        Clob clob = cs.getClob(parameterName);
        if (clob != null) {
            int size = (int) clob.length();
            value = clob.getSubString(1, size);
        }
        return value;
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        StringReader reader = new StringReader((String) value);
        rs.updateCharacterStream(columnLabel, reader, ((String) value).length());
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        StringReader reader = new StringReader((String) value);
        rs.updateCharacterStream(columnIndex, reader, ((String) value).length());
    }

    public Object set(PreparedStatement ps, Object obj, int i) throws SQLException {
        if (null == obj) {
            ps.setNull(i, getSqlType());
        } else {
            StringReader reader = new StringReader((String) obj);
            ps.setCharacterStream(i, reader, ((String) obj).length());
        }
        return null;
    }

    @Override
    public int getSqlType() {
        return Types.CLOB;
    }

    @Override
    public Class<String> getDefaultJavaType() {
        return String.class;
    }
}