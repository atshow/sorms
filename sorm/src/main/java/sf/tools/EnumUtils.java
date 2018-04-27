package sf.tools;

public class EnumUtils {
    /**
     * 获得枚举，根据EnumMapping 注解，如果没有，则使用枚举名称
     * @param c     枚举类
     * @param value 参考值
     * @return
     */
    public static Enum getEnumByValue(Class c, Object value) {
        if (!c.isEnum())
            throw new IllegalArgumentException(c.getName());
        Object[] enums = c.getEnumConstants();
        for (Object o : enums) {
            if (value == o) {
                return (Enum) o;
            }
        }
        return null;
    }

    public static Object getValueByEnum(Object en) {
        if (en == null) return null;
        Class c = en.getClass();
        return getEnumByValue(c, en);
    }
}
