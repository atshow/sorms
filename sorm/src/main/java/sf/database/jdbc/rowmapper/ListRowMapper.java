package sf.database.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class ListRowMapper<T> implements RowMapper<List<Object>> {

    @Override
    public List<Object> handle(ResultSet rs, int rowNum) throws SQLException {
        int cols = rs.getMetaData().getColumnCount();
        List<Object> result = new ArrayList<>();
        for (int i = 0; i < cols; i++) {
            result.add(rs.getObject(i + 1));
        }
        return result;
    }

}
