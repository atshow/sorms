package sf.tools;

public class NumberUtils {
    public static <T extends Number> Object getTargetNumber(Number num, Class<?> clz) {
        if (clz == byte.class || clz == Byte.class) {
            return num.byteValue();
        } else if (clz == short.class || clz == Short.class) {
            return num.shortValue();
        } else if (clz == int.class || clz == Integer.class) {
            return num.intValue();
        } else if (clz == long.class || clz == Long.class) {
            return num.longValue();
        } else if (clz == float.class || clz == Float.class) {
            return num.floatValue();
        } else if (clz == double.class || clz == Double.class) {
            return num.doubleValue();
        }
        return num;
    }
}
