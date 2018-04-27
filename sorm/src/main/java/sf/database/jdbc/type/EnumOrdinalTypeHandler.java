package sf.database.jdbc.type;

import java.sql.*;

public class EnumOrdinalTypeHandler<E extends Enum<E>> implements TypeHandler<E> {
    private Class<E> type;
    private final E[] enums;

    public EnumOrdinalTypeHandler(Class<E> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
        this.enums = type.getEnumConstants();
        if (this.enums == null) {
            throw new IllegalArgumentException(type.getSimpleName() + " does not represent an enum type.");
        }
    }

    @Override
    public E get(ResultSet rs, String columnName) throws SQLException {
        int i = rs.getInt(columnName);
        if (rs.wasNull()) {
            return null;
        } else {
            return getEnum(i);
        }
    }


    @Override
    public E get(ResultSet rs, int index) throws SQLException {
        int i = rs.getInt(index);
        if (rs.wasNull()) {
            return null;
        } else {
            return getEnum(i);
        }
    }

    @Override
    public E get(CallableStatement cs, int index) throws SQLException {
        int i = cs.getInt(index);
        if (cs.wasNull()) {
            return null;
        } else {
            return getEnum(i);
        }
    }

    @Override
    public E get(CallableStatement cs, String parameterName) throws SQLException {
        int i = cs.getInt(parameterName);
        if (cs.wasNull()) {
            return null;
        } else {
            return getEnum(i);
        }
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        rs.updateInt(columnLabel, ((E) value).ordinal());
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        rs.updateInt(columnIndex, ((E) value).ordinal());
    }

    @Override
    public Object set(PreparedStatement ps, Object obj, int index) throws SQLException {
        if (null == obj) {
            ps.setNull(index, getSqlType());
        } else {
            ps.setInt(index, ((E) obj).ordinal());
        }
        return null;
    }

    @Override
    public int getSqlType() {
        return Types.INTEGER;
    }

    @Override
    public Class<E> getDefaultJavaType() {
        return type;
    }


    private E getEnum(int i) {
        try {
            return enums[i];
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Cannot convert " + i + " to " + type.getSimpleName() + " by ordinal value.", ex);
        }
    }
}
