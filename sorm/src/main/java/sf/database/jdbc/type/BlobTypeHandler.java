package sf.database.jdbc.type;

import java.io.ByteArrayInputStream;
import java.sql.*;

public class BlobTypeHandler implements TypeHandler<byte[]> {
    String suffix;

    public BlobTypeHandler() {
        suffix = ".blob";
    }

    public byte[] get(ResultSet rs, String columnName) throws SQLException {
        Blob blob = rs.getBlob(columnName);
        byte[] returnValue = null;
        if (null != blob) {
            returnValue = blob.getBytes(1, (int) blob.length());
        }
        return returnValue;
    }

    public byte[] get(ResultSet rs, int index) throws SQLException {
        Blob blob = rs.getBlob(index);
        byte[] returnValue = null;
        if (null != blob) {
            returnValue = blob.getBytes(1, (int) blob.length());
        }
        return returnValue;
    }

    public byte[] get(CallableStatement rs, int index) throws SQLException {
        Blob blob = rs.getBlob(index);
        byte[] returnValue = null;
        if (null != blob) {
            returnValue = blob.getBytes(1, (int) blob.length());
        }
        return returnValue;
    }

    @Override
    public byte[] get(CallableStatement cs, String parameterName) throws SQLException {
        Blob blob = cs.getBlob(parameterName);
        byte[] returnValue = null;
        if (null != blob) {
            returnValue = blob.getBytes(1, (int) blob.length());
        }
        return returnValue;
    }

    public Object set(PreparedStatement ps, Object obj, int i) throws SQLException {
        if (null == obj) {
            ps.setNull(i, getSqlType());
        } else {
            byte[] bytes = (byte[]) obj;
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ps.setBinaryStream(i, bis, bytes.length);
        }
        return null;
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        byte[] bytes = (byte[]) value;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        rs.updateBinaryStream(columnLabel, bis, bytes.length);
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        byte[] bytes = (byte[]) value;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        rs.updateBinaryStream(columnIndex, bis, bytes.length);
    }

    public int getSqlType() {
        return Types.BLOB;
    }

    @Override
    public Class<byte[]> getDefaultJavaType() {
        return byte[].class;
    }

}