package sf.database.util;

import sf.database.meta.ColumnMapping;
import sf.database.meta.MetaHolder;
import sf.tools.reflect.UnsafeUtils;
import sf.tools.utils.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * orm的设置值的方法
 */
public class OrmValueUtils {
    /**
     * 快速方法 使用unsafe实现
     */
    public static boolean fast = false;
    public static boolean byField = false;


    /**
     * 实例化,提供快速实例化,或标准实例化
     * @param clz
     * @param <T>
     * @return
     */
    public static <T> T instance(Class<T> clz) {
        T t = null;
        if (fast) {
            t = UnsafeUtils.newInstance(clz);
        } else {
            try {
                t = clz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return t;
    }

    /**
     * 设置值
     * @param obj
     * @param cm
     * @param value
     * @param <T>
     */
    public static <T> void setValue(T obj, ColumnMapping cm, Object value) {
        if (fast) {
            setValueFieldUnsafe(obj, cm, value);
        } else if (byField) {
            setValueFieldReflect(obj, cm, value);
        } else {
            setValueMethodReflect(obj, cm, value);
        }
    }

    /**
     * 使用field 反射设置值
     * @param obj
     * @param cm
     * @param value
     * @param <T>
     */
    private static <T> void setValueFieldReflect(T obj, ColumnMapping cm, Object value) {
        Field f = cm.getFieldAccessor().getField();
        if (!f.isAccessible()) {
            f.setAccessible(true);
        }
        ReflectionUtils.setField(cm.getFieldAccessor().getField(), obj, value);
    }

    /**
     * 使用method 反射设置值
     * @param obj
     * @param cm
     * @param value
     * @param <T>
     */
    private static <T> void setValueMethodReflect(T obj, ColumnMapping cm, Object value) {
        cm.getFieldAccessor().set(obj, value);
    }

    /**
     * 使用unsafe设置值
     * @param obj
     * @param cm
     * @param value
     * @param <T>
     */
    private static <T> void setValueFieldUnsafe(T obj, ColumnMapping cm, Object value) {
        UnsafeUtils.setValue(obj, cm.getFieldAccessor().getFieldOffset(), cm.getClz(), value);
    }

    /**
     * @param obj
     * @param cm
     * @param <T>
     * @param <U>
     * @return
     */
    public static <T, U> U getValue(T obj, ColumnMapping cm) {
        if (MetaHolder.getMeta(obj.getClass()) != cm.getMeta()) {
            throw new RuntimeException("类型不一致");
        }
        if (fast) {
            return getValueFieldUnsafe(obj, cm);
        } else if (byField) {
            return getValueFieldReflect(obj, cm);
        } else {
            return getValueMethodReflect(obj, cm);
        }
    }

    /**
     * @param obj
     * @param cm
     * @param <T>
     * @param <U>
     * @return
     */
    private static <T, U> U getValueFieldReflect(T obj, ColumnMapping cm) {
        Field f = cm.getFieldAccessor().getField();
        if (!f.isAccessible()) {
            f.setAccessible(true);
        }
        Object value = ReflectionUtils.getField(f, obj);
        return (U) value;
    }

    /**
     * @param obj
     * @param cm
     * @param <T>
     * @param <U>
     * @return
     */
    private static <T, U> U getValueMethodReflect(T obj, ColumnMapping cm) {
        Object value = cm.getFieldAccessor().get(obj);
        return (U) value;
    }

    /**
     * @param obj
     * @param cm
     * @param <T>
     * @param <U>
     * @return
     */
    private static <T, U> U getValueFieldUnsafe(T obj, ColumnMapping cm) {
        U value = UnsafeUtils.getValue(obj, cm.getFieldAccessor().getFieldOffset(), (Class<U>) cm.getClz());
        return value;
    }
}
