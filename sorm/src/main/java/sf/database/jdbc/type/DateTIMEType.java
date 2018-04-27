package sf.database.jdbc.type;

import java.sql.*;
import java.util.Date;

public class DateTIMEType implements TypeHandler<Date> {
    public Date get(ResultSet rs, String colName) throws SQLException {
        return rs.getTime(colName);
    }

    public Date get(ResultSet rs, int index) throws SQLException {
        return rs.getTime(index);
    }

    public Date get(CallableStatement rs, int index) throws SQLException {
        return rs.getTime(index);
    }

    @Override
    public Date get(CallableStatement cs, String parameterName) throws SQLException {
        return cs.getTime(parameterName);
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        java.sql.Time v = new java.sql.Time(((Date) value).getTime());
        rs.updateTime(columnLabel, v);
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        java.sql.Time v = new java.sql.Time(((Date) value).getTime());
        rs.updateTime(columnIndex, v);
    }

    public Object set(PreparedStatement stat, Object obj, int i) throws SQLException {
        java.sql.Time v = null;
        if (null == obj) {
            stat.setNull(i, Types.TIME);
        } else {
            v = new java.sql.Time(((Date) obj).getTime());
            stat.setTime(i, v);
        }
        return null;
    }

    @Override
    public int getSqlType() {
        return Types.TIME;
    }

    @Override
    public Class<Date> getDefaultJavaType() {
        return Date.class;
    }
}
