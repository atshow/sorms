package sf.database.jdbc.type;

import java.sql.*;

public class ShortSMALLINTType implements TypeHandler<Short> {
    public Short get(ResultSet rs, String colName) throws SQLException {
        short re = rs.getShort(colName);
        return rs.wasNull() ? null : re;
    }

    public Short get(ResultSet rs, int index) throws SQLException {
        short re = rs.getShort(index);
        return rs.wasNull() ? null : re;
    }

    @Override
    public Short get(CallableStatement cs, int index) throws SQLException {
        short re = cs.getShort(index);
        return cs.wasNull() ? null : re;
    }

    @Override
    public Short get(CallableStatement cs, String parameterName) throws SQLException {
        short re = cs.getShort(parameterName);
        return cs.wasNull() ? null : re;
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        rs.updateShort(columnLabel, ((Number) value).shortValue());
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        rs.updateShort(columnIndex, ((Number) value).shortValue());
    }

    public Object set(PreparedStatement stat, Object obj, int i) throws SQLException {
        if (null == obj) {
            stat.setNull(i, getSqlType());
        } else {
            stat.setShort(i, ((Number) obj).shortValue());
        }
        return null;
    }

    @Override
    public int getSqlType() {
        return Types.SMALLINT;
    }

    @Override
    public Class<Short> getDefaultJavaType() {
        return Short.class;
    }
}
