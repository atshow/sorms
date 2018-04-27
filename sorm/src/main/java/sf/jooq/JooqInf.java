package sf.jooq;

import org.jooq.Delete;
import org.jooq.Insert;
import org.jooq.Select;
import org.jooq.Update;
import sf.common.wrapper.Page;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface JooqInf {
    /**
     * @param conn
     * @param select
     * @param <T>    返回值根据select的查询条件变化,可能为bean,单一值,或Map
     * @return
     * @throws SQLException
     */
    <T> T jooqSelectOne(Connection conn, Select select, Class<T> returnClass) throws SQLException;

    /**
     * @param conn
     * @param select
     * @param <T>    返回值根据select的查询条件变化,可能为bean,单一值,或Map
     * @return
     * @throws SQLException
     */
    <T> List<T> jooqSelectList(Connection conn, Select select, Class<T> returnClass) throws SQLException;

    /**
     * @param conn
     * @param select
     * @param start
     * @param limit
     * @param <T>    返回值根据select的查询条件变化,可能为bean,单一值,或Map
     * @return
     * @throws SQLException
     */
    <T> Page<T> jooqSelectPage(Connection conn, Select select, Class<T> returnClass, int start, int limit) throws SQLException;

    /**
     * @param conn
     * @param insert
     * @return
     * @throws SQLException
     */
    int jooqInsert(Connection conn, Insert insert) throws SQLException;

    /**
     * @param conn
     * @param update
     * @return
     * @throws SQLException
     */
    int jooqUpdate(Connection conn, Update update) throws SQLException;

    /**
     * @param conn
     * @param delete
     * @return
     * @throws SQLException
     */
    int jooqDelect(Connection conn, Delete delete) throws SQLException;
}
