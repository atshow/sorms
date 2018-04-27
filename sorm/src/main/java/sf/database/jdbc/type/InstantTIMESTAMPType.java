package sf.database.jdbc.type;

import java.sql.*;
import java.time.Instant;

public class InstantTIMESTAMPType implements TypeHandler<Instant> {
    public Instant get(ResultSet rs, String columnName) throws SQLException {
        Timestamp date = rs.getTimestamp(columnName);
        return getInstant(date);
    }

    public Instant get(ResultSet rs, int index) throws SQLException {
        Timestamp date = rs.getTimestamp(index);
        return getInstant(date);
    }

    public Instant get(CallableStatement rs, int index) throws SQLException {
        Timestamp date = rs.getTimestamp(index);
        return getInstant(date);
    }

    @Override
    public Instant get(CallableStatement cs, String parameterName) throws SQLException {
        Timestamp date = cs.getTimestamp(parameterName);
        return getInstant(date);
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        rs.updateTimestamp(columnLabel, Timestamp.from((Instant) value));
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        rs.updateTimestamp(columnIndex, Timestamp.from((Instant) value));
    }

    public Object set(PreparedStatement stat, Object obj, int i) throws SQLException {
        if (null == obj) {
            stat.setNull(i, getSqlType());
        } else {
            stat.setTimestamp(i, Timestamp.from((Instant) obj));
        }
        return null;
    }

    @Override
    public int getSqlType() {
        return Types.TIMESTAMP;
    }

    @Override
    public Class<Instant> getDefaultJavaType() {
        return Instant.class;
    }


    private static Instant getInstant(Timestamp timestamp) {
        if (timestamp != null) {
            return timestamp.toInstant();
        }
        return null;
    }
}
