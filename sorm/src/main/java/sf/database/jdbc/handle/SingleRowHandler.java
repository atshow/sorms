package sf.database.jdbc.handle;

import sf.database.jdbc.rowmapper.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SingleRowHandler<T> implements ResultSetHandler<T> {

    private RowMapper<T> mapper;

    public SingleRowHandler(RowMapper<T> mapper) {
        this.mapper = mapper;
    }

    @Override
    public T handle(ResultSet rs) throws SQLException {
        if (rs.next()) {
            return mapper.handle(rs, 0);
        }
        return null;
    }
}
