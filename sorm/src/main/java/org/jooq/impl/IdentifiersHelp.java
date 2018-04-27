package org.jooq.impl;

import org.jooq.SQLDialect;
import sf.tools.enums.DynimcEnumUtils;
import sf.tools.enums.EnumHelp;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

public class IdentifiersHelp {
    public static String SQLSERVER = "SQLSERVER";
    public static String SQLSERVER_NAME = "SQLSERVER";

    public static Field field;

    static {
        try {
            field = Identifiers.class.getDeclaredField("QUOTES");
            field.setAccessible(true);
            /*去除final修饰符的影响，将字段设为可修改的*/
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加sqlserver
     */
    public static void addOuterDB() {
        if (!exist(SQLSERVER)) {
//            EnumBuster<SQLDialect> buster = new EnumBuster<SQLDialect>(SQLDialect.class, SelectQueryImpl.class);
//            SQLDialect ANGRY = buster.make(SQLSERVER, 18, new Class[]{String.class, boolean.class}, new Object[]{SQLSERVER_NAME, false});


            DynimcEnumUtils.addEnum(SQLDialect.class, SQLSERVER, new Class[]{String.class, boolean.class}, new Object[]{SQLSERVER_NAME, false});
            EnumMap<SQLDialect, String[][]> QUOTES = new EnumMap<SQLDialect, String[][]>(SQLDialect.class);
            List<Field> r = EnumHelp.findRelatedSwitchFields(SQLDialect.class, new Class[]{SelectQueryImpl.class,
                    Alias.class, DefaultBinding.class, DefaultRenderContext.class});
            EnumHelp.setOrdinal(r, SQLDialect.valueOf(SQLSERVER));
//            buster.addByValue(SQLDialect.valueOf(SQLSERVER));
            QUOTES.put(SQLDialect.valueOf(SQLSERVER), new String[][]{
                    {"[", "\""},
                    {"]", "\""},
                    {"[]", "\"\""}
            });
            try {
                field.set(null, QUOTES);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean exist(String nameForDialect) {
        boolean exist = false;
        for (SQLDialect s : SQLDialect.values()) {
            if (Objects.equals(s.name(), nameForDialect)) {
                exist = true;
                break;
            }
        }
        return exist;
    }

}
