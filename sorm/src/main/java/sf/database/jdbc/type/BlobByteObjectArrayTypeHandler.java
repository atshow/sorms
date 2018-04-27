package sf.database.jdbc.type;

import sf.tools.ArrayUtils;

import java.io.ByteArrayInputStream;
import java.sql.*;

public class BlobByteObjectArrayTypeHandler implements TypeHandler<Byte[]> {

    @Override
    public Byte[] get(ResultSet rs, String columnName) throws SQLException {
        Blob blob = rs.getBlob(columnName);
        return getBytes(blob);
    }

    @Override
    public Byte[] get(ResultSet rs, int index) throws SQLException {
        Blob blob = rs.getBlob(index);
        return getBytes(blob);
    }

    @Override
    public Byte[] get(CallableStatement cs, int index) throws SQLException {
        Blob blob = cs.getBlob(index);
        return getBytes(blob);
    }

    @Override
    public Byte[] get(CallableStatement cs, String parameterName) throws SQLException {
        Blob blob = cs.getBlob(parameterName);
        return getBytes(blob);
    }

    @Override
    public Object set(PreparedStatement ps, Object obj, int index) throws SQLException {
        if (null == obj) {
            ps.setNull(index, getSqlType());
        } else {
            Byte[] bytes = (Byte[]) obj;
            ByteArrayInputStream bis = new ByteArrayInputStream(ArrayUtils.toPrimitive(bytes));
            ps.setBinaryStream(index, bis, bytes.length);
        }
        return null;
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        Byte[] bytes = (Byte[]) value;
        ByteArrayInputStream bis = new ByteArrayInputStream(ArrayUtils.toPrimitive(bytes));
        rs.updateBinaryStream(columnLabel, bis, bytes.length);
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        Byte[] bytes = (Byte[]) value;
        ByteArrayInputStream bis = new ByteArrayInputStream(ArrayUtils.toPrimitive(bytes));
        rs.updateBinaryStream(columnIndex, bis, bytes.length);
    }

    @Override
    public Class<Byte[]> getDefaultJavaType() {
        return Byte[].class;
    }

    @Override
    public int getSqlType() {
        return Types.BLOB;
    }

    private Byte[] getBytes(Blob blob) throws SQLException {
        Byte[] returnValue = null;
        if (blob != null) {
            returnValue = ArrayUtils.toObject(blob.getBytes(1, (int) blob.length()));
        }
        return returnValue;
    }

}
