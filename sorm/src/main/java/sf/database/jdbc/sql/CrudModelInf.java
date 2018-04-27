package sf.database.jdbc.sql;

import sf.common.wrapper.Page;
import sf.database.DBCascadeField;
import sf.database.DBObject;
import sf.database.OrmContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 按模型操作
 * @author sxf
 */
public interface CrudModelInf {
    //查询

    /**
     * @param conn
     * @param clz
     * @param keyParams
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T extends DBObject> T selectByPrimaryKeys(Connection conn, Class<T> clz, Object... keyParams) throws SQLException;

    List<Map<String, Object>> selectListMap(Connection conn, OrmContext context) throws SQLException;

    <T> T selectOne(Connection conn, Class<T> beanClass, OrmContext context) throws SQLException;

    <T> List<T> selectList(Connection conn, Class<T> beanClass, OrmContext context)
            throws SQLException;

    <T> Page<T> selectPage(Connection conn, int start, int limit, Class<T> beanClass, OrmContext context) throws SQLException;

    /**
     * 查询一条记录，如果结果不唯一则抛出异常
     * @param conn
     * @param query
     * @return 查询结果
     * @throws SQLException
     */
    <T extends DBObject> T selectOne(Connection conn, T query) throws SQLException;

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
    <T extends DBObject> List<T> selectList(Connection conn, T query) throws SQLException;

    /**
     * 查询并分页
     * @param query 查询请求
     * @param start 起始记录，offset。从0开始。
     * @param limit 限制记录条数。如每页10条传入10。
     * @return
     */
    <T extends DBObject> Page<T> selectPage(Connection conn, T query, int start, int limit) throws SQLException;

    <T extends DBObject> void selectIterator(Connection conn, OrmIterator<T> ormIt, T query) throws SQLException;

    <T extends DBObject> void selectStream(Connection conn, OrmStream<T> ormStream, T query) throws SQLException;

    /**
     * 插入
     * @param conn
     * @param obj
     * @return
     * @throws SQLException
     */
    int insert(Connection conn, DBObject obj) throws SQLException;


    /**
     * 更新对象(不区分乐观锁条件)
     * @param conn
     * @param obj
     * @return
     * @throws SQLException
     */
    <T extends DBObject> int update(Connection conn, T obj) throws SQLException;


    /**
     * 乐观锁, 以特定字段的值作为限制条件,更新对象,并更新该字段的值.参考字段的Java属性名.必须设置了@Version标签<br>
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
     * @param conn
     * @param obj  需要更新的对象, 必须带主键,并设置过主键的值.
     * @return 若更新成功, 返回值大于0, 否则小于等于0
     * @throws SQLException
     */
    <T extends DBObject> int updateAndSet(Connection conn, T obj) throws SQLException;

    /**
     * 根据乐观锁条件更新,并会更新数据库中的乐观锁条件.但不会更新对象的乐观锁字段的值.
     * 基于版本的更新，版本不一样无法更新到数据
     * @param conn
     * @param obj  需要更新的对象, 必须有@Version属性
     * @return 若更新成功, 大于0, 否则小于0
     * @throws SQLException
     */
    <T extends DBObject> int updateWithVersion(Connection conn, T obj) throws SQLException;

    /**
     * 合并记录
     * @param conn
     * @param obj
     * @return
     * @throws SQLException
     */
    int merge(Connection conn, DBObject obj) throws SQLException;


    /**
     * @param conn
     * @param obj
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T extends DBObject> int delete(Connection conn, T obj) throws SQLException;


    /**
     * @param conn
     * @param modelList
     * @param batchSize
     * @return
     * @throws SQLException
     */
    int[] batchInsert(Connection conn, Collection<? extends DBObject> modelList, boolean insertFast, int batchSize) throws SQLException;

    /**
     * 更新对象,如果有乐观锁,会根据乐观锁条件更新,并会更新数据库中的乐观锁条件.但不会更新对象的乐观锁字段的值.
     * @param conn
     * @param modelList
     * @param batchSize
     * @return
     * @throws SQLException
     */
    int[] batchUpdate(Connection conn, Collection<? extends DBObject> modelList, int batchSize) throws SQLException;

    /**
     * @param conn
     * @param modelList
     * @param batchSize
     * @return
     * @throws SQLException
     */
    int[] batchDelete(Connection conn, Collection<? extends DBObject> modelList, int batchSize) throws SQLException;

    /**
     * @param conn
     * @param clz
     * @return
     */
    boolean createTable(Connection conn, Class<?> clz);

