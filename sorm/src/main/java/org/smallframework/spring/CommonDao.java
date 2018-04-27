package org.smallframework.spring;

import sf.common.wrapper.Page;

import javax.persistence.NonUniqueResultException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface CommonDao {
    /**
     * 插入记录(不带级联)
     * @param entity 要插入的记录
     * @return 插入后的记录
     */
    <T> T insert(T entity);

    /**
     * 插入记录（带级联的插入记录）
     * @param entity
     * @return
     */
    <T> T insertCascade(T entity);

    /**
     * 在记录已经存在的情况下更新，否则插入记录
     * @param entity 要持久化的记录
     */
    void persist(Object entity);

    /**
     * 在记录已经存在的情况下更新，否则插入记录
     * @param entity
     * @return
     */
    <T> T merge(T entity);

    /**
     * 删除记录(不带级联)
     * @param entity 待删除对象（模板）.
     *               <ul>
     *               <li>如果对象是{@link IQueryableEntity}设置了Query条件，按query条件查询。 否则——
     *               </li>
     *               <li>如果设置了主键值，按主键查询，否则——</li>
     *               <li>按所有设置过值的字段作为条件查询。</li>
     *               </ul>
     */
    int remove(Object entity);

    /**
     * 删除记录(带级联)
     * @param entity <ul>
     *               <li>如果对象是{@link IQueryableEntity}设置了Query条件，按query条件查询。 否则——
     *               </li>
     *               <li>如果设置了主键值，按主键查询，否则——</li>
     *               <li>按所有设置过值的字段作为条件查询。</li>
     *               </ul>
     */
    int removeCascade(Object entity);

    /**
     * 根据模板的对象删除记录。
     * @param entity     作为删除条件的对象（模板）
     * @param properties 作为删除条件的字段名。当不指定properties时，首先检查entity当中是否设置了主键，如果有主键按主键删除，
     *                   否则按所有非空字段作为匹配条件。
     * @return
     */
    <T> int removeByExample(T entity, String... properties);

    /**
     * 查询列表
     * @param data 查询请求。
     *             <ul>
     *             <li>如果设置了Query条件，按query条件查询。 否则——</li>
     *             <li>如果设置了主键值，按主键查询，否则——</li>
     *             <li>按所有设置过值的字段作为条件查询。</li>
     *             </ul>
     * @return 结果
     */
    <T> List<T> find(T data);

    /**
     * 根据主键查询
     * @param type 类
     * @param id   主键
     * @return 查询结果
     * @since 1.10
     */
    <T> T load(Class<T> type, Serializable... id);

    /**
     * 根据样例查找
     * @param entity   查询条件
     * @param property 作为查询条件的字段名。当不指定properties时，首先检查entity当中是否设置了主键，如果有主键按主键删除，
     *                 否则按所有非空字段作为匹配条件。
     * @return 查询结果
     */
    <T> List<T> findByExample(T entity, String... properties);

    /**
     * 查询并分页
     * @param data  查询请求
     * @param start 起始记录，offset。从0开始。
     * @param limit 限制记录条数。如每页10条传入10。
     * @return
     */
    <T> Page<T> findAndPage(T data, int start, int limit);

    /**
     * 查询一条记录，如果结果不唯一则抛出异常
     * @param data
     * @param unique 要求查询结果是否唯一。为true时，查询结果不唯一将抛出异常。为false时，查询结果不唯一仅取第一条。
     * @return 查询结果
     * @throws NonUniqueResultException 结果不唯一
     */
    <T> T load(T data);

    /**
     * 根据查询查询一条记录
     * @param entity
     * @param unique true表示结果必须唯一，false则允许结果不唯一仅获取第一条记录
     * @return 查询结果
     * @throws NonUniqueResultException 结果不唯一
     */
    public <T> T load(T entity, boolean unique);

    /**
     * 更新记录(不带级联)
     * @param entity 要更新的对象
     * @return 影响的记录条数
     */
    <T> int update(T entity);

    /**
     * 更新记录（带级联）
     * @param entity
     * @return
     */
    <T> int updateCascade(T entity);

    /**
     * 更新记录
     * @param entity   要更新的对象
     * @param property where字段值
     * @return 影响的记录条数
     */
    <T> int updateByProperty(T entity, String... property);

    /**
     * 根据指定的几个字段作为条件来更新记录
     * @param entity    要更新的对象
     * @param setValues 要设置的属性和值
     * @param property  where字段值
     * @return 影响的记录条数
     */
    <T> int update(T entity, Map<String, Object> setValues, String... property);

    /**
     * 执行命名查询 {@linkplain NamedQueryConfig 什么是命名查询}
     * @param nqName 命名查询名称
     * @param type   返回类型
     * @param params 查询参数
     * @return 查询结果
     */
    <T> List<T> findByNq(String nqName, Class<T> type, Map<String, Object> params);

    /**
     * 使用命名查询查找并分页 {@linkplain NamedQueryConfig 什么是命名查询}
     * @param nqName 命名查询名称
     * @param type   返回类型
     * @param params 查询参数
     * @param start  开始记录数，从0开始
     * @param limit  限制结果条数
     * @return 查询结果
     */
    <T> Page<T> findAndPageByNq(String nqName, Class<T> type, Map<String, Object> params, int start, int limit);

    /**
     * 使用命名查询执行（更新/创建/删除）等操作 {@linkplain NamedQueryConfig 什么是命名查询}
     * @param nqName 命名查询名称
     * @param params sql参数
     * @return 查询结果
     */
    int executeNq(String nqName, Map<String, Object> params);

    /**
     * 执行指定的SQL（更新/创建/删除）等操作
     * @param sql   SQL语句,可使用 {@linkplain NativeQuery 增强的SQL (参见什么是E-SQL条目)}。
     * @param param
     * @return 查询结果
     */
    int executeQuery(String sql, Map<String, Object> param);

    /**
     * 根据指定的SQL查找
     * @param sql        SQL语句,可使用 {@linkplain NativeQuery 增强的SQL (参见什么是E-SQL条目)}。
     *
     *                   <pre>
     *                   <code>
     *                    String sql="select * from table where 1=1 and id=:id<int> and name like :name<$string>";
     *                    Map<String, Object> params = new HashMap<String,Object>();
     *                    params.put(id,123);
     *                    params.put(name,"Join");
     *                    session.findByQuery(sql,ResultClass.class, params); //根据SQL语句查询，返回类型为ResultClass。
     *                   <code>
     *                              </pre>
     * @param retutnType 返回类型
     * @param params     绑定变量参数
     * @return 查询结果
     */
    <T> List<T> findByQuery(String sql, Class<T> retutnType, Map<String, Object> params);

    /**
     * 根据指定的SQL查找并分页
     * @param sql        SQL语句,可使用 {@linkplain NativeQuery 增强的SQL (参见什么是E-SQL条目)}。
     * @param retutnType 返回结果类型
     * @param params     绑定变量参数
     * @param start      起始记录行，第一条记录从0开始。
     * @param limit      每页记录数
     * @return 查询结果
     */
    <T> Page<T> findAndPageByQuery(String sql, Class<T> retutnType, Map<String, Object> params, int start, int limit);

    /**
     * 根据主键的值加载一条记录
     * @param entityClass
     * @param primaryKey
     * @return 查询结果
     */
    <T> T loadByPrimaryKey(Class<T> entityClass, Serializable primaryKey);

    /**
     * 根据主键的值批量加载记录 (不支持复合主键)
     * @param entityClass 实体类
     * @param values      多个主键值
     */
    <T> List<T> loadByPrimaryKeys(Class<T> entityClass, List<? extends Serializable> values);

    /**
     * 根据某个指定字段进行查找
     * @param meta
     * @param propertyName
     * @param value
     * @return
     */
    <T> List<T> findByField(Class<T> meta, String propertyName, Object value);

    /**
     * 根据指定的字段值读取单个记录
     * @param meta   数据库表的元模型. {@linkplain ITableMetadata 什么是元模型}
     * @param unique true要求查询结果必须唯一。false允许结果不唯一，但仅取第一条。
     * @param field
     * @param id
     * @return 查询结果
     * @throws NonUniqueResultException 结果不唯一
     */
    <T> T loadByField(Class<T> meta, String field, Serializable key, boolean unique);

    /**
     * 根据指定的字段值删除记录
     * @param meta  数据库表的元模型. {@linkplain ITableMetadata 什么是元模型}
     * @param field
     * @param value
     * @return 影响记录行数
     */
    <T> int removeByField(Class<T> meta, String field, Serializable value);

    /**
     * 批量插入
     * @param entities 要写入的对象列表
     * @return 影响记录行数
     */
    <T> int batchInsert(List<T> entities);

    /**
     * 批量插入
     * @param entities 要写入的对象列表
     * @param doGroup  是否对每条记录重新分组。
     *                 {@linkplain jef.database.Batch#isGroupForPartitionTable
     *                 什么是重新分组}
     * @return 影响记录行数
     */
    <T> int batchInsert(List<T> entities, Boolean doGroup);

    /**
     * 批量删除
     * @param entities 要删除的对象列表
     * @return 影响记录行数
     */
    <T> int batchDelete(List<T> entities);

    /**
     * 批量删除
     * @param entities 要删除的对象列表
     * @param doGroup  是否对每条记录重新分组。
     *                 {@linkplain jef.database.Batch#isGroupForPartitionTable
     *                 什么是重新分组}
     * @return 影响记录行数
     */
    <T> int batchDelete(List<T> entities, Boolean doGroup);

    /**
     * 批量（按主键）更新
     * @param entities 要写入的对象列表
     * @return 影响记录行数
     */
    <T> int batchUpdate(List<T> entities);

    /**
     * 批量（按主键）更新
     * @param entities 要写入的对象列表
     * @param doGroup  是否对每条记录重新分组。
     *                 {@linkplain jef.database.Batch#isGroupForPartitionTable
     *                 什么是重新分组}
     * @return 影响记录行数
     */
    <T> int batchUpdate(List<T> entities, Boolean doGroup);
}
