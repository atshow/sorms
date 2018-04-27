package sf.database.dao;

import com.querydsl.sql.AbstractSQLQuery;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLMergeClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import org.jooq.*;
import org.jooq.impl.JooqVisitor;
import sf.common.wrapper.Page;
import sf.database.DBCascadeField;
import sf.database.DBObject;
import sf.database.datasource.DataSourceLookup;
import sf.database.datasource.RoutingDataSource;
import sf.database.datasource.lookup.SingleDataSourceLookup;
import sf.database.dbinfo.DBMetaData;
import sf.database.jdbc.sql.ConnectionCallback;
import sf.database.jdbc.sql.OrmIterator;
import sf.database.jdbc.sql.OrmStream;
import sf.database.jdbc.sql.ResultSetCallback;
import sf.database.meta.MetaHolder;
import sf.database.meta.OptimisticLock;
import sf.database.meta.TableMapping;
import sf.database.util.DBUtils;
import sf.ext.gen.GenConfig;
import sf.ext.gen.PojoGen;
import sf.jooq.Jooq;
import sf.jooq.tables.JooqTable;
import sf.querydsl.OrmSQLQueryFactory;
import sf.querydsl.QueryDSL;
import sf.tools.utils.Assert;
import sf.tools.utils.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * dbclient基础
 * @author sxf
 */
public class DBClient extends DaoSupport implements DBMethod {
    public static int SAVE_BATCH_SIZE = 100;// 批量执行数目为50条

    /**
     * sql执行上下文.
     */
    protected ThreadLocal<DBContext> dbContexts = ThreadLocal.withInitial(() -> null);
    protected Map<DataSource, OrmSQLQueryFactory> dataSourceSQLQueryFactoryMap = new ConcurrentHashMap<>();

    public DBClient(DataSource dataSource) {
        if (dataSource instanceof RoutingDataSource) {
            setDataSource((RoutingDataSource) dataSource);
        } else {
            RoutingDataSource routing = new RoutingDataSource();
            Map<String, Object> targetDataSources = new HashMap<>();
            targetDataSources.put("master", dataSource);
            routing.setTargetDataSources(targetDataSources);
            routing.setDefaultTargetDataSource("master");
            DataSourceLookup dsl = new SingleDataSourceLookup(dataSource);
            routing.setDataSourceLookup(dsl);
            setDataSource(routing);
        }
    }

    public DBClient() {

    }

    /**
     * 使用上下文语境(供以后扩展使用.)
     * @param dataSource 设置数据源
     * @return
     */
    @Override
    public DBClient useContext(String dataSource) {
        DBContext context = new DBContext();
        context.setDataSource(dataSource);
        return useContext(context);
    }

    /**
     * 使用上下文语境(供以后扩展使用.)
     * @param context 执行上下文.
     * @return
     */
    @Override
    public DBClient useContext(DBContext context) {
        Assert.notNull(context, "Context is null!");
        dbContexts.set(context);
        return this;
    }

    @Override
    protected DataSource getDataSource(Class<?> clz) {
        DBContext context = dbContexts.get();
        if (context != null && context.getDataSource() != null) {
            dbContexts.remove();
            return dataSource.getDataSource(context.getDataSource());
        }
        return super.getDataSource(clz);
    }

    @Override
    protected void closeConnection(Connection conn, DataSource ds) {
        super.closeConnection(conn, ds);
    }

