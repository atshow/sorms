package sf.tools.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class PropertyHold implements Property {
    /**
     * 字段名
     */
    private Field field;
    private String name;
    private Method setter;
    private Method getter;

    /**
     * 泛型原始类型
     */
    private Class<?>[] rawTypes;

    /**
     * 内存中的偏移量(使用unsafe实现)
     */
    private long fieldOffset;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isReadable() {
        return field != null;
    }

    @Override
    public boolean isWriteable() {
        return field != null;
    }

    @Override
    public Object get(Object obj) {
        try {
            return getter.invoke(obj);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void set(Object obj, Object value) {
        try {
            setter.invoke(obj, value);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<?> getType() {
        if (field != null)
            return field.getType();
        if (getter != null)
            return getter.getReturnType();
        return setter.getParameterTypes()[0];
    }

    @Override
    public Type getGenericType() {
        if (field != null)
            return field.getGenericType();
        if (getter != null)
            return getter.getGenericReturnType();
        return setter.getGenericParameterTypes()[0];
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Method getSetter() {
        return setter;
    }

    public void setSetter(Method setter) {
        this.setter = setter;
    }

    public Method getGetter() {
        return getter;
    }

    public void setGetter(Method getter) {
        this.getter = getter;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getFieldOffset() {
        return fieldOffset;
    }

    public void setFieldOffset(long fieldOffset) {
        this.fieldOffset = fieldOffset;
    }

    public Class<?>[] getRawTypes() {
        return rawTypes;
    }

    public void setRawTypes(Class<?>[] rawTypes) {
        this.rawTypes = rawTypes;
    }
}
