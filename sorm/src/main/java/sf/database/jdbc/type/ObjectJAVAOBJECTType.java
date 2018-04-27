package sf.database.jdbc.type;

import java.sql.*;

public class ObjectJAVAOBJECTType implements TypeHandler<Object> {
    public Object get(ResultSet rs, String colName) throws SQLException {
        return rs.getObject(colName);
    }

    public Object get(ResultSet rs, int index) throws SQLException {
        return rs.getObject(index);
    }

    public Object get(CallableStatement rs, int index) throws SQLException {
        return rs.getObject(index);
    }

    @Override
    public Object get(CallableStatement cs, String parameterName) throws SQLException {
        return cs.getObject(parameterName);
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        rs.updateObject(columnLabel, value);
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        rs.updateObject(columnIndex, value);
    }

    public Object set(PreparedStatement stat, Object obj, int i) throws SQLException {
        stat.setObject(i, obj);
        return null;
    }

    @Override
    public int getSqlType() {
        return Types.JAVA_OBJECT;
    }

    @Override
    public Class<Object> getDefaultJavaType() {
        return Object.class;
    }
}
