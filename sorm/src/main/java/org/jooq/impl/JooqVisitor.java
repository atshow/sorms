package org.jooq.impl;

import org.jooq.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 打破访问限制.
 */
public class JooqVisitor {
    static Field dmlTable = null;

    static Field jooqColumnFields = null;

    static {
        try {
            jooqColumnFields = Fields.class.getDeclaredField("fields");
            jooqColumnFields.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取from的表数量
     * @param query
     * @return
     */
    public static List<? extends Table<?>> getSelectFromTableList(SelectQuery<?> query) {
        List<? extends Table<?>> tables = Collections.emptyList();
        if (query instanceof SelectQueryImpl) {
            SelectQueryImpl<?> impl = (SelectQueryImpl<?>) query;
            TableList tl = impl.getFrom();
            if (tl != null) {
                tables = new ArrayList<>(tl);
            }
        }
        return tables;
    }

    /**
     * 获取更新的表
     * @param update
     * @return
     */
    public static Table<?> getUpdateTable(Update<?> update) {
        UpdateImpl<?> impl = (UpdateImpl<?>) update;
        Query q = impl.getDelegate();
        if (q instanceof AbstractDMLQuery) {
            return getDMLTable((AbstractDMLQuery<?>) q);
        } else {
            return null;
        }
    }

    /**
     * 获取删除的表
     * @param delete
     * @return
     */
    public static Table<?> getDeleteTable(Delete<?> delete) {
        DeleteImpl<?> impl = (DeleteImpl<?>) delete;
        Query q = impl.getDelegate();
        if (q instanceof AbstractDMLQuery) {
            return getDMLTable((AbstractDMLQuery<?>) q);
        } else {
            return null;
        }
    }

    /**
     * 获取插入表
     * @param insert
     * @return
     */
    public static Table<?> getInsertTable(Insert<?> insert) {
        InsertImpl<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> impl = (InsertImpl<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) insert;
        Query q = impl.getDelegate();
        if (q instanceof AbstractDMLQuery) {
            return getDMLTable((AbstractDMLQuery<?>) q);
        } else {
            return null;
        }
    }

    /**
     * @param query
     * @return
     */
    private static Table<?> getDMLTable(AbstractDMLQuery<?> query) {
        Table<?> table = null;
        try {
            if (dmlTable == null) {
                Field f = AbstractDMLQuery.class.getDeclaredField("table");
                f.setAccessible(true);
                dmlTable = f;
            }
            table = (Table<?>) dmlTable.get(query);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return table;
    }

    public static Fields<?> getFields(TableImpl impl) {
        return impl.fields;
    }


    /**
     * 删除列
     * @param impl
     * @param fields
     */
    public static void removeFields(TableImpl impl, String... fields) {
        Fields feds = impl.fields;
        org.jooq.Field[] oldlFieds = feds.fields();
        List<org.jooq.Field> list = new ArrayList<>(Arrays.asList(oldlFieds));
        for (String f : fields) {
            org.jooq.Field t = feds.field(f);
            list.remove(t);
        }
        org.jooq.Field[] newFieds = new org.jooq.Field[list.size()];
        list.toArray(newFieds);
        try {
            jooqColumnFields.set(feds, newFieds);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
