package sf.database.jdbc.sql;

import sf.common.wrapper.Page;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 根据模板id执行sql语句
 * @author sxf
 */
public interface CrudTemplateInf {
    /**
     * @param conn
     * @param sqlId
     * @param parameters
     * @return
     * @throws SQLException
     */
    int[] executeBatch(Connection conn, String sqlId, List<Map<String, Object>> parameters) throws SQLException;

    /**
     * @param conn
     * @param sqlId
     * @param parameters
     * @return
     * @throws SQLException
     */
    int execute(Connection conn, String sqlId, Map<String, Object> parameters) throws SQLException;

    /**
     * @param conn
     * @param start
     * @param limit
     * @param beanClass
     * @param sqlId
     * @param parameters
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> Page<T> selectPage(Connection conn, int start, int limit, Class<T> beanClass, String sqlId,
                           Map<String, Object> parameters) throws SQLException;

    /**
     * @param conn
     * @param arrayComponentClass
     * @param sqlId
     * @param parameters
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> T[] selectArray(Connection conn, Class<T> arrayComponentClass, String sqlId, Map<String, Object> parameters)
            throws SQLException;

    /**
     * @param conn
     * @param beanClass
     * @param sqlId
     * @param parameters
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> List<T> selectList(Connection conn, Class<T> beanClass, String sqlId, Map<String, Object> parameters)
            throws SQLException;

    /**
     * @param conn
     * @param beanClass
     * @param sqlId
     * @param parameters
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> T selectOne(Connection conn, Class<T> beanClass, String sqlId, Map<String, Object> parameters)
            throws SQLException;

    /**
     * @param conn
     * @param sqlId
     * @param parameters
     * @return
     * @throws SQLException
     */
    List<Map<String, Object>> select(Connection conn, String sqlId, Map<String, Object> parameters) throws SQLException;
}
