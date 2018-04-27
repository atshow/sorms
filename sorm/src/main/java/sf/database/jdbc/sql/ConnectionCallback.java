package sf.database.jdbc.sql;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 直接获得Connecton，通常用于存储过程等不支持的地方
 */
public interface ConnectionCallback<T> {
    T call(Connection con) throws SQLException;
}
