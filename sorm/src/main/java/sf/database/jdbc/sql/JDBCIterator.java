package sf.database.jdbc.sql;

import sf.database.jdbc.rowmapper.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class JDBCIterator<T> implements Iterator<T> {

    private ResultSet rs;
    private RowMapper<T> rm;
    private int rowNum;

    @Override
    public boolean hasNext() {
        try {
            return rs.next();
        } catch (SQLException e) {
            rethrow(e);
            return false;
        }
    }

    @Override
    public T next() {
        try {
            return rm.handle(rs, rowNum++);
        } catch (SQLException e) {
            rethrow(e);
            return null;
        }
    }

    public void remove() {
        try {
            this.rs.deleteRow();
        } catch (SQLException e) {
            rethrow(e);
        }
    }

    protected void rethrow(SQLException e) {
        throw new RuntimeException(e.getMessage());
    }

    public ResultSet getRs() {
        return rs;
    }

    public void setRs(ResultSet rs) {
        this.rs = rs;
    }

    public RowMapper<T> getRm() {
        return rm;
    }

    public void setRm(RowMapper<T> rm) {
        this.rm = rm;
    }

    /**
     * Generates an <code>Iterable</code>, suitable for use in for-each loops.
     * @param rs Wrap this <code>ResultSet</code> in an <code>Iterator</code>.
     * @return an <code>Iterable</code>, suitable for use in for-each loops.
     */
    public static Iterable iterable(ResultSet rs, RowMapper mapper) {
        return () -> {
            JDBCIterator it = new JDBCIterator();
            it.setRs(rs);
            it.setRm(mapper);
            return it;
        };
    }
}
