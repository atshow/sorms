package sf.database.jdbc.type;

import java.sql.*;

public class DoubleDOUBLEType implements TypeHandler<Double> {
    public Double get(ResultSet rs, String colName) throws SQLException {
        double re = rs.getDouble(colName);
        return rs.wasNull() ? null : re;
    }

    public Double get(ResultSet rs, int index) throws SQLException {
        double re = rs.getDouble(index);
        return rs.wasNull() ? null : re;
    }

    public Double get(CallableStatement rs, int index) throws SQLException {
        double re = rs.getDouble(index);
        return rs.wasNull() ? null : re;
    }

    @Override
    public Double get(CallableStatement cs, String parameterName) throws SQLException {
        double re = cs.getDouble(parameterName);
        return cs.wasNull() ? null : re;
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        rs.updateDouble(columnLabel, ((Number) value).doubleValue());
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        rs.updateDouble(columnIndex, ((Number) value).doubleValue());
    }


    public Object set(PreparedStatement stat, Object obj, int i) throws SQLException {
        if (null == obj) {
            stat.setNull(i, getSqlType());
        } else {
            stat.setDouble(i, ((Number) obj).doubleValue());
        }
        return null;
    }

    @Override
    public int getSqlType() {
        return Types.DOUBLE;
    }

    @Override
    public Class<Double> getDefaultJavaType() {
        return Double.class;
    }
}
