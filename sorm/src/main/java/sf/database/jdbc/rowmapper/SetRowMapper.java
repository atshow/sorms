package sf.database.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;


public class SetRowMapper<T> implements RowMapper<Set<Object>> {

    @Override
    public Set<Object> handle(ResultSet rs, int rowNum) throws SQLException {
        int cols = rs.getMetaData().getColumnCount();
        Set<Object> result = new LinkedHashSet<>();
        for (int i = 0; i < cols; i++) {
            result.add(rs.getObject(i + 1));
        }
        return result;
    }

}
