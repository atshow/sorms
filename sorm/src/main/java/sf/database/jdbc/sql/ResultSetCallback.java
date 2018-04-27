package sf.database.jdbc.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 结果集回调处理
 * @author
 */
public interface ResultSetCallback<T> {

    T callback(ResultSet rs) throws SQLException;
}
