package sf.database;

import sf.database.meta.TableMapping;

import java.util.*;

/**
 * orm执行的sql上下文语境
 */
public class OrmContext {

    /**
     * sql
     */
    private String sql;


    /**
     * 参数值包含 键和值.
     */
    private List<OrmParameter> paras;
    private Map<String, Object> env = null;
    private boolean isUpdate = false;
    private Object result;


    /**
     * 预处理的结果,主要用于乐观锁更新后的返回值的处理.
     */
    private List<OrmParameter> preResultParas;

    /**
     * 返回类型,非list,可以为Map.class,java bean,或者基本类型.
     */
    private Class<?> resultBeanClass;

    private TableMapping tableMapping;

    /**
     * 执行的class,标明在哪个类上执行的方法,
     */
    private Class<?> execClz;

    public OrmContext() {
    }

    public OrmContext(TableMapping tableMapping) {
        this.tableMapping = tableMapping;
    }

    public OrmContext(String sqlId, String sql, List<OrmParameter> paras, boolean isUpdate) {
        this.sql = sql;
        this.paras = paras;
        this.isUpdate = isUpdate;
    }

    public void putEnv(String key, Object value) {
        if (env == null) {
            env = new HashMap<String, Object>();
        }
        env.put(key, value);
    }

    public Object getEnv(String key) {
        if (env == null) {
            return null;
        } else {
            return env.get(key);
        }
    }

    public List<Object> getValues() {
        if (paras != null) {
            List<Object> list = new ArrayList<>();
            for (OrmParameter p : paras) {
                Object value = p.getValue();
                list.add(value);
            }
            return list;
        } else {
            return Collections.emptyList();
        }
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<OrmParameter> getParas() {
        return paras;
    }

    public void setParas(List<OrmParameter> paras) {
        this.paras = paras;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setUpdate(boolean update) {
        isUpdate = update;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public TableMapping getTableMapping() {
        return tableMapping;
    }

    public OrmContext setTableMapping(TableMapping tableMapping) {
        this.tableMapping = tableMapping;
        return this;
    }

    public Class<?> getExecClz() {
        return execClz;
    }

    public void setExecClz(Class<?> execClz) {
        this.execClz = execClz;
    }

    public Class<?> getResultBeanClass() {
        return resultBeanClass;
    }

    public void setResultBeanClass(Class<?> resultBeanClass) {
        this.resultBeanClass = resultBeanClass;
    }

    public List<OrmParameter> getPreResultParas() {
        return preResultParas;
    }

    public void setPreResultParas(List<OrmParameter> preResultParas) {
        this.preResultParas = preResultParas;
    }
}
