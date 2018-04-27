package sf.database.jdbc.type;

import java.sql.*;
import java.time.LocalDateTime;

public class LocalDateTimeTIMESTAMPType implements TypeHandler<LocalDateTime> {
    public LocalDateTime get(ResultSet rs, String columnName) throws SQLException {
        Timestamp date = rs.getTimestamp(columnName);
        return getLocalDateTime(date);
    }

    public LocalDateTime get(ResultSet rs, int index) throws SQLException {
        Timestamp date = rs.getTimestamp(index);
        return getLocalDateTime(date);
    }

    public LocalDateTime get(CallableStatement rs, int index) throws SQLException {
        Timestamp date = rs.getTimestamp(index);
        return getLocalDateTime(date);
    }

    @Override
    public LocalDateTime get(CallableStatement cs, String parameterName) throws SQLException {
        Timestamp date = cs.getTimestamp(parameterName);
        return getLocalDateTime(date);
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        rs.updateTimestamp(columnLabel, Timestamp.valueOf((LocalDateTime) value));
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        rs.updateTimestamp(columnIndex, Timestamp.valueOf((LocalDateTime) value));
    }

    public Object set(PreparedStatement stat, Object obj, int i) throws SQLException {
        if (null == obj) {
            stat.setNull(i, getSqlType());
        } else {
            stat.setTimestamp(i, Timestamp.valueOf((LocalDateTime) obj));
        }
        return null;
    }

    @Override
    public int getSqlType() {
        return Types.TIMESTAMP;
    }

    @Override
    public Class<LocalDateTime> getDefaultJavaType() {
        return LocalDateTime.class;
    }


    private static LocalDateTime getLocalDateTime(Timestamp timestamp) {
        if (timestamp != null) {
            return timestamp.toLocalDateTime();
        }
        return null;
    }
}
