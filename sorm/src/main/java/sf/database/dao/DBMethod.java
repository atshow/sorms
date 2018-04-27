package sf.database.dao;

import com.querydsl.sql.AbstractSQLQuery;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLMergeClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import org.jooq.Delete;
import org.jooq.Insert;
import org.jooq.Select;
import org.jooq.Update;
import sf.common.wrapper.Page;
import sf.database.DBCascadeField;
import sf.database.DBObject;
import sf.database.jdbc.sql.ConnectionCallback;
import sf.database.jdbc.sql.OrmIterator;
import sf.database.jdbc.sql.OrmStream;
import sf.database.jdbc.sql.ResultSetCallback;
import sf.ext.gen.GenConfig;
import sf.querydsl.OrmSQLQueryFactory;

import java.util.List;
import java.util.Map;

/**
 * 数据库方法
 */
public interface DBMethod {

    /////// 执行上下文设置

    /**
     * 执行sql上下文,比如切换数据源
     * @param dataSource 数据源名称
     * @return
     */
    DBClient useContext(String dataSource);

    /**
     * 执行sql上下文,比如切换数据源
     * @param context 上下文
     * @return
     */
    DBClient useContext(DBContext context);

    /**
     * @param clz
     * @param keyParams
     * @param <T>
     * @return
     */
    <T extends DBObject> T selectByPrimaryKeys(Class<T> clz, Object... keyParams);

    /**
     * 查询一条记录，如果结果不唯一则抛出异常
     * @param query
     * @return 查询结果
     */
    <T extends DBObject> T selectOne(T query);

    /**
     * 查询列表
     * @param query 查询请求。
     *              <ul>
     *              <li>如果设置了Query条件，按query条件查询。 否则——</li>
     *              <li>如果设置了主键值，按主键查询，否则——</li>
     *              <li>按所有设置过值的字段作为条件查询。</li>
     *              </ul>
     * @return 结果
     */
    <T extends DBObject> List<T> selectList(T query);

    /**
     * 查询并分页
     * @param query 查询请求
     * @param start 起始记录，offset。从0开始。
     * @param limit 限制记录条数。如每页10条传入10。
     * @return
     */
    <T extends DBObject> Page<T> selectPage(T query, int start, int limit);

    <T extends DBObject> void selectIterator(OrmIterator<T> ormIt, T query);

    <T extends DBObject> void selectStream(OrmStream<T> ormStream, T query);

    ///// model 执行
    <T extends DBObject> int insert(T obj);

    /**
     * @param obj
     * @param <T>
     * @return
     */
    <T extends DBObject> int merge(T obj);


    /**
     * 更新对象(不认乐观锁条件)
     * @param obj
     * @return
     */
    <T extends DBObject> int update(T obj);


    /**
     * 乐观锁, 以特定字段的值作为限制条件,更新对象,并更新该字段的值.参考字段的Java属性名.必须设置了@Version标签
     * 注意:对于时间作为乐观锁,由于数据库和java时间存在差异,如果需要设置更新后的值,需要重新调用OptimisticLock.setNewOptimisticLockValues查询方法设置最新的值.
     * 锁更新规则
     * <p>
     * 1.对于数字,则+1;<br>
     * 2.对于日期取当前时间戳.<br>
     * 3.对于字符串,取UUID的值.
     * </p>
     * <p>
     * 执行的sql如下:
     * <p/>
     * <code>update t_user set age=30, city="广州", version=version+1 where name="wendal" and version=124;</code>
     * @param obj 需要更新的对象, 必须带主键,并设置过主键的值.
     * @return 若更新成功, 返回值大于0, 否则小于等于0
     */
    <T extends DBObject> int updateAndSet(T obj);

    /**
     * 根据乐观锁条件更新,并会更新数据库中的乐观锁条件.但不会更新对象的乐观锁字段的值.
     * 基于版本的更新，版本不一样无法更新数据
     * @param obj 需要更新的对象, 必须有@Version属性
     * @return 若更新成功, 大于0, 否则小于0
     */
    <T extends DBObject> int updateWithVersion(T obj);


