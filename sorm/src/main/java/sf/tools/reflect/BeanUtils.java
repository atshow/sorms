package sf.tools.reflect;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;

public class BeanUtils {
    /**
     * 包装类转换为原生类
     * @param wrapperClass
     * @return
     */
    public static Class<?> toPrimitiveClass(Class<?> wrapperClass) {
        if (wrapperClass == Integer.class) {
            return Integer.TYPE;
        } else if (wrapperClass == Byte.class) {
            return Byte.TYPE;
        } else if (wrapperClass == Short.class) {
            return Short.TYPE;
        } else if (wrapperClass == Long.class) {
            return Long.TYPE;
        } else if (wrapperClass == Float.class) {
            return Float.TYPE;
        } else if (wrapperClass == Double.class) {
            return Double.TYPE;
        } else if (wrapperClass == Character.class) {
            return Character.TYPE;
        } else if (wrapperClass == Boolean.class) {
            return Boolean.TYPE;
        } else {
            return wrapperClass;
        }
    }

    /**
     * 将8种原生类型的类转换为对应的包装的类型。
     */
    public static Class<?> toWrapperClass(Class<?> primitiveClass) {
        if (primitiveClass == Integer.TYPE)
            return Integer.class;
        if (primitiveClass == Long.TYPE)
            return Long.class;
        if (primitiveClass == Double.TYPE)
            return Double.class;
        if (primitiveClass == Short.TYPE)
            return Short.class;
        if (primitiveClass == Float.TYPE)
            return Float.class;
        if (primitiveClass == Character.TYPE)
            return Character.class;
        if (primitiveClass == Byte.TYPE)
            return Byte.class;
        if (primitiveClass == Boolean.TYPE)
            return Boolean.class;
        return primitiveClass;
    }

    /**
     * 深度复制，实参类必须实现Serializable接口
     * @param t
     * @return
     */
    public static <T extends Serializable> T deepCopy(T t) {
        return Objects.deepCopy(t);
    }

    public static Object getFieldValue(Object obj, String fieldName) {
        BeanInfo bi = null;
        Object value = null;
        try {
            bi = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] pds = bi.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                // Class<?> propertyType = pd.getPropertyType();
                Method readMethod = pd.getReadMethod();
                // Method writeMethod = pd.getWriteMethod();
                if (pd.getName().equals(fieldName)) {
                    value = readMethod.invoke(obj);
                    break;
                }
            }
        } catch (Exception e) {
            return new RuntimeException(e);
        }
        return value;
    }

}
