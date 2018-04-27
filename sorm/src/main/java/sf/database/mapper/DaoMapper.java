package sf.database.mapper;

import sf.common.wrapper.Page;
import sf.database.DBObject;
import sf.database.dao.DBMethod;

import java.util.List;

/**
 * @Author: sxf
 * @Date: 2018/3/25 11:04
 */
public interface DaoMapper<T extends DBObject> {
    /**
     * 通用插入，插入一个实体对象到数据库,返回主键
     * @param entity
     */
    T insert(T entity);


    /**
     * 批量插入实体
     * @param list
     * @param fast 是否快速插入 如果是将不返还主键
     */
    void insertBatch(List<T> list, boolean fast);

    /**
     * 根据主键更新对象,对象set过才被更新
     * @param entity
     * @return
     */
    int updateById(T entity);

    /**
     * 根据主键删除对象，如果对象是复合主键，传入对象本生即可
     * @param key
     * @return
     */
    int deleteById(Object key);

    /**
     * 根据主键获取对象，如果对象不存在，则会抛出一个Runtime异常
     * @param key
     * @return
     */
    T unique(Object key);

    /**
     * 根据主键获取对象，如果对象不存在，返回null
     * @param key
     * @return
     */
    T single(Object key);

    /**
     * 根据主键获取对象，如果在事物中执行会添加数据库行级锁(select * from table where id = ? for
     * update)，如果对象不存在，返回null
     * @param key
     * @return
     */
    T lock(Object key);

    /**
     * 返回实体对应的所有数据库记录
     * @return
     */
    List<T> all();

    /**
     * 返回实体对应的一个范围的记录
     * @param start
     * @param size
     * @return
     */
    List<T> all(int start, int size);

    /**
     * 返回实体在数据库里的总数
     * @return
     */
    long allCount();

    /**
     * 模板查询，返回符合模板得所有结果。beetlsql将取出非null值（日期类型排除在外），从数据库找出完全匹配的结果集
     * @param entity
     * @return
     */
    List<T> template(T entity);

    /**
     * 模板查询，返回一条结果,如果没有，返回null
     * @param entity
     * @return
     */
    T templateOne(T entity);

    List<T> template(T entity, int start, int size);

    void templatePage(Page<T> query);

    /**
     * 符合模板得个数
     * @param entity
     * @return
     */
    long templateCount(T entity);

    /**
     * 执行一个jdbc sql模板查询
     * @param sql
     * @param args
     * @return
     */
    List<T> execute(String sql, Object... args);

    /**
     * 执行一个更新的jdbc sql
     * @param sql
     * @param args
     * @return
     */
    int executeUpdate(String sql, Object... args);

    DBMethod getDBMethod();
}
