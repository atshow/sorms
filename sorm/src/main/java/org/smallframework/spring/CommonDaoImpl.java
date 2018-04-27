package org.smallframework.spring;

import sf.common.wrapper.Page;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class CommonDaoImpl implements CommonDao {

    @Override
    public <T> T insert(T entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T insertCascade(T entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void persist(Object entity) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> T merge(T entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int remove(Object entity) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int removeCascade(Object entity) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public <T> int removeByExample(T entity, String... properties) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public <T> List<T> find(T data) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T load(Class<T> type, Serializable... id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> List<T> findByExample(T entity, String... properties) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> Page<T> findAndPage(T data, int start, int limit) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T load(T data) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T load(T entity, boolean unique) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> int update(T entity) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public <T> int updateCascade(T entity) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public <T> int updateByProperty(T entity, String... property) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public <T> int update(T entity, Map<String, Object> setValues, String... property) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public <T> List<T> findByNq(String nqName, Class<T> type, Map<String, Object> params) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> Page<T> findAndPageByNq(String nqName, Class<T> type, Map<String, Object> params, int start, int limit) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int executeNq(String nqName, Map<String, Object> params) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int executeQuery(String sql, Map<String, Object> param) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public <T> List<T> findByQuery(String sql, Class<T> retutnType, Map<String, Object> params) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> Page<T> findAndPageByQuery(String sql, Class<T> retutnType, Map<String, Object> params, int start,
                                          int limit) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T loadByPrimaryKey(Class<T> entityClass, Serializable primaryKey) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> List<T> loadByPrimaryKeys(Class<T> entityClass, List<? extends Serializable> values) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> List<T> findByField(Class<T> meta, String propertyName, Object value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T loadByField(Class<T> meta, String field, Serializable key, boolean unique) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> int removeByField(Class<T> meta, String field, Serializable value) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public <T> int batchInsert(List<T> entities) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public <T> int batchInsert(List<T> entities, Boolean doGroup) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public <T> int batchDelete(List<T> entities) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public <T> int batchDelete(List<T> entities, Boolean doGroup) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public <T> int batchUpdate(List<T> entities) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public <T> int batchUpdate(List<T> entities, Boolean doGroup) {
        // TODO Auto-generated method stub
        return 0;
    }

}
