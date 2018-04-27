package sf.database.jdbc.type;

import java.sql.*;

public class StringSQLXMLType implements TypeHandler<String> {
    public String get(ResultSet rs, String colName) throws SQLException {
        SQLXML xml = rs.getSQLXML(colName);
        return xml.toString();
    }

    public String get(ResultSet rs, int index) throws SQLException {
        SQLXML xml = rs.getSQLXML(index);
        return xml.toString();
    }

    public String get(CallableStatement rs, int index) throws SQLException {
        SQLXML xml = rs.getSQLXML(index);
        return xml.toString();
    }

    @Override
    public String get(CallableStatement cs, String parameterName) throws SQLException {
        SQLXML xml = cs.getSQLXML(parameterName);
        return xml.toString();
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        rs.updateSQLXML(columnLabel, (SQLXML) value);
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        rs.updateSQLXML(columnIndex, (SQLXML) value);
    }

    public Object set(PreparedStatement stat, Object obj, int index) throws SQLException {
        if (null == obj) {
            stat.setNull(index, getSqlType());
        } else {
            SQLXML xml = stat.getConnection().createSQLXML();
            xml.setString(String.valueOf(obj));
            stat.setSQLXML(index, xml);
        }
        return null;
    }

    @Override
    public int getSqlType() {
        return Types.SQLXML;
    }

    @Override
    public Class<String> getDefaultJavaType() {
        return String.class;
    }
}
