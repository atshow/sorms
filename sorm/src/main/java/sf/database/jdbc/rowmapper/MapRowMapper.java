package sf.database.jdbc.rowmapper;

import sf.common.CaseInsensitiveMap;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

public class MapRowMapper<T> implements RowMapper<Map<String, Object>> {

    @Override
    public Map<String, Object> handle(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> result = new CaseInsensitiveMap<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        int cols = rsmd.getColumnCount();

        for (int i = 1; i <= cols; i++) {
            String columnName = rsmd.getColumnLabel(i);
            if (columnName == null || columnName.length() == 0) {
                columnName = rsmd.getColumnName(i);
            }
            result.put(columnName, rs.getObject(i));
        }

        return result;
    }

}
