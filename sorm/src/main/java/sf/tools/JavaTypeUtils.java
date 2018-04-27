package sf.tools;

public class JavaTypeUtils {
    /**
     * 判断是否是数字类型
     * @param clz
     * @return
     */
    public static boolean isNumberClass(Class<?> clz) {
        if (Number.class.isAssignableFrom(clz) || clz == byte.class || clz == short.class || clz == int.class || clz == long.class
                || clz == float.class || clz == double.class) {
            return true;
        }
        return false;
    }
}
