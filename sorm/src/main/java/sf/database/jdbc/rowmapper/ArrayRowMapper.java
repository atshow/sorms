package sf.database.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public class ArrayRowMapper<T> implements RowMapper<Object[]> {

    @Override
    public Object[] handle(ResultSet rs, int rowNum) throws SQLException {
        int cols = rs.getMetaData().getColumnCount();
        Object[] result = new Object[cols];

        for (int i = 0; i < cols; i++) {
            result[i] = rs.getObject(i + 1);
        }

        return result;
    }

}
