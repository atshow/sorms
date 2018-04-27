package sf.database.template.sql;

import java.util.*;

/**
 * 匹配后的sql
 */
public class SQLContext {
    /**
     * 模板id
     */
    private String sqlId;
    /**
     * 原始sql
     */
    private String originalSql;

    /**
     * parse后sql
     */
    private String afterSql;

    /**
     * 参数值包含 键和值.
     */
    private List<SQLParameter> paras;
    private Map<String, Object> env = null;
    private boolean isUpdate = false;
    private Object result;
    /**
     * 返回类型,非list,可以为Map.class,java bean,或者基本类型.
     */
    private Class<?> resultBeanClass;

    private Map<String, Object> inputParas;

    /**
     * 执行的class,标明在哪个类上执行的方法,
     */
    private Class<?> execClz;

    public SQLContext() {
    }

    public SQLContext(String sqlId, String originalSql, List<SQLParameter> paras, Map<String, Object> inputParas,
                      boolean isUpdate) {
        this.originalSql = originalSql;
        this.paras = paras;
        this.sqlId = sqlId;
        this.inputParas = inputParas;
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
            for (SQLParameter p : paras) {
                Object value = p.getValue();
                list.add(value);
            }
            return list;
        } else {
            return Collections.emptyList();
        }
    }

    public String getOriginalSql() {
        return originalSql;
    }

    public void setOriginalSql(String originalSql) {
        this.originalSql = originalSql;
    }

    public String getAfterSql() {
        return afterSql;
    }

    public void setAfterSql(String afterSql) {
        this.afterSql = afterSql;
    }

    public String getSqlId() {
        return sqlId;
    }

    public void setSqlId(String sqlId) {
        this.sqlId = sqlId;
    }

    public List<SQLParameter> getParas() {
        return paras;
    }

    public void setParas(List<SQLParameter> paras) {
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

    public Map<String, Object> getInputParas() {
        return inputParas;
    }

    public void setInputParas(Map<String, Object> inputParas) {
        this.inputParas = inputParas;
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
}
