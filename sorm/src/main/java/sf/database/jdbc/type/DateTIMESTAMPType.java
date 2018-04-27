package sf.database.jdbc.type;

import java.sql.*;
import java.util.Date;

public class DateTIMESTAMPType implements TypeHandler<Date> {
    public Date get(ResultSet rs, String colName) throws SQLException {
        return rs.getTimestamp(colName);
    }

    public Date get(ResultSet rs, int index) throws SQLException {
        return rs.getTimestamp(index);
    }

    public Date get(CallableStatement rs, int index) throws SQLException {
        return rs.getTimestamp(index);
    }

    @Override
    public Date get(CallableStatement cs, String parameterName) throws SQLException {
        return cs.getTimestamp(parameterName);
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        Timestamp v = new Timestamp(((Date) value).getTime());
        rs.updateTimestamp(columnLabel, v);
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        Timestamp v = new Timestamp(((Date) value).getTime());
        rs.updateTimestamp(columnIndex, v);
    }

    public Object set(PreparedStatement stat, Object obj, int i) throws SQLException {
        Timestamp v = null;
        if (null == obj) {
            stat.setNull(i, Types.TIMESTAMP);
        } else {
            v = new Timestamp(((Date) obj).getTime());
            stat.setTimestamp(i, v);
        }
        return null;
    }

    @Override
    public int getSqlType() {
        return Types.TIMESTAMP;
    }

    @Override
    public Class<Date> getDefaultJavaType() {
        return Date.class;
    }
}
