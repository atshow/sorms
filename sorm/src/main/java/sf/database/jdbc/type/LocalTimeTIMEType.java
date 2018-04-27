package sf.database.jdbc.type;

import java.sql.*;
import java.time.LocalTime;

public class LocalTimeTIMEType implements TypeHandler<LocalTime> {
    public LocalTime get(ResultSet rs, String columnName) throws SQLException {
        Time date = rs.getTime(columnName);
        return getLocalTime(date);
    }

    public LocalTime get(ResultSet rs, int index) throws SQLException {
        Time date = rs.getTime(index);
        return getLocalTime(date);
    }

    public LocalTime get(CallableStatement rs, int index) throws SQLException {
        Time date = rs.getTime(index);
        return getLocalTime(date);
    }

    @Override
    public LocalTime get(CallableStatement cs, String parameterName) throws SQLException {
        Time date = cs.getTime(parameterName);
        return getLocalTime(date);
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        rs.updateTime(columnLabel, Time.valueOf((LocalTime) value));
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        rs.updateTime(columnIndex, Time.valueOf((LocalTime) value));
    }

    public Object set(PreparedStatement stat, Object obj, int i) throws SQLException {
        if (null == obj) {
            stat.setNull(i, getSqlType());
        } else {
            stat.setTime(i, Time.valueOf((LocalTime) obj));
        }
        return null;
    }

    @Override
    public int getSqlType() {
        return Types.TIME;
    }

    @Override
    public Class<LocalTime> getDefaultJavaType() {
        return LocalTime.class;
    }


    private static LocalTime getLocalTime(Time time) {
        if (time != null) {
            return time.toLocalTime();
        }
        return null;
    }
}
