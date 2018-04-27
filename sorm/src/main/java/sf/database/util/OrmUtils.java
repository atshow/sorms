package sf.database.util;

import sf.database.DBField;
import sf.database.DBObject;
import sf.database.annotations.BindDataSource;
import sf.database.meta.ColumnMapping;
import sf.database.meta.MetaHolder;
import sf.database.meta.TableMapping;
import sf.tools.StringUtils;
import sf.tools.utils.ReflectionUtils;

import java.lang.reflect.Field;

public class OrmUtils {

    /**
     * 对使用了BindDataSource注解做处理.
     * @param clz
     * @return
     */
    public static String getBindDataSource(Class<?> clz) {
        if (DBObject.class.isAssignableFrom(clz)) {
            TableMapping tm = MetaHolder.getMeta(clz);
            if (tm != null) {
                return tm.getBindDsName();
            }
        } else {
            BindDataSource bds = clz.getAnnotation(BindDataSource.class);
            if (bds != null && StringUtils.isNotBlank(bds.value())) {
                return bds.value();
            }
        }
        return null;
    }


    /**
     * 获取值
     * @param obj
     * @param fields
     * @return
     */
    public static Object[] getDataObjectValues(DBObject obj, DBField... fields) {
        TableMapping table = MetaHolder.getMeta(obj.getClass());
        Object[] values = new Object[fields.length];
        int i = 0;
        for (DBField field : fields) {
            ColumnMapping cm = table.getSchemaMap().get(field);
            if (cm != null) {
                values[i] = OrmValueUtils.getValue(obj, cm);
            }
            i++;
        }
        return values;
    }

    /**
     * 获取值
     * @param obj
     * @param field
     * @return
     */
    public static Object getDataObjectValue(DBObject obj, DBField field) {
        TableMapping table = MetaHolder.getMeta(obj.getClass());
        Object value = null;
        ColumnMapping cm = table.getSchemaMap().get(field);
        if (cm != null) {
            value = OrmValueUtils.getValue(obj, cm);
        }
        return value;
    }

    public static Object getDataObjectValue(DBObject obj, Field f) {
        Object value = null;
        if (!f.isAccessible()) {
            f.setAccessible(true);
        }
        value = ReflectionUtils.getField(f, obj);
        return value;
    }

    public static Object getDataObjectValue(DBObject obj, String field) {
        TableMapping table = MetaHolder.getMeta(obj.getClass());
        DBField dbField = table.getFields().get(field);
        Object value = getDataObjectValue(obj, dbField);
        return value;
    }
}
