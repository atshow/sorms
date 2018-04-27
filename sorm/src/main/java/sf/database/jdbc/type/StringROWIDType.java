package sf.database.jdbc.type;

import java.sql.*;

/**
 * rowid为只读对象.
 */
public class StringROWIDType implements TypeHandler<String> {
    public String get(ResultSet rs, String colName) throws SQLException {
        return rs.getString(colName);
    }

    public String get(ResultSet rs, int index) throws SQLException {
        RowId rowId = rs.getRowId(index);
        if (rowId != null) {
            return rowId.toString();
        } else {
            return null;
        }
    }

    @Override
    public String get(CallableStatement cs, int index) throws SQLException {
        RowId rowId = cs.getRowId(index);
        if (rowId != null) {
            return rowId.toString();
        } else {
            return null;
        }
    }

    @Override
    public String get(CallableStatement cs, String parameterName) throws SQLException {
        RowId rowId = cs.getRowId(parameterName);
        if (rowId != null) {
            return rowId.toString();
        } else {
            return null;
        }
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        //不能设置rowid对象
        throw new UnsupportedOperationException(" you can not update rowid");
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        //不能设置rowid对象
        throw new UnsupportedOperationException(" you can not update rowid");
    }

    public Object set(PreparedStatement stat, Object obj, int i) throws SQLException {
        //不能设置rowid对象
        throw new UnsupportedOperationException(" you can not set rowid");
    }

    @Override
    public int getSqlType() {
        return Types.ROWID;
    }

    @Override
    public Class<String> getDefaultJavaType() {
        return String.class;
    }
}
