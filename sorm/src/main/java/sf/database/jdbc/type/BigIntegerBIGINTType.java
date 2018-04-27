package sf.database.jdbc.type;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;

public class BigIntegerBIGINTType implements TypeHandler<BigInteger> {
    public BigInteger get(ResultSet rs, String colName) throws SQLException {
        BigDecimal bigDecimal = rs.getBigDecimal(colName);
        return bigDecimal == null ? null : bigDecimal.toBigInteger();
    }

    public BigInteger get(ResultSet rs, int index) throws SQLException {
        BigDecimal bigDecimal = rs.getBigDecimal(index);
        return bigDecimal == null ? null : bigDecimal.toBigInteger();
    }

    @Override
    public BigInteger get(CallableStatement cs, int index) throws SQLException {
        BigDecimal bigDecimal = cs.getBigDecimal(index);
        return bigDecimal == null ? null : bigDecimal.toBigInteger();
    }

    @Override
    public BigInteger get(CallableStatement cs, String parameterName) throws SQLException {
        BigDecimal bigDecimal = cs.getBigDecimal(parameterName);
        return bigDecimal == null ? null : bigDecimal.toBigInteger();
    }

    public Object set(PreparedStatement stat, Object obj, int i) throws SQLException {
        if (null == obj) {
            stat.setNull(i, getSqlType());
        } else {
            stat.setBigDecimal(i, new BigDecimal((BigInteger) obj));
        }
        return null;
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        rs.updateBigDecimal(columnLabel, new BigDecimal((BigInteger) value));
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        rs.updateBigDecimal(columnIndex, new BigDecimal((BigInteger) value));
    }

    @Override
    public int getSqlType() {
        return Types.BIGINT;
    }

    @Override
    public Class<BigInteger> getDefaultJavaType() {
        return BigInteger.class;
    }
}
