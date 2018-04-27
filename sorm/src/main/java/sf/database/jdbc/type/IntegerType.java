package sf.database.jdbc.type;

import java.sql.*;

public class IntegerType implements TypeHandler<Integer> {
    public Integer get(ResultSet rs, String colName) throws SQLException {
        int re = rs.getInt(colName);
        return rs.wasNull() ? null : re;
    }

    public Integer get(ResultSet rs, int index) throws SQLException {
        int re = rs.getInt(index);
        return rs.wasNull() ? null : re;
    }

    @Override
    public Integer get(CallableStatement cs, int index) throws SQLException {
        int re = cs.getInt(index);
        return cs.wasNull() ? null : re;
    }

    @Override
    public Integer get(CallableStatement cs, String parameterName) throws SQLException {
        int re = cs.getInt(parameterName);
        return cs.wasNull() ? null : re;
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        rs.updateInt(columnLabel, ((Number) value).intValue());
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        rs.updateInt(columnIndex, ((Number) value).intValue());
    }

    public Object set(PreparedStatement stat, Object obj, int i) throws SQLException {
        if (null == obj) {
            stat.setNull(i, getSqlType());
        } else {
            stat.setInt(i, ((Number) obj).intValue());
        }
        return null;
    }

    @Override
    public int getSqlType() {
        return Types.INTEGER;
    }

    @Override
    public Class<Integer> getDefaultJavaType() {
        return Integer.class;
    }
}
