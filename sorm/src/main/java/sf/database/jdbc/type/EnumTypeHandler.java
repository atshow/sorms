package sf.database.jdbc.type;

import sf.tools.StringUtils;

import java.sql.*;

public class EnumTypeHandler<E extends Enum<E>> implements TypeHandler<E> {
    private Class<E> type;

    public EnumTypeHandler(Class<E> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }

    @Override
    public E get(ResultSet rs, String columnName) throws SQLException {
        String s = rs.getString(columnName);
        return StringUtils.isBlank(s) ? null : Enum.valueOf(type, s);
    }

    @Override
    public E get(ResultSet rs, int index) throws SQLException {
        String s = rs.getString(index);
        return StringUtils.isBlank(s) ? null : Enum.valueOf(type, s);
    }

    @Override
    public E get(CallableStatement cs, int index) throws SQLException {
        String s = cs.getString(index);
        return StringUtils.isBlank(s) ? null : Enum.valueOf(type, s);
    }

    @Override
    public E get(CallableStatement cs, String parameterName) throws SQLException {
        String s = cs.getString(parameterName);
        return StringUtils.isBlank(s) ? null : Enum.valueOf(type, s);
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        rs.updateString(columnLabel, ((E) value).name());
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        rs.updateString(columnIndex, ((E) value).name());
    }

    @Override
    public Object set(PreparedStatement ps, Object obj, int i) throws SQLException {
        if (null == obj) {
            ps.setNull(i, getSqlType());
        } else {
            ps.setString(i, ((E) obj).name());
        }
        return null;
    }

    @Override
    public int getSqlType() {
        return Types.VARCHAR;
    }

    @Override
    public Class<E> getDefaultJavaType() {
        return type;
    }
}
