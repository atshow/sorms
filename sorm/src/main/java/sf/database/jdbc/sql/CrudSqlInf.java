package sf.database.jdbc.sql;

import sf.common.wrapper.Page;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 按sql操作
 */
public interface CrudSqlInf {
    /**
     * @param conn
     * @param sql
     * @param parameters 参数
     * @param insertFast 是否快速插入
     * @param batchSize  批处理条数
     * @param pkeys      主键列
     * @param keyValues  返回主键值
     * @return
     * @throws SQLException
     */
    int[] executeBatch(Connection conn, String sql, List<Object[]> parameters, boolean insertFast,
                       int batchSize, List<String> pkeys, List<Map<String, Object>> keyValues) throws SQLException;

    int[] executeBatch(Connection conn, String sql, List<Object[]> parameters) throws SQLException;

    <T> void selectIterator(Connection conn, OrmIterator<T> ormIt, Class<T> beanClass, String sql, Object... parameters)
            throws SQLException;

    <T> void selectStream(Connection conn, OrmStream<T> ormStream, Class<T> beanClass, String sql, Object... parameters)
            throws SQLException;

    <T> T selectResultSet(Connection conn, ResultSetCallback<T> callback, String sql, Object... parameters)
            throws SQLException;

    /**
     * 主要支持存储过程执行
     * @param con
     * @param action
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> T execute(Connection con, ConnectionCallback<T> action) throws SQLException;

    /**
     * @param conn
     * @param sql
     * @param parameters
     * @return
     * @throws SQLException
     */
    int execute(Connection conn, String sql, Object... parameters) throws SQLException;

    /**
     * @param conn
     * @param start
     * @param limit
     * @param beanClass
     * @param sql
     * @param parameters
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> Page<T> selectPage(Connection conn, int start, int limit, Class<T> beanClass, String sql, Object... parameters)
            throws SQLException;

    /**
     * @param conn
     * @param arrayComponentClass
     * @param sql
     * @param parameters
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> T[] selectArray(Connection conn, Class<T> arrayComponentClass, String sql, Object... parameters)
            throws SQLException;

    /**
     * @param conn
     * @param beanClass
     * @param sql
     * @param parameters
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> List<T> selectList(Connection conn, Class<T> beanClass, String sql, Object... parameters) throws SQLException;

    /**
     * @param conn
     * @param beanClass
     * @param sql
     * @param parameters
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> T selectOne(Connection conn, Class<T> beanClass, String sql, Object... parameters) throws SQLException;

    /**
     * @param conn
     * @param sql
     * @param paras
     * @return
     * @throws SQLException
     */
    List<Map<String, Object>> select(Connection conn, String sql, Object... paras) throws SQLException;
}
