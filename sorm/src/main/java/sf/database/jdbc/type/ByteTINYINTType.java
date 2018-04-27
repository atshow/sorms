package sf.database.jdbc.type;

import java.sql.*;

public class ByteTINYINTType implements TypeHandler<Byte> {
    public Byte get(ResultSet rs, String colName) throws SQLException {
        byte re = rs.getByte(colName);
        return rs.wasNull() ? null : re;
    }

    public Byte get(ResultSet rs, int index) throws SQLException {
        byte re = rs.getByte(index);
        return rs.wasNull() ? null : re;
    }

    @Override
    public Byte get(CallableStatement cs, int index) throws SQLException {
        byte re = cs.getByte(index);
        return cs.wasNull() ? null : re;
    }

    @Override
    public Byte get(CallableStatement cs, String parameterName) throws SQLException {
        byte re = cs.getByte(parameterName);
        return cs.wasNull() ? null : re;
    }

    public Object set(PreparedStatement stat, Object obj, int i) throws SQLException {
        if (null == obj) {
            stat.setNull(i, getSqlType());
        } else {
            byte v;
            if (obj instanceof Number)
                v = ((Number) obj).byteValue();
            else
                v = Byte.valueOf(obj.toString());
            stat.setByte(i, v);
        }
        return null;
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        byte v;
        if (value instanceof Number)
            v = ((Number) value).byteValue();
        else
            v = Byte.valueOf(value.toString());
        rs.updateByte(columnLabel, v);
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        byte v;
        if (value instanceof Number)
            v = ((Number) value).byteValue();
        else
            v = Byte.valueOf(value.toString());
        rs.updateByte(columnIndex, v);
    }

    @Override
    public int getSqlType() {
        return Types.TINYINT;
    }

    @Override
    public Class<Byte> getDefaultJavaType() {
        return Byte.class;
    }
}
