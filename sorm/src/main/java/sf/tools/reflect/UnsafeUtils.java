package sf.tools.reflect;

import sf.tools.utils.Assert;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * 使用sun.misc.Unsafe进行一些特殊操作的工具类
 * @author
 */
@SuppressWarnings("restriction")
public final class UnsafeUtils {

    static sun.misc.Unsafe unsafe;
    public static long stringoffset;
    public static boolean enable = System.getProperty("disable.unsafe") == null;

    static {
        try {
            Class<?> clazz = Class.forName("sun.misc.Unsafe");
            Field field = clazz.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
            java.lang.reflect.Field stringValue = String.class.getDeclaredField("value");
            stringoffset = unsafe.objectFieldOffset(stringValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取Unsafe对象
     * @return
     */
    public final static sun.misc.Unsafe getUnsafe() {
        return unsafe;
    }

    /**
     * 不使用反射直接创造对象，注意类的构造方法不会被执行
     * @param clz
     * @return 被构造的对象
     */
    @SuppressWarnings("unchecked")
    public final static <T> T newInstance(Class<T> clz) {
        try {
            return (T) unsafe.allocateInstance(clz);
        } catch (InstantiationException e) {
            throw new IllegalStateException(e.getCause());
        }
    }


    /**
     * 用指定的ClassLoader加载二进制数据为class
     * @param className
     * @param data
     * @param i
     * @param length
     * @param classLoader
     * @return
     */
    public final static Class<?> defineClass(String className, byte[] data, int i, int length, ClassLoader classLoader) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("the input class data is empty!");
        }
        if (length < 1 || i + length < data.length) {
            throw new IllegalArgumentException("the input length is invalid!");
        }
        if (className == null || className.length() == 0) {
            throw new IllegalArgumentException("the class name is invalid!" + className);
        }
        Assert.notNull(classLoader, "");
//		LogUtil.debug("Unsafe load class ["+className+"] to "+classLoader);
        return unsafe.defineClass(className, data, i, length, classLoader, null);
    }

    /**
     * 返回String对象中的char[]数组。<br>
     * 修改String 中的char[]是十分危险的，尤其是当被修改的char[]是属于常量池的String时，会发生十分难以检测的，不可预期的问题。
     * <p>
     * 因此这个方法用来：
     * 1、获取String中的char[]，用于读取和遍历。
     * 2、只有当非常确信String所引用的char[]不在常量池中时，可以进行修改。如果一个常量string进行substring操作，会产生的一个不完全对象的String。
     * 这个string的char[]引用上一个string的char[]，因此只有确信string对象是通过拼接等方式重新生成时，才能使用此方法
     * <p>
     * <p>
     * 这个方法的意义所在：
     * string.toCharArray();方法和这个很相似，但是性能上要稍微差一点。这个差别非常小。
     * 以长度为8的string来测试，运行一万次，toCharArray耗时3000us(当string比较大时可能更慢)，调用这个方法耗时约1000us(由于不用创建新对象)。
     * <p>
     * 实际测试发现，通过toCharArray来遍历对象要比下面的方法更耗时。
     * for(int j=0;j<s.length();j++){
     * char c=s.charAt(j);
     * }
     * 而这种方式和unsafe的方式其实耗时差距不大。
     * @param str
     * @return
     */
    public final static char[] getValue(String str) {
        return (char[]) unsafe.getObject(str, stringoffset);
    }

    /**
     * @param obj
     * @param fieldOffset
     * @param valueClz
     * @param value
     */
    public final static void setValue(Object obj, long fieldOffset, Class<?> valueClz, Object value) {
        if (valueClz == byte.class) {
            byte b = (byte) value;
            unsafe.putByte(obj, fieldOffset, b);
        } else if (valueClz == boolean.class) {
            boolean b = (boolean) value;
            unsafe.putBoolean(obj, fieldOffset, b);
        } else if (valueClz == char.class) {
            char b = (char) value;
            unsafe.putChar(obj, fieldOffset, b);
        } else if (valueClz == short.class) {
            short b = (short) value;
            unsafe.putShort(obj, fieldOffset, b);
        } else if (valueClz == int.class) {
            int b = (int) value;
            unsafe.putInt(obj, fieldOffset, b);
        } else if (valueClz == long.class) {
            long b = (long) value;
            unsafe.putLong(obj, fieldOffset, b);
        } else if (valueClz == float.class) {
            float b = (float) value;
            unsafe.putFloat(obj, fieldOffset, b);
        } else if (valueClz == double.class) {
            double b = (double) value;
            unsafe.putDouble(obj, fieldOffset, b);
        } else {
            unsafe.putObject(obj, fieldOffset, value);
        }
    }

    @SuppressWarnings("unchecked")
    public final static <T> T getValue(Object obj, long fieldOffset, Class<T> valueClz) {
        Object value;
        if (valueClz == byte.class) {
            value = unsafe.getByte(obj, fieldOffset);
        } else if (valueClz == boolean.class) {
            value = unsafe.getBoolean(obj, fieldOffset);
        } else if (valueClz == char.class) {
            value = unsafe.getChar(obj, fieldOffset);
        } else if (valueClz == short.class) {
            value = unsafe.getShort(obj, fieldOffset);
        } else if (valueClz == int.class) {
            value = unsafe.getInt(obj, fieldOffset);
        } else if (valueClz == long.class) {
            value = unsafe.getLong(obj, fieldOffset);
        } else if (valueClz == float.class) {
            value = unsafe.getFloat(obj, fieldOffset);
        } else if (valueClz == double.class) {
            value = unsafe.getDouble(obj, fieldOffset);
        } else {
            value = unsafe.getObject(obj, fieldOffset);
        }
        return (T) value;
    }
}
