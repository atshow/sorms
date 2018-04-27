package sf.database.jdbc.rowmapper;

import sf.common.CaseInsensitiveMap;
import sf.database.DBObject;
import sf.database.jdbc.type.TypeHandler;
import sf.database.meta.ColumnMapping;
import sf.database.meta.MetaHolder;
import sf.database.meta.TableMapping;
import sf.database.util.DBUtils;
import sf.database.util.OrmValueUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 对象转换
 * @param <T>
 */
public class BeanRowMapper<T> implements RowMapper<T> {
    private Class<T> beanClass;
    public static boolean useTail = false;

    /**
     * @param beanClass
     */
    public BeanRowMapper(Class<T> beanClass) {
        this.beanClass = beanClass;
    }

    /**
     * @param beanClass
     * @param useTail   是否使用tail
     */
    public BeanRowMapper(Class<T> beanClass, boolean useTail) {
        this.beanClass = beanClass;
        this.useTail = useTail;
    }

    /**
     * @param rs
     * @return
     * @throws SQLException
     */
    @Override
    public T handle(ResultSet rs, int rowNum) throws SQLException {
        try {
            T bean = OrmValueUtils.instance(beanClass);
            TableMapping table = MetaHolder.getMeta(beanClass);
            ResultSetMetaData rsmd = rs.getMetaData();
            int cols = rsmd.getColumnCount();
            for (int i = 1; i <= cols; i++) {
                String columnName = rsmd.getColumnLabel(i);
                if (columnName == null || columnName.length() == 0) {
                    columnName = rsmd.getColumnName(i);
                }
                boolean find = false;
                ColumnMapping cm = DBUtils.getColumnByDBName((CaseInsensitiveMap<ColumnMapping>) table.getMetaFieldMap(), columnName);
                if (cm != null) {
                    TypeHandler<Object> handler = cm.getHandler();
                    Object value = handler.get(rs, columnName);
                    if (!cm.getClz().isPrimitive() || value != null) {
                        //不为基础类型或者 值不为空,则插值
                        OrmValueUtils.setValue(bean, cm, value);
                    }
                    find = true;
                }

                if (!find) {
                    find = stackFind(rs, bean, columnName);
                }

                if (!find && useTail) {
                    //未找到将值设置在tail中.
                    if (DBObject.class.isAssignableFrom(beanClass)) {
                        DBObject d = (DBObject) bean;
                        Map<String, Object> map = d.getTail();
                        if (map == null) {
                            map = new CaseInsensitiveMap<>();
                            d.setTail(map);
                        }
                        map.put(columnName, rs.getObject(columnName));
                    }
                }
            }
            if (DBObject.class.isAssignableFrom(beanClass)) {
                DBObject d = (DBObject) bean;
                d.clearUpdate();
            }
            return bean;
        } catch (Exception e) {
            throw new SQLException("Can't set bean property.", e);
        }
    }

    /**
     * 迭代查找赋值
     * @param rs
     * @param bean
     * @param columnName
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws SQLException
     */
    private boolean stackFind(ResultSet rs, Object bean, String columnName)
            throws InstantiationException, IllegalAccessException, SQLException {

        boolean flag = false;
        TableMapping table = MetaHolder.getMeta(beanClass);
        List<ColumnMapping> allOne2On2 = table.getAllOne2One();
        if (allOne2On2 != null) {
            for (ColumnMapping cm : allOne2On2) {
                ColumnMapping subCm = DBUtils.getColumnByDBName((CaseInsensitiveMap<ColumnMapping>) MetaHolder.getMeta(cm.getClz()).getMetaFieldMap(), columnName);
                if (subCm != null) {
                    Object obj = null;
                    Object innerObj = OrmValueUtils.getValue(bean, cm);
                    if (innerObj == null) {
                        obj = OrmValueUtils.instance(cm.getClz());
                    } else {
                        obj = innerObj;
                    }
                    TypeHandler<Object> handler = subCm.getHandler();
                    Object value = handler.get(rs, columnName);
                    if (value != null) {
                        OrmValueUtils.setValue(obj, subCm, value);
                        flag = true;
                    }
                    // 使用set字段方法,非method,可能对某些特殊方法执行不到
                    if (innerObj == null) {
                        OrmValueUtils.setValue(bean, cm, obj);
                    }
                    //清除updateValue
                    if (DBObject.class.isAssignableFrom(cm.getClz())) {
                        DBObject d = (DBObject) obj;
                        d.clearUpdate();
                    }
                } else {
                    Object innerObj = OrmValueUtils.getValue(bean, cm);
                    if (innerObj == null) {
                        innerObj = cm.getClz().newInstance();
                    }
                    // 迭代查找
                    boolean f = stackFind(rs, innerObj, columnName);
                    if (f) {
                        OrmValueUtils.setValue(bean, cm, innerObj);
                        f = true;
                        flag = true;
                    }
                }
            }
        }
        return flag;
    }

}
