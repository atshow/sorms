package sf.database.jdbc.type;

import java.sql.*;

public class LongBIGINTType implements TypeHandler<Long> {
    public Long get(ResultSet rs, String colName) throws SQLException {
        long re = rs.getLong(colName);
        return rs.wasNull() ? null : re;
    }

    public Long get(ResultSet rs, int index) throws SQLException {
        long re = rs.getLong(index);
        return rs.wasNull() ? null : re;
    }

    @Override
    public Long get(CallableStatement cs, int index) throws SQLException {
        long re = cs.getLong(index);
        return cs.wasNull() ? null : re;
    }

    @Override
    public Long get(CallableStatement cs, String parameterName) throws SQLException {
        long re = cs.getLong(parameterName);
        return cs.wasNull() ? null : re;
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        rs.updateLong(columnLabel, ((Number) value).longValue());
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        rs.updateLong(columnIndex, ((Number) value).longValue());
    }

    public Object set(PreparedStatement stat, Object obj, int i) throws SQLException {
        if (null == obj) {
            stat.setNull(i, getSqlType());
        } else {
            long v;
            v = ((Number) obj).longValue();
            stat.setLong(i, v);
        }
        return null;
    }

    @Override
    public int getSqlType() {
        return Types.BIGINT;
    }

    @Override
    public Class<Long> getDefaultJavaType() {
        return Long.class;
    }
}
