package sf.database.jdbc.type;

import java.math.BigDecimal;
import java.sql.*;

public class BigDecimalDECIMALType implements TypeHandler<BigDecimal> {
    public BigDecimal get(ResultSet rs, String columnLabel) throws SQLException {
        return rs.getBigDecimal(columnLabel);
    }

    public BigDecimal get(ResultSet rs, int index) throws SQLException {
        return rs.getBigDecimal(index);
    }

    @Override
    public BigDecimal get(CallableStatement cs, int index) throws SQLException {
        return cs.getBigDecimal(index);
    }

    @Override
    public BigDecimal get(CallableStatement cs, String parameterName) throws SQLException {
        return cs.getBigDecimal(parameterName);
    }

    public Object set(PreparedStatement stat, Object obj, int i) throws SQLException {
        if (null == obj) {
            stat.setNull(i, getSqlType());
        } else {
            stat.setBigDecimal(i, (BigDecimal) obj);
        }
        return null;
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        rs.updateBigDecimal(columnLabel, (BigDecimal) value);
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        rs.updateBigDecimal(columnIndex, (BigDecimal) value);
    }

    @Override
    public int getSqlType() {
        return Types.DECIMAL;
    }

    @Override
    public Class<BigDecimal> getDefaultJavaType() {
        return BigDecimal.class;
    }
}
