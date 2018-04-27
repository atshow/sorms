package sf.database.jdbc.type;

import java.sql.*;

public class ObjectARRAYType implements TypeHandler<Object> {

    @Override
    public Object get(ResultSet rs, String columnName) throws SQLException {
        Array array = rs.getArray(columnName);
        return array == null ? null : array.getArray();
    }

    @Override
    public Object get(ResultSet rs, int index) throws SQLException {
        Array array = rs.getArray(index);
        return array == null ? null : array.getArray();
    }

    @Override
    public Object get(CallableStatement cs, int index) throws SQLException {
        Array array = cs.getArray(index);
        return array == null ? null : array.getArray();
    }

    @Override
    public Object get(CallableStatement cs, String parameterName) throws SQLException {
        Array array = cs.getArray(parameterName);
        return array == null ? null : array.getArray();
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        rs.updateArray(columnLabel, (Array) value);
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        rs.updateArray(columnIndex, (Array) value);
    }

    @Override
    public Object set(PreparedStatement ps, Object obj, int index) throws SQLException {
        if (null == obj) {
            ps.setNull(index, getSqlType());
        } else {
            ps.setArray(index, (Array) obj);
        }
        return null;
    }

    @Override
    public Class<Object> getDefaultJavaType() {
        return Object.class;
    }

    @Override
    public int getSqlType() {
        return Types.ARRAY;
    }
}
