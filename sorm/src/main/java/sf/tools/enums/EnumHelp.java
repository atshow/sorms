package sf.tools.enums;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class EnumHelp {
    public static <E extends Enum<E>> List<Field> findRelatedSwitchFields(Class<E> clazz, Class[] switchUsers) {
        List<Field> result = new LinkedList<Field>();
        try {
            for (Class switchUser : switchUsers) {
                String name = switchUser.getName();
                int i = 0;
                while (true) {
                    try {
                        Class suspect = Class.forName(String.format("%s$%d", name, ++i));
                        Field[] fields = suspect.getDeclaredFields();
                        for (Field field : fields) {
                            String fieldName = field.getName();
                            if (fieldName.startsWith("$SwitchMap$") && fieldName.endsWith(clazz.getSimpleName())) {
                                field.setAccessible(true);
                                result.add(field);
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not fix switch", e);
        }
        return result;
    }

    public static <E extends Enum<E>> void setOrdinal(List<Field> result, E e) {
        E[] enums = (E[]) e.getClass().getEnumConstants();
        int[] all = new int[enums.length];
        for (int i = 0; i < all.length; i++) {
            all[i] = i;
        }
        for (Field f : result) {
            try {
                DynimcEnumUtils.setFailsafeFieldValue(f, null, all);
            } catch (NoSuchFieldException e1) {
                e1.printStackTrace();
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            }
        }
    }
}
