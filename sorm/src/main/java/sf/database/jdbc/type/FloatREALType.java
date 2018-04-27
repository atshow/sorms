package sf.database.jdbc.type;

import java.sql.*;

public class FloatREALType implements TypeHandler<Float> {
    public Float get(ResultSet rs, String colName) throws SQLException {
        float re = rs.getFloat(colName);
        return rs.wasNull() ? null : re;
    }

    public Float get(ResultSet rs, int index) throws SQLException {
        float re = rs.getFloat(index);
        return rs.wasNull() ? null : re;
    }

    public Float get(CallableStatement rs, int index) throws SQLException {
        float re = rs.getFloat(index);
        return rs.wasNull() ? null : re;
    }

    @Override
    public Float get(CallableStatement cs, String parameterName) throws SQLException {
        float re = cs.getFloat(parameterName);
        return cs.wasNull() ? null : re;
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        rs.updateFloat(columnLabel, ((Number) value).floatValue());
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        rs.updateFloat(columnIndex, ((Number) value).floatValue());
    }

    public Object set(PreparedStatement stat, Object obj, int i) throws SQLException {
        if (null == obj) {
            stat.setNull(i, getSqlType());
        } else {
            stat.setFloat(i, ((Number) obj).floatValue());
        }
        return null;
    }

    @Override
    public Class<Float> getDefaultJavaType() {
        return Float.class;
    }

    @Override
    public int getSqlType() {
        return Types.REAL;
    }
}
