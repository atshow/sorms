package sf.database.jdbc.type;

import sf.tools.StringUtils;

import java.sql.*;

public class CharacterVARCHARType implements TypeHandler<Character> {
    public Character get(ResultSet rs, String colName) throws SQLException {
        String re = StringUtils.trim(rs.getString(colName));
        if (re == null || re.length() == 0)
            return null;
        return re.charAt(0);
    }

    public Character get(ResultSet rs, int index) throws SQLException {
        String re = StringUtils.trim(rs.getString(index));
        if (re == null || re.length() == 0)
            return null;
        return re.charAt(0);
    }

    @Override
    public Character get(CallableStatement cs, int index) throws SQLException {
        String re = StringUtils.trim(cs.getString(index));
        if (re == null || re.length() == 0)
            return null;
        return re.charAt(0);
    }

    @Override
    public Character get(CallableStatement cs, String parameterName) throws SQLException {
        String re = StringUtils.trim(cs.getString(parameterName));
        if (re == null || re.length() == 0)
            return null;
        return re.charAt(0);
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        String s = String.valueOf(((Character) value).charValue());
        rs.updateString(columnLabel, s);
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        String s = String.valueOf(((Character) value).charValue());
        rs.updateString(columnIndex, s);
    }

    public Object set(PreparedStatement stat, Object obj, int i) throws SQLException {
        if (null == obj) {
            stat.setString(i, null);
        } else {
            String s = String.valueOf(((Character) obj).charValue());
            stat.setString(i, s);
        }
        return null;
    }

    @Override
    public int getSqlType() {
        return Types.VARCHAR;
    }

    @Override
    public Class<Character> getDefaultJavaType() {
        return Character.class;
    }
}
