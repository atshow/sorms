package sf.database.jdbc.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sf.common.CaseInsensitiveMap;
import sf.common.log.OrmLog;
import sf.common.wrapper.Page;
import sf.database.dialect.DBDialect;
import sf.database.dialect.oracle.OracleDialect;
import sf.database.jdbc.handle.PageListHandler;
import sf.database.jdbc.handle.ResultSetHandler;
import sf.database.jdbc.handle.RowListHandler;
import sf.database.jdbc.handle.SingleRowHandler;
import sf.database.jdbc.rowmapper.MapRowMapper;
import sf.database.jdbc.rowmapper.RowMapper;
import sf.database.jdbc.rowmapper.RowMapperHelp;
import sf.database.jdbc.type.Jdbcs;
import sf.database.template.sql.SQLContext;
import sf.database.template.sql.SQLParameter;
import sf.database.util.DBUtils;
import sf.tools.StringUtils;
import sf.tools.utils.Assert;
import sf.tools.utils.CollectionUtils;

import java.sql.*;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 */
public class CrudSqlImpl implements CrudSqlInf {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CrudSqlImpl.class);

    private void logMessage(String sql, List<Object> values) {
        LOGGER.debug("Executing SQL:[{0}],values:{1}",
                sql, values);
    }

    private static CrudSqlImpl instance = new CrudSqlImpl();

    public static CrudSqlImpl getInstance() {
        return instance;
    }

    private CrudSqlImpl() {

    }

    static ResultSetHandler<List<Map<String, Object>>> mapRsh = new RowListHandler(new MapRowMapper());

    @Override
    public List<Map<String, Object>> select(Connection conn, String sql, Object... paras) throws SQLException {
        return select(conn, mapRsh, sql, paras);

//        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
//        OrmLog.commonArrayLog(sql, paras);
//        PreparedStatement pst = conn.prepareStatement(sql);
//        if (paras != null && paras.length > 0) {
//            for (int i = 0; i < paras.length; i++) {
//                pst.setObject(i + 1, paras[i]);
//            }
//        }
//        ResultSet rs = pst.executeQuery();
//        ResultSetMetaData rsmd = rs.getMetaData();
//        int colAmount = rsmd.getColumnCount();
//        while (rs.next()) {
//            Map<String, Object> map = new LinkedHashMap<String, Object>();
//            for (int i = 1; i <= colAmount; i++) {
//                map.put(rsmd.getColumnName(i), rs.getObject(i));
//            }
//            result.add(map);
//        }
//        CommonSql.close(rs);
//        CommonSql.close(pst);
//        return result;
    }

    @Override
    public <T> T selectOne(Connection conn, Class<T> beanClass, String sql, Object... parameters) throws SQLException {
        Assert.notNull(beanClass, "beanClass is null.");

        RowMapper<T> rowMapper = RowMapperHelp.getRowMapper(beanClass);
        ResultSetHandler<T> rsh = new SingleRowHandler<T>(rowMapper);
        return select(conn, rsh, sql, parameters);
    }

    @Override
    public <T> List<T> selectList(Connection conn, Class<T> beanClass, String sql, Object... parameters)
            throws SQLException {
        Assert.notNull(beanClass, "beanClass is null.");
        RowMapper<T> rowMapper = RowMapperHelp.getRowMapper(beanClass);
        ResultSetHandler<List<T>> rsh = new RowListHandler<T>(rowMapper);
        return select(conn, rsh, sql, parameters);
    }

    @Override
    public <T> T[] selectArray(Connection conn, Class<T> arrayComponentClass, String sql, Object... parameters)
            throws SQLException {
        try {
            Class<T[]> clazz = (Class<T[]>) Class.forName("[" + arrayComponentClass.getName());
            return selectOne(conn, clazz, sql, parameters);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param conn
     * @param start      0开始
     * @param limit      限制多少条数据
     * @param beanClass
     * @param sql
     * @param parameters
     * @return
     */
    @Override
    public <T> Page<T> selectPage(Connection conn, int start, int limit, Class<T> beanClass, String sql,
                                  Object... parameters) throws SQLException {
        Assert.notNull(beanClass, "beanClass is null.");
        RowMapper<T> rowMapper = RowMapperHelp.getRowMapper(beanClass);

        Page<T> page = null;

        String countSql = DBUtils.getSqlSelectCount(sql);
        long count = selectOne(conn, Long.class, countSql, parameters);
        page = new Page<T>((int) count, limit);
        List<T> items = Collections.emptyList();
        if (count > 0) {
            String pageSql = DBUtils.doGetDialect(conn, false).sqlPageList(sql, start, limit);
            PageListHandler<T> rsh = new PageListHandler<T>(rowMapper);
            if (pageSql == null) {
                // 如果不支持分页，那么使用原始的分页方法 ResultSet.absolute(first)
                rsh.setFirstResult(start);
            } else {
                // 使用数据库自身的分页SQL语句，将直接返回某一个
                rsh.setFirstResult(0);
                sql = pageSql;
            }
            rsh.setMaxResults(limit);
            items = select(conn, rsh, sql, parameters);
        }
        page.setList(items);
        return page;
    }

    public <T> T select(Connection conn, ResultSetHandler<T> rsh, String sql, Object... parameters)
            throws SQLException {
        Assert.notNull(rsh, "rsh is null.");
        Assert.notNull(sql, "sql is null.");
        PreparedStatement ps = null;
        ResultSet rs = null;
        T result = null;
        OrmLog.commonArrayLog(sql, parameters);
        //执行时间
        long start = System.currentTimeMillis();
        ps = conn.prepareStatement(sql);
        CommonSql.fillArrayStatement(ps, parameters);
        rs = ps.executeQuery();
        try {
            result = rsh.handle(rs);
            OrmLog.resultLog(start, result);
        } catch (Exception e) {
            throw e;
        } finally {
            CommonSql.close(rs);
            CommonSql.close(ps);
        }
        return result;
    }

    public <T> T selectByContext(Connection conn, ResultSetHandler<T> rsh, String sql, Object... parameters)
            throws SQLException {
        Assert.notNull(rsh, "rsh is null.");
        Assert.notNull(sql, "sql is null.");
        SQLContext context = new SQLContext();
        context.setAfterSql(sql);
        if (parameters != null && parameters.length > 0) {
            List<SQLParameter> sqlParameters = new ArrayList<>(parameters.length);
            for (Object o : parameters) {
                sqlParameters.add(new SQLParameter().setValue(o).setHandler(Jdbcs.getDB2BeanMappingType(o.getClass())));
            }
            context.setParas(sqlParameters);
        }
        return select(conn, rsh, context);
    }

    public <T> T select(Connection conn, ResultSetHandler<T> rsh, SQLContext context)
            throws SQLException {
        Assert.notNull(rsh, "rsh is null.");
        Assert.notNull(context, "sql is null.");
        PreparedStatement ps = null;
        ResultSet rs = null;
        T result = null;
        OrmLog.commonListLog(context.getAfterSql(), context.getValues());
        //执行时间
        long start = System.currentTimeMillis();
        ps = conn.prepareStatement(context.getAfterSql());
        CommonSql.fillSQLStatement(ps, context.getParas());
        rs = ps.executeQuery();
        try {
            result = rsh.handle(rs);
            OrmLog.resultLog(start, result);
        } catch (Exception e) {
            throw e;
        } finally {
            CommonSql.close(rs);
            CommonSql.close(ps);
        }
        return result;
    }

    /**
     * 迭代接口
     * @param conn
     * @param ormIt
     * @param beanClass
     * @param sql
     * @param parameters
     * @param <T>
     * @throws Exception
     */
    @Override
    public <T> void selectIterator(Connection conn, OrmIterator<T> ormIt, Class<T> beanClass, String sql, Object... parameters)
            throws SQLException {
        Assert.notNull(ormIt, "ormIt is null.");
        Assert.notNull(sql, "sql is null.");
        selectResultSet(conn, (ResultSetCallback<T>) rs -> {
            JDBCIterator<T> it = new JDBCIterator<>();
            it.setRs(rs);
            it.setRm(RowMapperHelp.getRowMapper(beanClass));
            ormIt.dealWith(it);
            return null;
        }, sql, parameters);
    }

    @Override
    public <T> void selectStream(Connection conn, OrmStream<T> ormStream, Class<T> beanClass, String sql, Object... parameters)
            throws SQLException {
        Assert.notNull(ormStream, "ormStream is null.");
        Assert.notNull(sql, "sql is null.");
        selectResultSet(conn, (ResultSetCallback<T>) rs -> {
            JDBCIterator<T> it = new JDBCIterator<>();
            it.setRs(rs);
            it.setRm(RowMapperHelp.getRowMapper(beanClass));
            Stream<T> stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, 0), false);
            ormStream.dealWith(stream);
            return null;
        }, sql, parameters);
    }

    @Override
    public <T> T selectResultSet(Connection conn, ResultSetCallback<T> callback, String sql, Object... parameters)
            throws SQLException {
        Assert.notNull(callback, "callback is null.");
        Assert.notNull(sql, "sql is null.");
        PreparedStatement ps = null;
        ResultSet rs = null;
        OrmLog.commonArrayLog(sql, parameters);
        ps = conn.prepareStatement(sql);
        CommonSql.fillArrayStatement(ps, parameters);
        //执行时间
        long start = System.currentTimeMillis();
        rs = ps.executeQuery();
        try {
            return callback.callback(rs);
        } catch (Exception e) {
            throw e;
        } finally {
            CommonSql.close(rs);
            CommonSql.close(ps);
            OrmLog.resultLog(start);
        }
    }

    @Override
    public <T> T execute(Connection con, ConnectionCallback<T> action) throws SQLException {
        Assert.notNull(action, "Callback object must not be null");
        return action.call(con);
    }

    @Override
    public int execute(Connection conn, String sql, Object... parameters) throws SQLException {
        Assert.notNull(sql, "sql is null.");
        PreparedStatement ps = null;
        int rows = 0;
        OrmLog.commonArrayLog(sql, parameters);
        //执行时间
        long start = System.currentTimeMillis();
        try {
            ps = conn.prepareStatement(sql);
            CommonSql.fillArrayStatement(ps, parameters);
            rows = ps.executeUpdate();
            OrmLog.resultLog(start, rows);
        } catch (Exception e) {
            OrmLog.resultLog(start);
            throw e;
        } finally {
            DBUtils.closeQuietly(ps);
        }
        return rows;
    }

    @Override
    public int[] executeBatch(Connection conn, String sql, List<Object[]> parameters) throws SQLException {
        return executeBatch(conn, sql, parameters, true, 100, null, null);
    }

    @Override
    public int[] executeBatch(Connection conn, String sql, List<Object[]> parameters, boolean insertFast,
                              int batchSize, List<String> pkeys, List<Map<String, Object>> keyValues) throws SQLException {
        Assert.notNull(sql, "sql is null.");
        int counter = 0;
        int pointer = 0;
        int size = parameters.size();


        boolean isInsert = false;
        boolean isInertAuto = false;
        boolean isUpdate = false;
        boolean isDelete = false;
        if (StringUtils.containsIgnoreCase(sql, "insert")) {
            isInsert = true;
        } else if (StringUtils.containsIgnoreCase(sql, "update")) {
            isUpdate = true;
        } else if (StringUtils.containsIgnoreCase(sql, "delete")) {
            isDelete = true;
        }

        PreparedStatement ps = null;
        int[] rows = new int[size];
        //执行时间
        long start = System.currentTimeMillis();
        if (isInsert && !insertFast) {
            DBDialect dialect = DBUtils.doGetDialect(conn, false);
            if (dialect.getClass() == OracleDialect.class && CollectionUtils.isNotEmpty(pkeys)) {
                ps = conn.prepareStatement(sql, pkeys.toArray(new String[pkeys.size()]));
            } else {
                ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                isInertAuto = true;
            }
        } else {
            ps = conn.prepareStatement(sql);
        }
        try {
            if (parameters != null && !parameters.isEmpty()) {
                int count = 0;
                for (Object[] parameter : parameters) {
                    if (parameter != null && parameter.length > 0) {
                        OrmLog.batchCommonLog(sql, parameters.size(), (count++) + 1, parameter);
                        CommonSql.fillArrayStatement(ps, parameter);
                    }
                    ps.addBatch();
                    if (++counter >= batchSize) {
                        counter = 0;
                        int[] r = ps.executeBatch();
                        for (int k = 0; k < r.length; k++) {
                            rows[pointer++] = r[k];
                        }
                    }
                }
            }
            int[] r = ps.executeBatch();
            for (int k = 0; k < r.length; k++) {
                rows[pointer++] = r[k];
            }
            if (isInertAuto && !insertFast) {
                if (keyValues != null) {
                    setPkValueAfter(ps, keyValues);
                }
            }
            OrmLog.resultLog(start, rows);
        } catch (Exception e) {
            OrmLog.resultLog(start);
            throw e;
        } finally {
            DBUtils.closeQuietly(ps);
        }
        return rows;
    }

    /**
     * Get id after save record.
     */
    private void setPkValueAfter(PreparedStatement pst, List<Map<String, Object>> keyValues) throws SQLException {
        ResultSet rs = pst.getGeneratedKeys();
        if (rs != null) {
            ResultSetMetaData rsmd = rs.getMetaData();
            int cols = rsmd.getColumnCount();
            while (rs.next()) {
                Map<String, Object> result = new CaseInsensitiveMap<>();
                for (int i = 1; i <= cols; i++) {
                    String columnName = rsmd.getColumnLabel(i);
                    if (columnName == null || columnName.length() == 0) {
                        columnName = rsmd.getColumnName(i);
                    }
                    result.put(columnName, rs.getObject(i));
                }
                keyValues.add(result);
            }
            rs.close();
        }
    }

}
