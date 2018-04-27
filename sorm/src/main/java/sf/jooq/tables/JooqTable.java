package sf.jooq.tables;

import org.jooq.*;
import org.jooq.impl.*;
import sf.database.DBField;
import sf.database.DBObject;
import sf.database.dialect.DBDialect;
import sf.database.meta.ColumnMapping;
import sf.database.meta.MetaHolder;
import sf.database.meta.TableMapping;
import sf.database.util.SQLUtils;
import sf.tools.StringUtils;

import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JooqTable<R extends Record> extends TableImpl<R> {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected final Map<DBField, TableField<R, ?>> map = new LinkedHashMap<>();

    private Class<? extends DBObject> clz;
    private Schema schema;
    private String comment = "";
    UniqueKey<R> primaryKey = null;
    Index primaryIndex = null;
    List<UniqueKey<R>> uniqueKeys = new ArrayList<>();
    List<Index> indexs = new ArrayList<>();

    @Override
    public Class<R> getRecordType() {
        return (Class<R>) JooqRecord.class;
    }

    protected JooqTable(Name name) {
        super(name);
    }

    protected JooqTable(Name name, Schema schema) {
        super(name, schema);
    }

    protected JooqTable(Name name, Schema schema, Table<?> aliased) {
        super(name, schema, (Table<R>) aliased);
    }

    protected JooqTable(Name name, Schema schema, Table<R> aliased, Field<?>[] parameters) {
        super(name, schema, aliased, parameters);
    }

    protected JooqTable(Name name, Schema schema, Table<R> aliased, Field<?>[] parameters, String comment) {
        super(name, schema, aliased, parameters, comment);
    }

    protected JooqTable(Class<? extends DBObject> clz, Name name, Schema schema, Table<R> aliased, Field<?>[] parameters, String comment) {
        super(name, schema, aliased, parameters, comment);
        init(clz);
    }


    public JooqTable(Class<? extends DBObject> clz) {
        this(DSL.name(MetaHolder.getMeta(clz).getTableName()), getSchema(clz));
        init(clz);
    }

    private void init(Class<? extends DBObject> clz) {
        this.clz = clz;
        this.schema = getSchema(clz);
        TableMapping tm = MetaHolder.getMeta(clz);
        this.comment = getComment(tm);
        for (Map.Entry<DBField, ColumnMapping> entry : tm.getSchemaMap().entrySet()) {
            TableField<R, ?> tableField = getBeanMappingType(entry.getValue());
            map.put(entry.getKey(), tableField);
        }
        primaryKey = createPrimaryKey(clz);
        uniqueKeys.add(primaryKey);
        primaryIndex = createPrimaryIndex(clz);
        indexs.add(primaryIndex);
    }

    private static String getComment(TableMapping tm) {
        String comment = "";
        if (tm.getComment() != null) {
            comment = tm.getComment().value();
        }
        return comment;
    }

    private static Schema getSchema(Class<? extends DBObject> clz) {
        TableMapping tm = MetaHolder.getMeta(clz);
        String schema = tm.getSchema();
        String catalog = tm.getCatalog();
        if (StringUtils.isNotBlank(schema)) {
            if (catalog == null) {
                return DSL.schema(DSL.name(schema));
            } else {
                return DSL.schema(DSL.name(catalog, schema));
            }
        }
        return null;
    }

    /**
     * 获取列
     * @param field
     * @param <R>
     * @param <T>
     * @return
     */
    public <R extends Record, T> TableField<R, T> column(DBField field) {
        TableField<? extends Record, ?> expression = map.get(field);
        if (expression == null) {
            throw new RuntimeException("字段不存在");
        }
        return (TableField<R, T>) expression;
    }

    /**
     * 获取列,快速写法
     * @param field
     * @param <R>
     * @param <T>
     * @return
     */
    public <R extends Record, T> TableField<R, T> c(DBField field) {
        return column(field);
    }

    public List<TableField<? extends Record, ?>> getTableFields() {
        List<TableField<? extends Record, ?>> list = new ArrayList<>();
        for (Map.Entry<DBField, TableField<R, ?>> entry : map.entrySet()) {
            list.add(entry.getValue());
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JooqTable<R> as(String alias) {
        return new JooqTable<R>(clz, DSL.name(alias), schema, this, null, comment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JooqTable<R> as(Name alias) {
        return new JooqTable<R>(clz, alias, schema, this, null, comment);
    }

    /**
     * Rename this table
     */
    @Override
    public JooqTable<R> rename(String name) {
        return new JooqTable<R>(clz, DSL.name(name), schema, null, null, comment);
    }

    /**
     * Rename this table
     */
    @Override
    public JooqTable<R> rename(Name name) {
        return new JooqTable<R>(clz, name, schema, null, null, comment);
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

    protected UniqueKey<R> createPrimaryKey(Class<? extends DBObject> clz) {
        TableMapping tm = MetaHolder.getMeta(clz);
        int i = 0;
        TableField<R, ?>[] pkTableFields = new TableField[tm.getPkFields().size()];
        for (ColumnMapping cm : tm.getPkFields()) {
            pkTableFields[i] = map.get(cm.getField());
            i++;
        }
        if (pkTableFields.length > 0) {
            return Internal.createUniqueKey((Table<R>) this, pkTableFields);
        }
        return null;
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

    protected Index createPrimaryIndex(Class<? extends DBObject> clz) {
        TableMapping tm = MetaHolder.getMeta(clz);
        int i = 0;
        TableField<R, ?>[] pkTableFields = new TableField[tm.getPkFields().size()];
        for (ColumnMapping cm : tm.getPkFields()) {
            pkTableFields[i] = map.get(cm.getField());
            i++;
        }
        if (pkTableFields.length > 0) {
            OrderField<?>[] of = new OrderField[pkTableFields.length];
            for (int j = 0; j < pkTableFields.length; j++) {
                of[j] = pkTableFields[j];
            }
            return Internal.createIndex("PRIMARY", this, of, true);
        }
        return null;
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

    /**
     * @param cm
     * @return
     */
    protected <T> TableField<R, T> getBeanMappingType(ColumnMapping cm) {
        String columnName = SQLUtils.getNoWrapperColumnName(cm.getRawColumnName());
        String comment = "";
        if (cm.getComment() != null) {
            comment = cm.getComment().value();
        }
        DataType<T> type = getSQLDataType(cm);
        return createField(columnName, type, this, comment);
    }

    private <T> DataType<T> getSQLDataType(ColumnMapping cm) {
        DataType<?> type = null;
        Class<?> mirror = cm.getClz();
        // jdk8日期
        if (mirror == LocalDate.class) {
            type = SQLDataType.LOCALDATE;
        } else if (mirror == LocalDateTime.class) {
            type = SQLDataType.LOCALDATETIME;
        } else if (mirror == LocalTime.class) {
            type = SQLDataType.LOCALTIME;
        } else {
            switch (cm.getSqlType()) {
                case Types.CHAR: {
                    int length = DBDialect.getColumnLength(cm);
                    type = SQLDataType.CHAR(length);
                    break;
                }
                case Types.VARCHAR: {
                    int length = DBDialect.getColumnLength(cm);
                    type = SQLDataType.VARCHAR(length);
                    break;
                }
                case Types.TINYINT: {
                    type = SQLDataType.TINYINT;
                    break;
                }
                case Types.SMALLINT: {
                    type = SQLDataType.SMALLINT;
                    break;
                }
                case Types.INTEGER: {
                    type = SQLDataType.INTEGER;
                    break;
                }
                case Types.BIGINT: {
                    type = SQLDataType.BIGINT;
                    break;
                }
                case Types.FLOAT: {
                    type = SQLDataType.FLOAT;
                    break;
                }
                case Types.DOUBLE:
                    // 用默认精度
                    type = SQLDataType.DOUBLE;
                    break;
                case Types.BOOLEAN: {
                    type = SQLDataType.BOOLEAN;
                    break;
                }
                case Types.DECIMAL: {
                    // BigDecimal
                    int scale = DBDialect.getColumnScale(cm);
                    int precision = DBDialect.getColumnPrecision(cm);
                    if (scale > 0 && precision > 0) {
                        type = SQLDataType.DECIMAL(precision, scale);
                    } else {
                        throw new UnsupportedOperationException("Unsupport type!");
                    }
                    break;
                }
                case Types.NUMERIC: {
                    int scale = DBDialect.getColumnScale(cm);
                    int precision = DBDialect.getColumnPrecision(cm);
                    if (precision > 0) {
                        type = SQLDataType.NUMERIC(precision, scale);
                    } else {
                        type = SQLDataType.NUMERIC(255, 0);
                    }
                    break;
                }
                case Types.DATE:
                    type = SQLDataType.DATE;
                    break;
                case Types.TIME:
                    type = SQLDataType.TIME;
                    break;
                case Types.TIMESTAMP:
                    type = SQLDataType.TIMESTAMP;
                    break;
                case Types.BLOB:
                    type = SQLDataType.BLOB;
                    break;
                case Types.CLOB: {
                    int length = DBDialect.getColumnLength(cm);
                    if (cm.isLob() || (cm.getColumn() != null && length >= 1000)) {
                        type = SQLDataType.CLOB;
                    }
                    break;
                }
                default:
                    throw new NullPointerException("type is null!");
            }
        }
        return (DataType<T>) type;
    }

    public Map<DBField, TableField<R, ?>> getMap() {
        return map;
    }

    public Class<? extends DBObject> getClz() {
        return clz;
    }

    public void setClz(Class<? extends DBObject> clz) {
        this.clz = clz;
    }

    /**
     * 克隆
     * @param <R>
     * @return
     */
    public <R extends Record> JooqTable<R> cloneNew() {
        JooqTable<R> table = new JooqTable<>(clz);
        return table;
    }
}
