package sf.database.jdbc.type;

import java.sql.*;
import java.util.Date;

public class DateDATEType implements TypeHandler<Date> {
    public Date get(ResultSet rs, String colName) throws SQLException {
        return rs.getDate(colName);
    }

    public Date get(ResultSet rs, int index) throws SQLException {
        return rs.getDate(index);
    }

    public Date get(CallableStatement rs, int index) throws SQLException {
        return rs.getDate(index);
    }

    @Override
    public Date get(CallableStatement cs, String parameterName) throws SQLException {
        return cs.getDate(parameterName);
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        java.sql.Date v = null;
        if (value instanceof Date) {
            v = new java.sql.Date(((Date) value).getTime());
        }
        rs.updateDate(columnLabel, v);
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        java.sql.Date v = null;
        if (value instanceof Date) {
            v = new java.sql.Date(((Date) value).getTime());
        }
        rs.updateDate(columnIndex, v);
    }

    public Object set(PreparedStatement stat, Object obj, int i) throws SQLException {
        if (null == obj) {
            stat.setNull(i, Types.DATE);
        } else {
            java.sql.Date v = null;
            if (obj instanceof Date) {
                v = new java.sql.Date(((Date) obj).getTime());
            }
            stat.setDate(i, v);
        }
        return null;
    }

    @Override
    public int getSqlType() {
        return Types.DATE;
    }

    @Override
    public Class<Date> getDefaultJavaType() {
        return Date.class;
    }
}
