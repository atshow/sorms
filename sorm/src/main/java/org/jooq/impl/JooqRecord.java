package org.jooq.impl;

import org.jooq.TableField;
import sf.database.DBField;
import sf.jooq.tables.JooqTable;

import java.util.Map;

public class JooqRecord extends RecordImpl<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object> {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private JooqTable<?> jooqTable;


    public JooqRecord() {
    }

    public void setValue(DBField field, Object value) {
        Map<DBField, ?> map = jooqTable.getMap();
        TableField<?, Object> tf = (TableField<?, Object>) map.get(field);
        setValue(tf, value);
    }

    public Object getValue(DBField field) {
        Map<DBField, ?> map = jooqTable.getMap();
        TableField<?, ?> tf = (TableField<?, ?>) map.get(field);
        return get(tf);
    }

    public JooqTable<?> getJooqTable() {
        return jooqTable;
    }

    void setJooqTable(JooqTable<?> jooqTable) {
        this.jooqTable = jooqTable;
    }
}
