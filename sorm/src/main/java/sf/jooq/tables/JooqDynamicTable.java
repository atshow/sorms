package sf.jooq.tables;

import org.jooq.*;
import org.jooq.impl.*;
import sf.common.CaseInsensitiveMap;
import sf.jooq.Jooq;
import sf.tools.utils.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 动态表支持
 * @param <R>
 */
public class JooqDynamicTable<R extends Record> extends TableImpl<R> {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected final Map<String, TableField<R, ?>> map = new CaseInsensitiveMap<>();
    protected final Map<String, JooqField> fields = new CaseInsensitiveMap<>();

    private Schema schema;
    private Name name;
    private String comment = "";
    UniqueKey<R> primaryKey = null;
    Index primaryIndex = null;
    List<UniqueKey<R>> uniqueKeys = new ArrayList<>();
    List<Index> indexs = new ArrayList<>();

    static JooqDynamicTable<?> DEFAULT = new JooqDynamicTable<>((Name) null);

    @Override
    public Class<R> getRecordType() {
        return (Class<R>) JooqRecord.class;
    }

    protected JooqDynamicTable(Name name) {
        super(name);
    }

    public JooqDynamicTable(String name) {
        super(DSL.name(name));
    }

    protected JooqDynamicTable(Name name, Schema schema) {
        super(name, schema);
    }

    protected JooqDynamicTable(Name name, Schema schema, Table<?> aliased) {
        super(name, schema, (Table<R>) aliased);
    }

    protected JooqDynamicTable(Name name, Schema schema, Table<R> aliased, Field<?>[] parameters) {
        super(name, schema, aliased, parameters);
    }

    protected JooqDynamicTable(Name name, Schema schema, Table<R> aliased, Field<?>[] parameters, String comment) {
        super(name, schema, aliased, parameters, comment);
    }


    /**
     * @param name    表名
     * @param catalog
     * @param schema
     * @param comment
     */
    public JooqDynamicTable(String name, String catalog, String schema, String comment) {
        this(name, catalog, schema, null, comment);
    }

    /**
     * @param name    表名
     * @param catalog
     * @param schema
     * @param aliased 别名
     * @param comment
     */
    public JooqDynamicTable(String name, String catalog, String schema, Table aliased, String comment) {
        super(DSL.name(name), Jooq.getSchema(catalog, schema), (Table<R>) aliased, null, comment);
        this.schema = Jooq.getSchema(catalog, schema);
        this.name = DSL.name(name);
    }


    /**
     * 添加列
     * @param columnName
     * @param type
     * @param comment
     * @param <R>
     * @param <T>
     * @return
     */
    public <R extends Record, T> TableField<Record, T> addColumn(String columnName, DataType<T> type, String comment) {
        TableField<Record, T> tf = createField(columnName, type, (Table<Record>) this, comment);
        JooqField jf = new JooqField();
        jf.setName(columnName);
        jf.setType(type);
        jf.setTable(this);
        jf.setComment(comment);
        jf.setConverter(null);
        jf.setBinding(null);
        fields.put(jf.getName(), jf);
        return tf;
    }

    /**
     * 删除列
     * @param columnNames
     */
    public void removeColumn(String... columnNames) {
        JooqVisitor.removeFields(this, columnNames);
        if (columnNames != null) {
            for (String c : columnNames) {
                fields.remove(c);
            }
        }
    }


    public <R extends Record, T> TableField<R, T> column(String field) {
        TableField<? extends Record, ?> expression = map.get(field);
        if (expression == null) {
            throw new RuntimeException("字段不存在");
        }
        return (TableField<R, T>) expression;
    }

    public <R extends Record, T> TableField<R, T> c(String field) {
        return column(field);
    }