    /**
     * 警告:从数据库中获取最新的乐观锁的值到对象中,一般情况下,该方法用不到.慎用.
     * @param obj
     * @param <T>
     */
    <T extends DBObject> void setNewOptimisticLockValues(T obj);

    /**
     * @param obj
     * @param <T>
     * @return
     */
    <T extends DBObject> int delete(T obj);

    /**
     * @param modelList
     * @return
     */
    int[] batchInsert(List<? extends DBObject> modelList);

    /**
     * 快速插入方法
     * @param modelList
     * @return
     */
    int[] batchInsertFast(List<? extends DBObject> modelList);

    /**
     * 更新对象,如果有乐观锁,会根据乐观锁条件更新,并会更新数据库中的乐观锁条件.但不会更新对象的乐观锁字段的值.
     * @param modelList
     * @return
     */
    int[] batchUpdate(List<? extends DBObject> modelList);

    /**
     * @param modelList
     * @return
     */
    int[] batchDelete(List<? extends DBObject> modelList);

    /**
     * 根据表名生成对应的pojo类
     * @param pkg      包名,如 com.test
     * @param srcPath: 文件保存路径
     * @param config   配置生成的风格
     * @throws Exception
     */
    void genPojoCodes(String pkg, String srcPath, GenConfig config);

    /**
     * @param clz
     * @return
     */
    boolean createTable(Class<?> clz);

    /**
     * 创建表
     * @return
     */
    boolean createTables();

    /**
     * 刷新表
     * @return
     */
    boolean refreshTables();

    boolean refreshTable(Class<?> clz);

    /**
     * @param obj
     * @return
     */
    <T extends DBObject> T fetchLinks(T obj);

    /**
     * @param obj
     * @param fields 级联字段
     * @return
     */
    <T extends DBObject> T fetchLinks(T obj, DBCascadeField... fields);

    /**
     * 将对象插入数据库同时，也将符合一个正则表达式的所有关联字段关联的对象统统插入相应的数据库
     * <p>
     * 关于关联字段更多信息，请参看 '@One' | '@Many' | '@ManyMany' 更多的描述
     * @param obj
     * @param fields 指定字段,控制力度更细,至少一个或多个 描述了什么样的关联字段将被关注。如果为 null，则表示全部的关联字段都会被插入
     * @return
     */
    int insertCascade(DBObject obj, DBCascadeField... fields);

    /**
     * 仅将对象所有的关联字段插入到数据库中，并不包括对象本身
     * @param obj    数据对象
     * @param fields 字段名称，描述了什么样的关联字段将被关注。如果为 null，则表示全部的关联字段都会被插入
     * @return 数据对象本身
     * @see javax.persistence.OneToOne
     * @see javax.persistence.ManyToMany
     * @see javax.persistence.OneToMany
     */
    <T extends DBObject> T insertLinks(T obj, DBCascadeField... fields);

    /**
     * 将对象的一个或者多个，多对多的关联信息，插入数据表
     * @param obj    对象
     * @param fields 正则表达式，描述了那种多对多关联字段将被执行该操作
     * @return 对象自身
     * @see javax.persistence.ManyToMany
     */
    <T extends DBObject> T insertRelation(T obj, DBCascadeField... fields);

    /**
     * 将对象更新的同时，也将符合一个正则表达式的所有关联字段关联的对象统统更新
     * <p>
     * 关于关联字段更多信息，请参看 '@One' | '@Many' | '@ManyMany' 更多的描述
     * @param obj    数据对象
     * @param fields 指定字段,控制力度更细,至少一个或多个,描述了什么样的关联字段将被关注。如果为 null，则表示全部的关联字段都会被更新
     * @return
     */
    int updateCascade(DBObject obj, DBCascadeField... fields);