    /**
     * @param conn
     * @param obj
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T extends DBObject> T fetchLinks(Connection conn, T obj) throws SQLException;

    /**
     * 查询连接
     * @param conn
     * @param obj
     * @param fields
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T extends DBObject> T fetchLinks(Connection conn, T obj, DBCascadeField... fields) throws SQLException;

    /**
     * 将对象插入数据库同时，也将符合一个正则表达式的所有关联字段关联的对象统统插入相应的数据库
     * <p>
     * 关于关联字段更多信息，请参看 '@One' | '@Many' | '@ManyMany' 更多的描述
     * @param conn
     * @param obj
     * @param fields 指定字段,控制力度更细,至少一个或多个 描述了什么样的关联字段将被关注。如果为 null，则表示全部的关联字段都会被插入
     * @return
     * @throws SQLException
     */
    int insertCascade(Connection conn, DBObject obj, DBCascadeField... fields) throws SQLException;

    /**
     * 仅将对象所有的关联字段插入到数据库中，并不包括对象本身
     * @param obj    数据对象
     * @param fields 字段名称，描述了什么样的关联字段将被关注。如果为 null，则表示全部的关联字段都会被插入
     * @return 数据对象本身
     * @see javax.persistence.OneToOne
     * @see javax.persistence.ManyToMany
     * @see javax.persistence.OneToMany
     */
    <T extends DBObject> T insertLinks(Connection con, T obj, DBCascadeField... fields) throws SQLException;

    /**
     * 将对象的一个或者多个，多对多的关联信息，插入数据表
     * @param obj    对象
     * @param fields 正则表达式，描述了那种多对多关联字段将被执行该操作
     * @return 对象自身
     * @see javax.persistence.ManyToMany
     */
    <T extends DBObject> T insertRelation(Connection con, T obj, DBCascadeField... fields) throws SQLException;

    /**
     * 将对象更新的同时，也将符合一个正则表达式的所有关联字段关联的对象统统更新
     * <p>
     * 关于关联字段更多信息，请参看 '@One' | '@Many' | '@ManyMany' 更多的描述
     * @param conn
     * @param obj    数据对象
     * @param fields 指定字段,控制力度更细,至少一个或多个,描述了什么样的关联字段将被关注。如果为 null，则表示全部的关联字段都会被更新
     * @return
     * @throws SQLException
     */
    int updateCascade(Connection conn, DBObject obj, DBCascadeField... fields) throws SQLException;

    /**
     * 仅更新对象所有的关联字段，并不包括对象本身
     * @param obj    数据对象
     * @param fields 字段名称，描述了什么样的关联字段将被关注。如果为 null，则表示全部的关联字段都会被更新
     * @return 数据对象本身
     * @see javax.persistence.OneToOne
     * @see javax.persistence.ManyToMany
     * @see javax.persistence.OneToMany
     */
    <T extends DBObject> T updateLinks(Connection con, T obj, DBCascadeField... fields) throws SQLException;

    /**
     * 多对多关联是通过一个中间表将两条数据表记录关联起来。
     * <p>
     * 而这个中间表可能还有其他的字段，比如描述关联的权重等
     * <p>
     * 这个操作可以让你一次更新某一个对象中多个多对多关联的数据
     * @param con    数据库连接
     * @param obj    数据对象
     * @param fields 字段名称，描述了那种多对多关联字段将被执行该操作
     * @return 共有多少条数据被更新
     * @see javax.persistence.ManyToMany
     */
    <T extends DBObject> int updateRelation(Connection con, T obj, DBCascadeField... fields) throws SQLException;


    /**
     * 将对象删除的同时，也将符合一个正则表达式的所有关联字段关联的对象统统删除
     *
     * <b style=color:red>注意：</b>
     * <p>
     * Java 对象的字段会被保留，这里的删除，将只会删除数据库中的记录
     * <p>
     * 关于关联字段更多信息，请参看 '@One' | '@Many' | '@ManyMany' 更多的描述
     * @param conn
     * @param obj
     * @param fields 指定字段,控制力度更细,至少一个或多个 描述了什么样的关联字段将被关注。如果为 null，则表示全部的关联字段都会被删除
     * @param <T>
     * @return
     * @throws SQLException
     */
    <T extends DBObject> int deleteCascade(Connection conn, T obj, DBCascadeField... fields) throws SQLException;

    /**
     * 仅删除对象所有的关联字段，并不包括对象本身。
     *
     * <b style=color:red>注意：</b>
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
    <T extends DBObject> int deleteLinks(Connection con, T obj, DBCascadeField... fields) throws SQLException;

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
    <T extends DBObject> int deleteRelation(Connection con, T obj, DBCascadeField... fields) throws SQLException;
}
