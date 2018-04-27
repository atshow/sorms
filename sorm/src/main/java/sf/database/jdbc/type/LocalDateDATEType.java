package sf.database.jdbc.type;

import java.sql.*;
import java.time.LocalDate;

public class LocalDateDATEType implements TypeHandler<LocalDate> {

    public LocalDate get(ResultSet rs, String columnName) throws SQLException {
        Date date = rs.getDate(columnName);
        return getLocalDate(date);
    }

    public LocalDate get(ResultSet rs, int index) throws SQLException {
        Date date = rs.getDate(index);
        return getLocalDate(date);
    }

    public LocalDate get(CallableStatement cs, int index) throws SQLException {
        Date date = cs.getDate(index);
        return getLocalDate(date);
    }

    @Override
    public LocalDate get(CallableStatement cs, String parameterName) throws SQLException {
        Date date = cs.getDate(parameterName);
        return getLocalDate(date);
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        rs.updateDate(columnLabel, Date.valueOf((LocalDate) value));
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        rs.updateDate(columnIndex, Date.valueOf((LocalDate) value));
    }

    public Object set(PreparedStatement stat, Object obj, int i) throws SQLException {
        if (null == obj) {
            stat.setNull(i, getSqlType());
        } else {
            stat.setDate(i, Date.valueOf((LocalDate) obj));
        }
        return null;
    }

    @Override
    public int getSqlType() {
        return Types.DATE;
    }

    @Override
    public Class<LocalDate> getDefaultJavaType() {
        return LocalDate.class;
    }


    private static LocalDate getLocalDate(Date date) {
        if (date != null) {
            return date.toLocalDate();
        }
        return null;
    }
}