    @Override
    public <T extends DBObject> T selectByPrimaryKeys(Class<T> clz, Object... keyParams) {
        DataSource ds = getDataSource(clz);
        Connection conn = null;
        try {
            conn = getConnection(ds);
            return crud.getCrudModel().selectByPrimaryKeys(conn, clz, keyParams);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T extends DBObject> T selectOne(T query) {
        DataSource ds = getDataSource(query.getClass());
        Connection conn = null;
        try {
            conn = getConnection(ds);
            return crud.getCrudModel().selectOne(conn, query);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T extends DBObject> List<T> selectList(T query) {
        DataSource ds = getDataSource(query.getClass());
        Connection conn = null;
        try {
            conn = getConnection(ds);
            return crud.getCrudModel().selectList(conn, query);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T extends DBObject> Page<T> selectPage(T query, int start, int limit) {
        DataSource ds = getDataSource(query.getClass());
        Connection conn = null;
        try {
            conn = getConnection(ds);
            return crud.getCrudModel().selectPage(conn, query, start, limit);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T extends DBObject> void selectIterator(OrmIterator<T> ormIt, T query) {
        DataSource ds = getDataSource(query.getClass());
        Connection conn = null;
        try {
            conn = getConnection(ds);
            crud.getCrudModel().selectIterator(conn, ormIt, query);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T extends DBObject> void selectStream(OrmStream<T> ormStream, T query) {
        DataSource ds = getDataSource(query.getClass());
        Connection conn = null;
        try {
            conn = getConnection(ds);
            crud.getCrudModel().selectStream(conn, ormStream, query);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see sf.database.dao.DBMethod#save(sf.database.DataObject)
     */
    @Override
    public <T extends DBObject> int insert(T obj) {
        DataSource ds = getDataSource(obj.getClass());
        Connection conn = null;
        try {
            conn = getConnection(ds);
            return crud.getCrudModel().insert(conn, obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T extends DBObject> int merge(T obj) {
        DataSource ds = getDataSource(obj.getClass());
        Connection conn = null;
        try {
            conn = getConnection(ds);
            return crud.getCrudModel().merge(conn, obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see sf.database.dao.DBMethod#update(sf.database.DataObject)
     */
    @Override
    public <T extends DBObject> int update(T obj) {
        DataSource ds = getDataSource(obj.getClass());
        Connection conn = null;
        try {
            conn = getConnection(ds);
            return crud.getCrudModel().update(conn, obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T extends DBObject> int updateAndSet(T obj) {
        DataSource ds = getDataSource(obj.getClass());
        Connection conn = null;
        try {
            conn = getConnection(ds);
            return crud.getCrudModel().updateAndSet(conn, obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T extends DBObject> int updateWithVersion(T obj) {
        DataSource ds = getDataSource(obj.getClass());
        Connection conn = null;
        try {
            conn = getConnection(ds);
            return crud.getCrudModel().updateWithVersion(conn, obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T extends DBObject> void setNewOptimisticLockValues(T obj) {
        DataSource ds = getDataSource(obj.getClass());
        Connection conn = null;
        try {
            conn = getConnection(ds);
            OptimisticLock.setNewOptimisticLockValues(conn, obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see sf.database.dao.DBMethod#delete(sf.database.DataObject)
     */
    @Override
    public <T extends DBObject> int delete(T obj) {
        DataSource ds = getDataSource(obj.getClass());
        Connection conn = null;
        try {
            conn = getConnection(ds);
            return crud.getCrudModel().delete(conn, obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see sf.database.dao.DBMethod#batchSave(java.util.List)
     */
    @Override
    public int[] batchInsert(List<? extends DBObject> modelList) {
        DataSource ds = getDataSource(modelList.get(0).getClass());
        Connection conn = null;
        try {
            conn = getConnection(ds);
            return crud.getCrudModel().batchInsert(conn, modelList, false, SAVE_BATCH_SIZE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public int[] batchInsertFast(List<? extends DBObject> modelList) {
        DataSource ds = getDataSource(modelList.get(0).getClass());
        Connection conn = null;
        try {
            conn = getConnection(ds);
            return crud.getCrudModel().batchInsert(conn, modelList, true, SAVE_BATCH_SIZE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see sf.database.dao.DBMethod#batchUpdate(java.util.List)
     */
    @Override
    public int[] batchUpdate(List<? extends DBObject> modelList) {
        DataSource ds = getDataSource(modelList.get(0).getClass());
        Connection conn = null;
        try {
            conn = getConnection(ds);
            return crud.getCrudModel().batchUpdate(conn, modelList, SAVE_BATCH_SIZE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    /* (non-Javadoc)
     * @see sf.database.dao.DBMethod#batchDelete(java.util.List)
     */
    @Override
    public int[] batchDelete(List<? extends DBObject> modelList) {
        DataSource ds = getDataSource(modelList.get(0).getClass());
        Connection conn = null;
        try {
            conn = getConnection(ds);
            return crud.getCrudModel().batchDelete(conn, modelList, SAVE_BATCH_SIZE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }



    @Override
    public void genPojoCodes(String pkg, String srcPath, GenConfig config) {
        DataSource ds = getDataSource(null);
        Connection conn = getConnection(ds);
        try {
            PojoGen gen = new PojoGen(conn, pkg, srcPath, config);
            gen.gen();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }

    }

    /**
     * @param clz
     * @return
     */
    @Override
    public boolean createTable(Class<?> clz) {
        DataSource ds = getDataSource(clz);
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudModel().createTable(conn, clz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    /**
     * 创建全部表
     * @return
     */
    @Override
    public boolean createTables() {
        Connection conn = null;
        boolean flag = false;
        Field f = ReflectionUtils.findField(MetaHolder.class, "pool");
        f.setAccessible(true);
        try {
            Map<Class<?>, TableMapping> pool = (Map<Class<?>, TableMapping>) f.get(null);
            for (Map.Entry<?, TableMapping> entry : pool.entrySet()) {
                if (entry.getValue().getTableName() != null && DBObject.class.isAssignableFrom(entry.getValue().getThisType())) {
                    DataSource ds = getDataSource(entry.getValue().getThisType());
                    conn = getConnection(ds);
                    crud.getCrudModel().createTable(conn, entry.getValue().getThisType());
                    flag = true;
                    closeConnection(conn, ds);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {

        }
        return flag;
    }

    @Override
    public boolean refreshTables() {
        Connection conn = null;
        boolean flag = false;
        Field f = ReflectionUtils.findField(MetaHolder.class, "pool");
        f.setAccessible(true);
        try {
            Map<Class<?>, TableMapping> pool = (Map<Class<?>, TableMapping>) f.get(null);
            for (Map.Entry<?, TableMapping> entry : pool.entrySet()) {
                if (entry.getValue().getTableName() != null && DBObject.class.isAssignableFrom(entry.getValue().getThisType())) {
                    DataSource ds = getDataSource(entry.getValue().getThisType());
                    conn = getConnection(ds);
                    DBMetaData.refreshTable(conn, entry.getValue().getThisType());
                    flag = true;
                    closeConnection(conn, ds);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {

        }
        return flag;
    }

    @Override
    public boolean refreshTable(Class<?> clz) {
        DataSource ds = getDataSource(clz);
        Connection conn = getConnection(ds);
        boolean flag = false;
        try {
            DBMetaData.refreshTable(conn, clz);
            flag = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
        return flag;
    }

    @Override
    public <T extends DBObject> T fetchLinks(T obj) {
        DataSource ds = getDataSource(obj.getClass());
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudModel().fetchLinks(conn, obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T extends DBObject> T fetchLinks(T obj, DBCascadeField... fields) {
        DataSource ds = getDataSource(obj.getClass());
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudModel().fetchLinks(conn, obj, fields);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public int insertCascade(DBObject obj, DBCascadeField... fields) {
        DataSource ds = getDataSource(obj.getClass());
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudModel().insertCascade(conn, obj, fields);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T extends DBObject> T insertLinks(T obj, DBCascadeField... fields) {
        DataSource ds = getDataSource(obj.getClass());
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudModel().insertLinks(conn, obj, fields);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T extends DBObject> T insertRelation(T obj, DBCascadeField... fields) {
        DataSource ds = getDataSource(obj.getClass());
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudModel().insertRelation(conn, obj, fields);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public int updateCascade(DBObject obj, DBCascadeField... fields) {
        DataSource ds = getDataSource(obj.getClass());
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudModel().updateCascade(conn, obj, fields);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T extends DBObject> T updateLinks(T obj, DBCascadeField... fields) {
        DataSource ds = getDataSource(obj.getClass());
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudModel().updateLinks(conn, obj, fields);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T extends DBObject> int updateRelation(T obj, DBCascadeField... fields) {
        DataSource ds = getDataSource(obj.getClass());
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudModel().updateRelation(conn, obj, fields);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T extends DBObject> int deleteCascade(T obj, DBCascadeField... fields) {
        DataSource ds = getDataSource(obj.getClass());
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudModel().deleteCascade(conn, obj, fields);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T extends DBObject> int deleteLinks(T obj, DBCascadeField... fields) {
        DataSource ds = getDataSource(obj.getClass());
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudModel().deleteLinks(conn, obj, fields);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T extends DBObject> int deleteRelation(T obj, DBCascadeField... fields) {
        DataSource ds = getDataSource(obj.getClass());
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudModel().deleteRelation(conn, obj, fields);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    /* (non-Javadoc)
     * @see sf.database.dao.DBMethod#select(java.lang.String, java.lang.Object[])
     */
    @Override
    public List<Map<String, Object>> select(String sql, Object... paras) {
        DataSource ds = getDataSource(null);
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudSql().select(conn, sql, paras);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    /* (non-Javadoc)
     * @see sf.database.dao.DBMethod#selectOne(java.lang.Class, java.lang.String, java.lang.Object[])
     */
    @Override
    public <T> T selectOne(Class<T> beanClass, String sql, Object... parameters) {
        DataSource ds = getDataSource(null);
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudSql().selectOne(conn, beanClass, sql, parameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T> List<T> selectList(Class<T> beanClass, String sql, Object... parameters) {
        DataSource ds = getDataSource(null);
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudSql().selectList(conn, beanClass, sql, parameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    /* (non-Javadoc)
     * @see sf.database.dao.DBMethod#selectArray(java.lang.Class, java.lang.String, java.lang.Object[])
     */
    @Override
    public <T> T[] selectArray(Class<T> arrayComponentClass, String sql, Object... parameters) {
        DataSource ds = getDataSource(null);
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudSql().selectArray(conn, arrayComponentClass, sql, parameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see sf.database.dao.DBMethod#queryAsPagelist(int, int, java.lang.Class,
     * java.lang.String, java.lang.Object)
     */
    @Override
    public <T> Page<T> selectPage(int start, int limit, Class<T> beanClass, String sql, Object... parameters) {
        DataSource ds = getDataSource(null);
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudSql().selectPage(conn, start, limit, beanClass, sql, parameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T> void selectIterator(OrmIterator<T> ormIt, Class<T> beanClass, String sql, Object... parameters) {
        DataSource ds = getDataSource(null);
        Connection conn = getConnection(ds);
        try {
            crud.getCrudSql().selectIterator(conn, ormIt, beanClass, sql, parameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T> void selectStream(OrmStream<T> ormStream, Class<T> beanClass, String sql, Object... parameters) {
        DataSource ds = getDataSource(null);
        Connection conn = getConnection(ds);
        try {
            crud.getCrudSql().selectStream(conn, ormStream, beanClass, sql, parameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T> void selectResultSet(ResultSetCallback<T> callback, String sql, Object... parameters) {
        DataSource ds = getDataSource(null);
        Connection conn = getConnection(ds);
        try {
            crud.getCrudSql().selectResultSet(conn, callback, sql, parameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T> T execute(ConnectionCallback<T> action) {
        DataSource ds = getDataSource(null);
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudSql().execute(conn, action);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see sf.database.dao.DBMethod#execute(java.lang.String, java.lang.Object)
     */
    @Override
    public int execute(String sql, Object... parameters) {
        DataSource ds = getDataSource(null);
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudSql().execute(conn, sql, parameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see sf.database.dao.DBMethod#executeBatch(java.lang.String,
     * java.util.List)
     */
    @Override
    public int[] executeBatch(String sql, List<Object[]> parameters) {
        DataSource ds = getDataSource(null);
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudSql().executeBatch(conn, sql, parameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public List<Map<String, Object>> selectTemplate(String sqlId, Map<String, Object> parameter) {
        DataSource ds = getDataSource(null);
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudTemplate().select(conn, sqlId, parameter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T> T selectOneTemplate(Class<T> beanClass, String sqlId, Map<String, Object> parameters) {
        DataSource ds = getDataSource(null);
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudTemplate().selectOne(conn, beanClass, sqlId, parameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T> T[] selectArrayTemplate(Class<T> arrayComponentClass, String sqlId, Map<String, Object> parameters) {
        DataSource ds = getDataSource(null);
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudTemplate().selectArray(conn, arrayComponentClass, sqlId, parameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T> Page<T> selectPageTemplate(int start, int limit, Class<T> beanClass, String sqlId,
                                          Map<String, Object> parameters) {
        DataSource ds = getDataSource(null);
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudTemplate().selectPage(conn, start, limit, beanClass, sqlId, parameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public int executeTemplate(String sqlId, Map<String, Object> paramters) {
        DataSource ds = getDataSource(null);
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudTemplate().execute(conn, sqlId, paramters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public int[] executeBatchTemplate(String sqlId, List<Map<String, Object>> parameters) {
        DataSource ds = getDataSource(null);
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudTemplate().executeBatch(conn, sqlId, parameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T> List<T> selectListTemplate(Class<T> beanClass, String sqlId, Map<String, Object> parameters) {
        DataSource ds = getDataSource(null);
        Connection conn = getConnection(ds);
        try {
            return crud.getCrudTemplate().selectList(conn, beanClass, sqlId, parameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    ///querydsl执行
    @Override
    @Deprecated
    public <T> SQLQuery<T> sqlQuery(Class<? extends DBObject>... tableClass) {
        DataSource ds = getDataSource(null);
        Connection conn = getConnection(ds);
        return crud.getQueryDSLInf().sqlQuery(conn, tableClass);
    }

    @Override
    public <T> List<T> sqlQueryList(AbstractSQLQuery query) {
        return sqlQueryList(query, null);
    }

    @Override
    public <T> List<T> sqlQueryList(AbstractSQLQuery query, Class<T> returnClass) {
        Class<?> clz = QueryDSL.getQueryDSLTableClass(query);
        DataSource ds = getDataSource(clz);
        Connection conn = getConnection(ds);
        try {
            return crud.getQueryDSLInf().sqlQueryList(conn, query, returnClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T> Page<T> sqlQueryPage(AbstractSQLQuery query, int start, int limit) {
        return sqlQueryPage(query, null, start, limit);
    }

    @Override
    public <T> Page<T> sqlQueryPage(AbstractSQLQuery query, Class<T> returnClass, int start, int limit) {
        Class<?> clz = QueryDSL.getQueryDSLTableClass(query);
        DataSource ds = getDataSource(clz);
        Connection conn = getConnection(ds);
        try {
            return crud.getQueryDSLInf().sqlQueryPage(conn, query, returnClass, start, limit);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T> T sqlQueryOne(AbstractSQLQuery query) {
        return sqlQueryOne(query, null);
    }

    @Override
    public <T> T sqlQueryOne(AbstractSQLQuery query, Class<T> returnClass) {
        Class<?> clz = QueryDSL.getQueryDSLTableClass(query);
        DataSource ds = getDataSource(clz);
        Connection conn = getConnection(ds);
        try {
            return crud.getQueryDSLInf().sqlQueryOne(conn, query, returnClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public int queryDSLInsert(SQLInsertClause insert) {
        Class<?> clz = QueryDSL.getInsertTableClass(insert);
        DataSource ds = getDataSource(clz);
        Connection conn = getConnection(ds);
        try {
            return crud.getQueryDSLInf().queryDSLInsert(conn, insert);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public int queryDSLUpdate(SQLUpdateClause update) {
        Class<?> clz = QueryDSL.getUpdateTableClass(update);
        DataSource ds = getDataSource(clz);
        Connection conn = getConnection(ds);
        try {
            return crud.getQueryDSLInf().queryDSLUpdate(conn, update);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public int queryDSLDelete(SQLDeleteClause delete) {
        Class<?> clz = QueryDSL.getDeleteTableClass(delete);
        DataSource ds = getDataSource(clz);
        Connection conn = getConnection(ds);
        try {
            return crud.getQueryDSLInf().queryDSLDelete(conn, delete);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public int queryDSLMerge(SQLMergeClause merge) {
        Class<?> clz = QueryDSL.getMergeTableClass(merge);
        DataSource ds = getDataSource(clz);
        Connection conn = getConnection(ds);
        try {
            return crud.getQueryDSLInf().queryDSLMerge(conn, merge);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public OrmSQLQueryFactory sqlQueryFactory() {
        DataSource ds = getDataSource(null);
        OrmSQLQueryFactory queryFactory = dataSourceSQLQueryFactoryMap.get(ds);
        if (queryFactory == null) {
            queryFactory = createSQLQueryFactory(ds);
            dataSourceSQLQueryFactoryMap.put(ds, queryFactory);
        }
        return queryFactory;
    }

    protected OrmSQLQueryFactory createSQLQueryFactory(DataSource ds) {
        Connection conn = getConnection(ds);
        SQLTemplates sqlTemplates = DBUtils.doGetDialect(conn, false).getQueryDslDialect();
        Configuration configuration = new Configuration(sqlTemplates);
        OrmSQLQueryFactory queryFactory = new OrmSQLQueryFactory(configuration, ds);
        return queryFactory;
    }

    ///////// jooq /////////// 支持
    @Override
    public <T> T jooqSelectOne(Select<?> select) {
        return jooqSelectOne(select, null);
    }

    @Override
    public <T> T jooqSelectOne(Select<?> select, Class<T> returnClass) {
        Class<?> clz = Jooq.getSelectFromTable(select);
        DataSource ds = getDataSource(clz);
        Connection conn = getConnection(ds);
        try {
            return crud.getJooqInf().jooqSelectOne(conn, select, returnClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T> List<T> jooqSelectList(Select<?> select) {
        return jooqSelectList(select, null);
    }

    @Override
    public <T> List<T> jooqSelectList(Select<?> select, Class<T> returnClass) {
        Class<?> clz = Jooq.getSelectFromTable(select);
        DataSource ds = getDataSource(clz);
        Connection conn = getConnection(ds);
        try {
            return crud.getJooqInf().jooqSelectList(conn, select, returnClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public <T> Page<T> jooqSelectPage(Select<?> select, int start, int limit) {
        return jooqSelectPage(select, null, start, limit);
    }

    @Override
    public <T> Page<T> jooqSelectPage(Select<?> select, Class<T> returnClass, int start, int limit) {
        Class<?> clz = Jooq.getSelectFromTable(select);
        DataSource ds = getDataSource(clz);
        Connection conn = getConnection(ds);
        try {
            return crud.getJooqInf().jooqSelectPage(conn, select, returnClass, start, limit);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public int jooqInsert(Insert<?> insert) {
        Assert.notNull(insert, "");
        Table<?> table = JooqVisitor.getInsertTable(insert);
        Class<?> clz = null;
        if (table != null && table instanceof JooqTable) {
            clz = ((JooqTable<?>) table).getClz();
        }
        DataSource ds = getDataSource(clz);
        Connection conn = getConnection(ds);
        try {
            return crud.getJooqInf().jooqInsert(conn, insert);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public int jooqUpdate(Update<?> update) {
        Assert.notNull(update, "");
        Table<?> table = JooqVisitor.getUpdateTable(update);
        Class<?> clz = null;
        if (table != null && table instanceof JooqTable) {
            clz = ((JooqTable<?>) table).getClz();
        }
        DataSource ds = getDataSource(clz);
        Connection conn = getConnection(ds);
        try {
            return crud.getJooqInf().jooqUpdate(conn, update);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }

    @Override
    public int jooqDelect(Delete<?> delete) {
        Assert.notNull(delete, "");
        Table<?> table = JooqVisitor.getDeleteTable(delete);
        Class<?> clz = null;
        if (table != null && table instanceof JooqTable) {
            clz = ((JooqTable<?>) table).getClz();
        }
        DataSource ds = getDataSource(clz);
        Connection conn = getConnection(ds);
        try {
            return crud.getJooqInf().jooqDelect(conn, delete);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn, ds);
        }
    }
}