    /**
     * 仅更新对象所有的关联字段，并不包括对象本身
     * @param obj    数据对象
     * @param fields 字段名称，描述了什么样的关联字段将被关注。如果为 null，则表示全部的关联字段都会被更新
     * @return 数据对象本身
     * @see javax.persistence.OneToOne
     * @see javax.persistence.ManyToMany
     * @see javax.persistence.OneToMany
     */
    <T extends DBObject> T updateLinks(T obj, DBCascadeField... fields);

    /**
     * 多对多关联是通过一个中间表将两条数据表记录关联起来。
     * <p>
     * 而这个中间表可能还有其他的字段，比如描述关联的权重等
     * <p>
     * 这个操作可以让你一次更新某一个对象中多个多对多关联的数据
     * @param obj    数据对象
     * @param fields 字段名称，描述了那种多对多关联字段将被执行该操作
     * @return 共有多少条数据被更新
     * @see javax.persistence.ManyToMany
     */
    <T extends DBObject> int updateRelation(T obj, DBCascadeField... fields);

    /**
     * 将对象删除的同时，也将符合一个正则表达式的所有关联字段关联的对象统统删除 <b style=color:red>注意：</b>
     * <p>
     * Java 对象的字段会被保留，这里的删除，将只会删除数据库中的记录
     * <p>
     * 关于关联字段更多信息，请参看 '@One' | '@Many' | '@ManyMany' 更多的描述
     * @param obj
     * @param fields 指定字段,控制力度更细,至少一个或多个 描述了什么样的关联字段将被关注。如果为 null，则表示全部的关联字段都会被删除
     * @param <T>
     * @return
     */
    <T extends DBObject> int deleteCascade(T obj, DBCascadeField... fields);

    /**
     * 仅删除对象所有的关联字段，并不包括对象本身。 <b style=color:red>注意：</b>
     * <p>
     * Java 对象的字段会被保留，这里的删除，将只会删除数据库中的记录
     * <p>
     * 关于关联字段更多信息，请参看 '@One' | '@Many' | '@ManyMany' 更多的描述
     * @param obj    数据对象
     * @param fields 字段名称，描述了什么样的关联字段将被关注。如果为 null，则表示全部的关联字段都会被删除
     * @return 被影响的记录行数
     * @see javax.persistence.OneToOne
     * @see javax.persistence.ManyToOne
     * @see javax.persistence.ManyToMany
     */
    <T extends DBObject> int deleteLinks(T obj, DBCascadeField... fields);

    /**
     * 多对多关联是通过一个中间表将两条数据表记录关联起来。
     * <p>
     * 而这个中间表可能还有其他的字段，比如描述关联的权重等
     * <p>
     * 这个操作可以让你一次删除某一个对象中多个多对多关联的数据
     * @param obj
     * @param fields 字段名称，描述了那种多对多关联字段将被执行该操作
     * @return 共有多少条数据被更新
     * @see javax.persistence.ManyToMany
     */
    <T extends DBObject> int deleteRelation(T obj, DBCascadeField... fields);

    /////// sql 查询执行
    List<Map<String, Object>> select(String sql, Object... paras);

    <T> T selectOne(Class<T> beanClass, String sql, Object... parameters);

    <T> T[] selectArray(Class<T> arrayComponentClass, String sql, Object... parameters);

    <T> Page<T> selectPage(int start, int limit, Class<T> beanClass, String sql, Object... parameters);

    <T> List<T> selectList(Class<T> beanClass, String sql, Object... parameters);

    /**
     * 大数据接口
     * @param ormIt
     * @param beanClass
     * @param sql
     * @param parameters
     * @param <T>
     */
    <T> void selectIterator(OrmIterator<T> ormIt, Class<T> beanClass, String sql, Object... parameters);

    /**
     * 大数据接口
     * @param ormStream
     * @param beanClass
     * @param sql
     * @param parameters
     * @param <T>
     */
    <T> void selectStream(OrmStream<T> ormStream, Class<T> beanClass, String sql, Object... parameters);

