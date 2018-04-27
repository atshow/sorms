package sf.database.mapper;

import sf.common.wrapper.Page;
import sf.database.DBObject;
import sf.database.dao.DBMethod;

import java.util.List;

/**
 * @Author: sxf
 * @Date: 2018/3/25 11:04
 */
public class DaoMapperImpl<T extends DBObject> implements DaoMapper<T> {
    @Override
    public T insert(T t) {
        return null;
    }

    @Override
    public void insertBatch(List<T> list, boolean fast) {
        // TODO Auto-generated method stub

    }

    @Override
    public int updateById(T entity) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int deleteById(Object key) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public T unique(Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public T single(Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public T lock(Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<T> all() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<T> all(int start, int size) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long allCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<T> template(T entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public T templateOne(T entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<T> template(T entity, int start, int size) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void templatePage(Page<T> query) {
        // TODO Auto-generated method stub

    }

    @Override
    public long templateCount(T entity) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<T> execute(String sql, Object... args) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int executeUpdate(String sql, Object... args) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public DBMethod getDBMethod() {
        // TODO Auto-generated method stub
        return null;
    }
}
