package sf.database.jdbc.type;

import java.io.Reader;
import java.sql.*;

public class ReaderBINARYType implements TypeHandler<Reader> {

    public Reader get(ResultSet rs, String colName) throws SQLException {
        return rs.getCharacterStream(colName);
    }

    public Reader get(ResultSet rs, int index) throws SQLException {
        return rs.getCharacterStream(index);
    }

    @Override
    public Reader get(CallableStatement cs, String columnName) throws SQLException {
        return cs.getCharacterStream(columnName);
    }

    public Reader get(CallableStatement rs, int index) throws SQLException {
        return rs.getCharacterStream(index);
    }

    public Object set(PreparedStatement stat, Object obj, int index) throws SQLException {
        if (null == obj) {
            stat.setNull(index, getSqlType());
        } else {
            Jdbcs.setCharacterStream(index, (Reader) obj, stat);
        }
        return null;
    }

    @Override
    public void update(ResultSet rs, String columnName, Object value) throws SQLException {
        rs.updateCharacterStream(columnName, (Reader) value);
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        rs.updateCharacterStream(columnIndex, (Reader) value);
    }

    @Override
    public int getSqlType() {
        return Types.BINARY;
    }

    @Override
    public Class<Reader> getDefaultJavaType() {
        return Reader.class;
    }
}
