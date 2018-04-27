package sf.database.jdbc.handle;

import sf.database.jdbc.rowmapper.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class RowListHandler<T> implements ResultSetHandler<List<T>> {

    private RowMapper<T> mapper;

    public RowListHandler(RowMapper<T> mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<T> handle(ResultSet rs) throws SQLException {
        List<T> rows = new ArrayList<T>();
        int count = 0;
        while (rs.next()) {
            rows.add(mapper.handle(rs, count++));
        }
        return rows;
    }
}
