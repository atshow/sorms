package sf.database.jdbc.sql;

import sf.database.meta.ColumnMapping;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * 主键生成策略
 */
public interface IdentifierGenerator {


    /**
     * 生成一个新的主键
     * @param conn          连接,不能关闭.
     * @param columnMapping 列定义
     * @return 生成的主键值
     * @throws SQLException
     */
    Object generate(Connection conn, ColumnMapping columnMapping) throws SQLException;
}
