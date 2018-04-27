package sf.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.ForeignKey;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPathBase;
import sf.common.CaseInsensitiveMap;
import sf.database.util.SQLUtils;
import sf.tools.utils.Assert;
import sf.tools.utils.CollectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * 动态关系表模型匹配
 * @param <A>
 */
public class DynamicSQLRelationalPath<A> extends RelationalPathBase<Object> {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * 字段名称,对应的列
     */
    protected final Map<String, Expression<?>> map = new CaseInsensitiveMap<>();

    private static Field columnMetadataField = null;

    static {
        try {
            columnMetadataField = RelationalPathBase.class.getDeclaredField("columnMetadata");
            columnMetadataField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Expression<?>> getMap() {
        return map;
    }

    private DynamicSQLRelationalPath(Class<?> type, String variable, String schema, String table) {
        super(type, variable, schema, table);
    }

    private DynamicSQLRelationalPath(Class<?> type, PathMetadata metadata, String schema, String table) {
        super(type, metadata, schema, table);
    }


    /**
     * @param variable 默认为table名称
     */
    public DynamicSQLRelationalPath(String variable) {
        this(CaseInsensitiveMap.class, forVariable(variable), "null", variable);
    }

    /**
     * @param variable 默认为table名称
     * @param schema
     * @param table
     */
    public DynamicSQLRelationalPath(String variable, String schema, String table) {
        this(CaseInsensitiveMap.class, forVariable(variable), schema, table);
    }

    /**
     * @param metadata
     * @param schema
     * @param table
     */
    public DynamicSQLRelationalPath(PathMetadata metadata, String schema, String table) {
        this(Map.class, metadata, schema, table);
    }

    /**
     * 添加动态表模型.
     * @param columnMetadata 格式为ColumnMetadata.named("available").withIndex(2).ofType(Types.BIT).withSize(1)
     * @param columnType
     */
    public void addColumn(ColumnMetadata columnMetadata, Class<?> columnType) {
        Assert.notNull(columnMetadata, "column metadata is null");
        Assert.notNull(columnMetadata.getName(), "column metadata name is null");
        Assert.notNull(columnMetadata.getJdbcType(), "column metadata sql type is null");
        Assert.notNull(columnType, "columnType is null");
        String columnName = columnMetadata.getName();
        Expression<?> path = getBeanMappingType(columnName, columnType);
        map.put(columnName, path);
        //添加到列中
        addMetadata((Path<?>) path, columnMetadata);
    }

    /**
     * 删除动态表模型.
     * @param columnNames
     */
    public void removeColumns(String... columnNames) {
        Assert.notNull(columnNames, "column metadata is null");
        Assert.notEmpty(columnNames, "column metadata is empty");
        Map<Path<?>, ColumnMetadata> pathColumnMetadataMap = Collections.emptyMap();
        try {
            pathColumnMetadataMap = (Map<Path<?>, ColumnMetadata>) columnMetadataField.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        for (Iterator<Map.Entry<Path<?>, ColumnMetadata>> it = pathColumnMetadataMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Path<?>, ColumnMetadata> entry = it.next();
            ColumnMetadata metadata = entry.getValue();
            for (String column : columnNames) {
                if (metadata.getName().equalsIgnoreCase(column)) {
                    it.remove();
                    break;
                }
            }
        }
    }

    /**
     * 添加主键,需要先添加列后才能运行.
     * @param columns
     */
    public void addPrimaryKey(String... columns) {
        Assert.notEmpty(columns, "column metadata is empty");
        int i = 0;
        Path<?>[] pkPaths = new Path[columns.length];

        for (Path path : all()) {
            for (String column : columns) {
                if (Objects.equals(column, path.getMetadata().getElement())) {
                    pkPaths[i] = (Path<?>) path;
                    i++;
                    break;
                }
            }
        }
        if (pkPaths.length > 0) {
            createPrimaryKey(pkPaths);
        }
    }

    /**
     * 获取动态表字段 可以转换为NumberPath,BooleanPath...
     * @param columnName
     * @param <T>
     * @return
     */
    public <T> SimpleExpression<T> column(String columnName) {
        return expression(columnName);
    }

    /**
     * @param field
     * @param <S>
     * @return
     */
    public <S> SimpleExpression<S> expression(String field) {
        return column(field, SimpleExpression.class);
    }

    /**
     * @param field
     * @param <P>
     * @return
     */
    public <P> Path<P> path(String field) {
        return column(field, Path.class);
    }

    public BooleanPath bool(String field) {
        return column(field, BooleanPath.class);
    }

    public StringPath string(String field) {
        return column(field, StringPath.class);
    }

    public <T extends Number & Comparable<?>> NumberPath<T> number(String field) {
        return column(field, NumberPath.class);
    }

    public <T extends Enum<T>> EnumPath<T> enums(String field) {
        return column(field, EnumPath.class);
    }

    public <T> SimplePath<T> simple(String field) {
        return column(field, SimplePath.class);
    }

    public <A, E> ArrayPath<A, E> array(String field) {
        return column(field, ArrayPath.class);
    }

    public <T extends Comparable> ComparablePath<T> comparable(String field) {
        return column(field, ComparablePath.class);
    }

    public <T extends Comparable> DatePath<T> date(String field) {
        return column(field, DatePath.class);
    }

    public <T extends Comparable> DateTimePath<T> dateTime(String field) {
        return column(field, DateTimePath.class);
    }

    public <T extends Comparable> TimePath<T> time(String field) {
        return column(field, TimePath.class);
    }


    public <E, Q extends SimpleExpression<? super E>> CollectionPath<E, Q> collection(String field) {
        return column(field, CollectionPath.class);
    }


    public <E, Q extends SimpleExpression<? super E>> SetPath<E, Q> set(String field) {
        return column(field, SetPath.class);
    }


    public <E, Q extends SimpleExpression<? super E>> ListPath<E, Q> list(String field) {
        return column(field, ListPath.class);
    }

    public <K, V, E extends SimpleExpression<? super V>> MapPath<K, V, E> map(String field) {
        return column(field, MapPath.class);
    }

    private <T> T column(String field, Class<T> clz) {
        Expression<?> path = map.get(field);
        if (path == null) {
            //字段不存在
            throw new RuntimeException("field not exists!");
        }
        if (clz != null) {
            if (clz.isAssignableFrom(path.getClass())) {
                return (T) path;
            } else {
                //类型转换错误
                throw new ClassCastException("field cast error!");
            }
        } else {
            return (T) path;
        }
    }


    public Expression<?> getBeanMappingType(String rawColumnName, Class<?> columnType) {
        Class<?> mirror = columnType;
        String columnName = SQLUtils.getNoWrapperColumnName(rawColumnName);
        // String and char
        if (mirror == String.class)
            return createString(rawColumnName);

        // Boolean
        if (mirror == Boolean.class || mirror == Boolean.TYPE)
            return createBoolean(rawColumnName);
        // Byte
        if (mirror == Byte.class || mirror == Byte.TYPE)
            return createNumber(columnName, Byte.class);
        // Short
        if (mirror == Short.class || mirror == Short.TYPE)
            return createNumber(columnName, Short.class);
        // Int
        if (mirror == Integer.class || mirror == Integer.TYPE)
            return createNumber(columnName, Integer.class);
        // Float
        if (mirror == Float.class || mirror == Float.TYPE)
            return createNumber(columnName, Float.class);
        // Double
        if (mirror == Double.class || mirror == Double.TYPE)
            return createNumber(columnName, Double.class);
        // Long
        if (mirror == Long.class || mirror == Long.TYPE)
            return createNumber(columnName, Long.class);
        // BigDecimal
        if (mirror == BigDecimal.class)
            return createNumber(columnName, BigDecimal.class);
        //BigInteger
        if (mirror == BigInteger.class)
            return createNumber(columnName, BigInteger.class);

        // Enum
        if (mirror.isEnum())
            return createEnum(columnName, (Class) mirror);
        // Char
        if (mirror == CharSequence.class || mirror == Character.TYPE)
            return createSimple(columnName, Character.class);
        // Timestamp
        if (mirror == Timestamp.class)
            return createDateTime(columnName, Timestamp.class);
        // java.sql.Date
        if (mirror == java.sql.Date.class)
            return createDate(columnName, Date.class);
        // java.sql.Time
        if (mirror == java.sql.Time.class)
            return createTime(columnName, Timestamp.class);
        // Calendar
        if (mirror == LocalDate.class)
            return createDate(columnName, LocalDate.class);
        if (mirror == LocalDateTime.class)
            return createDateTime(columnName, LocalDateTime.class);
        if (mirror == LocalTime.class)
            return createTime(columnName, LocalTime.class);

        // java.util.Date
        if (mirror == java.util.Date.class)
            return createDateTime(columnName, java.util.Date.class);
        // byte[]
        if (mirror.isArray()) {
            ArrayPath<?, A> path = createArray(columnName, (Class<? super A>) mirror);
            return path;
        }
//        if (cm.getType()!=null&&cm.getType().value()==ObjectJsonMapping.class){
//            if (Set.class.isAssignableFrom(cm.getClz())){
//
//            }else if (Map.class.isAssignableFrom(cm.getClz())){
//
//            }else if(List.class.isAssignableFrom(cm.getClz())){
//
//            }
//
//            return createSimple(columnName, (Class<? super Object>) cm.getClz());
//        }
        // 默认情况
        SimplePath<A> path = createSimple(columnName, (Class<? super A>) mirror);
        return path;
    }

    protected Expression<?> copyPath(Path<?> path) {
        Class<?> mirror = path.getType();
        String columnName = path.getMetadata().getName();
        // String and char
        if (path.getClass() == StringPath.class)
            return createString(columnName);

        // Boolean
        if (path.getClass() == BooleanPath.class)
            return createBoolean(columnName);
        // number
        if (path.getClass() == NumberPath.class)
            return createNumber(columnName, (Class<? super Number>) mirror);

        // Enum
        if (path.getClass() == EnumPath.class)
            return createEnum(columnName, (Class) mirror);
        // Char
        if (mirror == CharSequence.class || mirror == Character.TYPE)
            return createSimple(columnName, Character.class);

        // 日期
        if (path.getClass() == DatePath.class)
            return createDate(columnName, (Class<? super Comparable>) mirror);
        if (path.getClass() == DateTimePath.class)
            return createDateTime(columnName, (Class<? super Comparable>) mirror);
        if (path.getClass() == TimePath.class)
            return createTime(columnName, (Class<? super Comparable>) mirror);

        // byte[]
        if (path.getClass() == ArrayPath.class) {
            return createArray(columnName, (Class<? super Object>) mirror);
        }
        if (path.getClass() == SetPath.class) {
            SetPath sp = (SetPath) path;
            return createSet(columnName, sp.getElementType(), (Class) getQueryType(sp), PathInits.DIRECT2);
        }
        if (path.getClass() == MapPath.class) {
            MapPath mp = (MapPath) path;
            return createMap(columnName, mp.getKeyType(), mp.getValueType(), (Class) getQueryType(mp));
        }
        if (path.getClass() == ListPath.class) {
            ListPath lp = (ListPath) path;
            return createList(columnName, lp.getElementType(), (Class) getQueryType(lp), PathInits.DIRECT2);
        }
        // 默认情况
        return createSimple(columnName, (Class<? super Object>) mirror);
    }

    private Class<?> getQueryType(Path<?> lp) {
        try {
            Field field = lp.getClass().getDeclaredField("queryType");
            field.setAccessible(true);
            Class<?> clz = (Class<?>) field.get(lp);
            return clz;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 克隆一个新的原始对象.
     * @param variable
     * @return
     */
    public <A> DynamicSQLRelationalPath<A> cloneNew(String variable) {
        DynamicSQLRelationalPath<A> relationalPath = new DynamicSQLRelationalPath(variable, this.getSchemaName(), this.getTableName());
        relationalPath.map.putAll(this.map);
        List<Path<?>> columns = this.getColumns();
        for (Path<?> c : columns) {
            ColumnMetadata cmd = this.getMetadata(c);
            relationalPath.addColumn(cmd, c.getMetadata().getPathType().getType());
            Path<?> p = (Path<?>) relationalPath.copyPath(c);
            relationalPath.addMetadata(p, cmd);
        }
        PrimaryKey pk = this.getPrimaryKey();
        if (pk != null && CollectionUtils.isNotEmpty(pk.getLocalColumns())) {
            Path[] paths = (Path[]) pk.getLocalColumns().toArray(new Path[pk.getLocalColumns().size()]);
            relationalPath.createPrimaryKey(paths);
        }
        Collection<ForeignKey<?>> foreignKeys = this.getForeignKeys();
        if (CollectionUtils.isNotEmpty(foreignKeys)) {
            for (ForeignKey<?> fk : foreignKeys) {
                relationalPath.createForeignKey(fk.getLocalColumns(), fk.getForeignColumns());
            }
        }
        Collection<ForeignKey<?>> inverseForeignKeys = this.getInverseForeignKeys();
        if (CollectionUtils.isNotEmpty(inverseForeignKeys)) {
            for (ForeignKey<?> fk : inverseForeignKeys) {
                relationalPath.createInvForeignKey(fk.getLocalColumns(), fk.getForeignColumns());
            }
        }
        return relationalPath;
    }
}