    public List<TableField<? extends Record, ?>> getTableFields() {
        List<TableField<? extends Record, ?>> list = new ArrayList<>();
        for (Map.Entry<String, TableField<R, ?>> entry : map.entrySet()) {
            list.add(entry.getValue());
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JooqDynamicTable<R> as(String alias) {
        JooqDynamicTable table = new JooqDynamicTable<R>(DSL.name(alias), schema, this, null, comment);
        table.fields.putAll(this.fields);
        table.addDBColumn();
        return table;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JooqDynamicTable<R> as(Name alias) {
        JooqDynamicTable table = new JooqDynamicTable<R>(alias, schema, this, null, comment);
        table.fields.putAll(this.fields);
        table.addDBColumn();
        return table;
    }

    /**
     * Rename this table
     */
    @Override
    public JooqDynamicTable<R> rename(String name) {
        JooqDynamicTable table = new JooqDynamicTable<R>(DSL.name(name), schema, null, null, comment);
        table.fields.putAll(this.fields);
        table.addDBColumn();
        return table;
    }

    /**
     * Rename this table
     */
    @Override
    public JooqDynamicTable<R> rename(Name name) {
        JooqDynamicTable table = new JooqDynamicTable<R>(name, schema, null, null, comment);
        table.fields.putAll(this.fields);
        table.addDBColumn();
        return table;
    }

    @Override
    public UniqueKey<R> getPrimaryKey() {
        return primaryKey;
    }

    @Override
    public List<UniqueKey<R>> getKeys() {
        return uniqueKeys;
    }

    public void setKey(UniqueKey<R> key) {
        if (key == null) {
            throw new NullPointerException("uniqueKey is null");
        }
        this.uniqueKeys.add(key);
    }

    /**
     * 添加主键
     * @param columns
     * @return
     */
    public UniqueKey<R> addPrimaryKey(String... columns) {
        Assert.notEmpty(columns, "");
        UniqueKey uniqueKey = null;
        int i = 0;
        TableField<R, ?>[] pkTableFields = new TableField[columns.length];
        for (TableField tf : getTableFields()) {
            for (String column : columns) {
                if (Objects.equals(column, tf.getName())) {
                    pkTableFields[i] = tf;
                    i++;
                    break;
                }
            }
        }
        if (pkTableFields.length > 0) {
            uniqueKey = Internal.createUniqueKey((Table<R>) this, pkTableFields);
            uniqueKeys.add(uniqueKey);
            //添加主键索引
            addIndex("PRIMARY", columns);
        }
        return uniqueKey;
    }

    /**
     * 添加索引
     * @param indexName
     * @param columns
     * @return
     */
    public Index addIndex(String indexName, String... columns) {
        Assert.notEmpty(columns, "");
        Index index = null;
        int i = 0;
        TableField<R, ?>[] tableFields = new TableField[columns.length];
        for (TableField tf : getTableFields()) {
            for (String column : columns) {
                if (Objects.equals(column, tf.getName())) {
                    tableFields[i] = tf;
                    i++;
                    break;
                }
            }
        }
        if (tableFields.length > 0) {
            OrderField<?>[] of = new OrderField[tableFields.length];
            for (int j = 0; j < tableFields.length; j++) {
                of[j] = tableFields[j];
            }
            //PRIMARY
            index = Internal.createIndex(indexName, this, of, true);
            indexs.add(index);
        }
        return index;
    }

    @Override
    public List<Index> getIndexes() {
        return indexs;
    }

    public void setIndex(Index index) {
        if (index == null) {
            throw new NullPointerException("index is null");
        }
        this.indexs.add(index);
    }


    @Override
    public Schema getSchema() {
        return schema;
    }


    public Map<String, TableField<R, ?>> getMap() {
        return map;
    }

    protected void addDBColumn() {
        for (Map.Entry<String, JooqField> entry : fields.entrySet()) {
            JooqField jf = entry.getValue();
            TableField tf = createField(jf.getName(), jf.getType(), this, jf.getComment(), jf.getConverter(), jf.getBinding());
            map.put(jf.getName(), tf);
        }
    }

}
