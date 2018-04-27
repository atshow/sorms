package sf.database.jdbc.type;

import java.sql.*;

public class BooleanBITType implements TypeHandler<Boolean> {
    public Boolean get(ResultSet rs, String colName) throws SQLException {
        boolean re = rs.getBoolean(colName);
        return rs.wasNull() ? null : re;
    }

    public Boolean get(ResultSet rs, int index) throws SQLException {
        boolean re = rs.getBoolean(index);
        return rs.wasNull() ? null : re;
    }

    public Boolean get(CallableStatement cs, int index) throws SQLException {
        boolean re = cs.getBoolean(index);
        return cs.wasNull() ? null : re;
    }

    @Override
    public Boolean get(CallableStatement cs, String parameterName) throws SQLException {
        boolean re = cs.getBoolean(parameterName);
        return cs.wasNull() ? null : re;
    }

    public Object set(PreparedStatement stat, Object obj, int i) throws SQLException {
        if (null == obj) {
            stat.setNull(i, getSqlType());
        } else {
            stat.setBoolean(i, (Boolean) obj);
        }
        return null;
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        rs.updateBoolean(columnLabel, (Boolean) value);
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        rs.updateBoolean(columnIndex, (Boolean) value);
    }

    @Override
    public int getSqlType() {
        return Types.BIT;
    }

    @Override
    public Class<Boolean> getDefaultJavaType() {
        return Boolean.class;
    }

}
