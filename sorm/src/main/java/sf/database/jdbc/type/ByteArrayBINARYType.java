package sf.database.jdbc.type;

import java.sql.*;

public class ByteArrayBINARYType implements TypeHandler<byte[]> {

    public byte[] get(ResultSet rs, String colName) throws SQLException {
        return rs.getBytes(colName);
    }

    public byte[] get(ResultSet rs, int index) throws SQLException {
        return rs.getBytes(index);
    }

    public byte[] get(CallableStatement rs, int index) throws SQLException {
        return rs.getBytes(index);
    }

    @Override
    public byte[] get(CallableStatement cs, String parameterName) throws SQLException {
        return cs.getBytes(parameterName);
    }

    public Object set(PreparedStatement stat, Object obj, int index) throws SQLException {
        if (null == obj) {
            stat.setNull(index, getSqlType());
        } else {
            stat.setBytes(index, (byte[]) obj);
        }
        return null;
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        rs.updateBytes(columnLabel, (byte[]) value);
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        rs.updateBytes(columnIndex, (byte[]) value);
    }

    @Override
    public int getSqlType() {
        return Types.BINARY;
    }

    @Override
    public Class<byte[]> getDefaultJavaType() {
        return byte[].class;
    }
}
