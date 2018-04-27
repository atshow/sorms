package sf.database.jdbc.type;

import sf.tools.IOUtils;

import java.io.*;
import java.sql.*;

public class InputStreamBINARYType implements TypeHandler<InputStream> {

    public InputStream get(ResultSet rs, String colName) throws SQLException {
        InputStream in = rs.getBinaryStream(colName);
        if (in == null) {
            return in;
        }
        try {
            File f = File.createTempFile(".tmp", ".io");
            IOUtils.saveAsFile(f, in);
            in.close();
            return new FileInputStream(f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream get(ResultSet rs, int index) throws SQLException {
        InputStream in = rs.getBinaryStream(index);
        if (in == null) {
            return in;
        }
        try {
            File f = File.createTempFile(".tmp", ".io");
            IOUtils.saveAsFile(f, in);
            in.close();
            return new FileInputStream(f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream get(CallableStatement rs, int index) throws SQLException {
        return null;
    }

    @Override
    public InputStream get(CallableStatement cs, String parameterName) throws SQLException {
        return null;
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        if (value instanceof ByteArrayInputStream) {
            rs.updateBinaryStream(columnLabel, (InputStream) value, ((ByteArrayInputStream) value).available());
        } else if (value instanceof InputStream) {
            try {
                rs.updateBinaryStream(columnLabel, (InputStream) value);
            } catch (SQLException e) {
                throw e;
            }
        }
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        if (value instanceof ByteArrayInputStream) {
            rs.updateBinaryStream(columnIndex, (InputStream) value, ((ByteArrayInputStream) value).available());
        } else if (value instanceof InputStream) {
            try {
                rs.updateBinaryStream(columnIndex, (InputStream) value);
            } catch (SQLException e) {
                throw e;
            }
        }
    }

    public Object set(PreparedStatement stat, Object obj, int index) throws SQLException {
        if (null == obj) {
            stat.setNull(index, getSqlType());
        } else {

            if (obj instanceof ByteArrayInputStream) {
                stat.setBinaryStream(index, (InputStream) obj, ((ByteArrayInputStream) obj).available());
            } else if (obj instanceof InputStream) {
                try {
                    stat.setBinaryStream(index, (InputStream) obj);
                } catch (SQLException e) {
                    throw e;
                }
            }
        }
        return null;
    }

    @Override
    public int getSqlType() {
        return Types.BINARY;
    }

    @Override
    public Class<InputStream> getDefaultJavaType() {
        return InputStream.class;
    }
}
