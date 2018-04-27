package sf.querydsl;

import com.querydsl.sql.AbstractSQLQuery;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLMergeClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import sf.common.wrapper.Page;
import sf.database.DBObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface QueryDSLInf {
    /**
     * QueryDSL支持，返回一个QueryDSL的查询对象，可以使用QueryDSL进行数据库操作,connection未释放,存在泄漏风险.
     * @param conn
     * @param tableClass 返回类型,主要是注册自定义类型.
     * @return SQLQuery
     * @see com.querydsl.sql.SQLQuery
     */
    @Deprecated
    <T> SQLQuery<T> sqlQuery(Connection conn, Class<? extends DBObject>... tableClass);

    /**
     * @param conn
     * @param query
     * @param returnClass 返回类型
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> List<T> sqlQueryList(Connection conn, AbstractSQLQuery query, Class<T> returnClass) throws SQLException;

    /**
     * @param conn
     * @param query
     * @param returnClass 返回类型
     * @param start
     * @param limit
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> Page<T> sqlQueryPage(Connection conn, AbstractSQLQuery query, Class<T> returnClass, int start, int limit) throws SQLException;

    /**
     * @param conn
     * @param query
     * @param returnClass 返回类型
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T> T sqlQueryOne(Connection conn, AbstractSQLQuery query, Class<T> returnClass) throws SQLException;

    /**
     * @param conn
     * @param insert
     * @return
     * @throws SQLException
     */
    int queryDSLInsert(Connection conn, SQLInsertClause insert) throws SQLException;

    /**
     * @param conn
     * @param update
     * @return
     * @throws SQLException
     */
    int queryDSLUpdate(Connection conn, SQLUpdateClause update) throws SQLException;

    /**
     * @param conn
     * @param delete
     * @return
     * @throws SQLException
     */
    int queryDSLDelete(Connection conn, SQLDeleteClause delete) throws SQLException;

    /**
     * @param conn
     * @param merge
     * @return
     * @throws SQLException
     */
    int queryDSLMerge(Connection conn, SQLMergeClause merge) throws SQLException;
}