    <T> void selectResultSet(ResultSetCallback<T> callback, String sql, Object... parameters);

    /**
     * 直接获取connection,注意连接不可手动关闭
     * @param action
     * @param <T>
     * @return
     */
    <T> T execute(ConnectionCallback<T> action);

    int execute(String sql, Object... parameters);

    int[] executeBatch(String sql, List<Object[]> parameters);

    //// 模板ID查询执行
    List<Map<String, Object>> selectTemplate(String sqlId, Map<String, Object> paramters);

    <T> T selectOneTemplate(Class<T> beanClass, String sqlId, Map<String, Object> paramters);

    <T> T[] selectArrayTemplate(Class<T> arrayComponentClass, String sqlId, Map<String, Object> paramters);

    <T> Page<T> selectPageTemplate(int start, int limit, Class<T> beanClass, String sqlId,
                                   Map<String, Object> paramters);

    int executeTemplate(String sqlId, Map<String, Object> paramters);

    int[] executeBatchTemplate(String sqlId, List<Map<String, Object>> parameters);

    <T> List<T> selectListTemplate(Class<T> beanClass, String sqlId, Map<String, Object> paramters);

    //// dsl执行方法

    ////////////////// QueryDSL支持///////////////////////

    /**
     * QueryDSL支持，返回一个QueryDSL的查询对象，可以使用QueryDSL进行数据库操作(存在连接泄漏风险)
     * @param tableClass 注册自定义类型,主要是对Map映射为json的支持.
     * @return SQLQuery
     * @see com.querydsl.sql.SQLQuery
     */
    @Deprecated
    <T> SQLQuery<T> sqlQuery(Class<? extends DBObject>... tableClass);

    <T> List<T> sqlQueryList(AbstractSQLQuery query);

    /**
     * @param query
     * @param returnClass
     * @param <T>
     * @return map或单一类型, 或bean实体类
     */
    <T> List<T> sqlQueryList(AbstractSQLQuery query, Class<T> returnClass);

    <T> Page<T> sqlQueryPage(AbstractSQLQuery query, int start, int limit);

    /**
     * @param query
     * @param start
     * @param limit
     * @param <T>
     * @return map或单一类型, 或bean实体类
     */
    <T> Page<T> sqlQueryPage(AbstractSQLQuery query, Class<T> returnClass, int start, int limit);

    <T> T sqlQueryOne(AbstractSQLQuery query);

    /**
     * @param query
     * @param <T>
     * @return map或单一类型, 或bean实体类
     */
    <T> T sqlQueryOne(AbstractSQLQuery query, Class<T> returnClass);

    /**
     * @param insert
     * @return
     */
    int queryDSLInsert(SQLInsertClause insert);

    /**
     * @param update
     * @return
     */
    int queryDSLUpdate(SQLUpdateClause update);

    /**
     * @param delete
     * @return
     */
    int queryDSLDelete(SQLDeleteClause delete);

    /**
     * @param merge
     * @return
     */
    int queryDSLMerge(SQLMergeClause merge);

    /**
     * QueryDSL支持，返回一个QueryDSL的工厂，可以使用QueryDSL进行数据库操作
     * @return
     * @see com.querydsl.sql.SQLQueryFactory
     */
    OrmSQLQueryFactory sqlQueryFactory();

    ////////////////// jooq支持////////////////
    <T> T jooqSelectOne(Select<?> select);

    <T> T jooqSelectOne(Select<?> select, Class<T> returnClass);

    <T> List<T> jooqSelectList(Select<?> select);

    <T> List<T> jooqSelectList(Select<?> select, Class<T> returnClass);

    <T> Page<T> jooqSelectPage(Select<?> select, int start, int limit);

    <T> Page<T> jooqSelectPage(Select<?> select, Class<T> returnClass, int start, int limit);

    int jooqInsert(Insert<?> insert);

    int jooqUpdate(Update<?> update);

    int jooqDelect(Delete<?> delete);
}
