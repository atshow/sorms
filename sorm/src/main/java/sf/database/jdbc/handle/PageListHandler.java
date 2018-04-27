package sf.database.jdbc.handle;

import sf.database.jdbc.rowmapper.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PageListHandler<T> implements ResultSetHandler<List<T>> {

    private RowMapper<T> mapper;
    private int first;
    private int max;

    public PageListHandler(RowMapper<T> mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<T> handle(ResultSet rs) throws SQLException {
        if (first > 1) {
            boolean succ = rs.absolute(first - 1);
            if (!succ) {
                return Collections.emptyList();
            }
        }

        List<T> rows = new ArrayList<T>();
        int count = 0;
        while (rs.next()) {
            rows.add(mapper.handle(rs, count++));
            if (max > 0 && rows.size() >= max) {
                break;
            }
        }
        return rows;
    }

    public void setFirstResult(int first) {
        this.first = first;
    }

    public void setMaxResults(int max) {
        this.max = max;
    }
}
