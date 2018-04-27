package sf.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.RelationalPathBase;
import sf.database.DBField;
import sf.database.DBObject;
import sf.database.meta.ColumnMapping;
import sf.database.meta.MetaHolder;
import sf.database.meta.TableMapping;
import sf.database.util.SQLUtils;

import javax.persistence.TemporalType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * DBObject对应的QueryDSL模型.
 */
public class SQLRelationalPath<T extends DBObject> extends RelationalPathBase<T> implements Cloneable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * 字段列,值为SimpleExpression
     */
    private final Map<DBField, Expression<?>> map = new HashMap<>();

    private Class<T> clz;

    public Map<DBField, Expression<?>> getMap() {
        return map;
    }


    public SQLRelationalPath(Class<? extends DBObject> type) {
        this(MetaHolder.getMeta(type));
    }

    protected SQLRelationalPath(Class<? extends DBObject> type, String variable) {
        this(MetaHolder.getMeta(type), variable);
    }

    public SQLRelationalPath(TableMapping tm) {
        this(tm, tm.getTableName());
    }

    protected SQLRelationalPath(TableMapping tm, String variable) {
        super((Class<T>) tm.getThisType(), PathMetadataFactory.forVariable(variable), tm.getSchema(),
                tm.getTableName());
        init(tm);
    }

    private void init(TableMapping tm) {
        this.clz = (Class<T>) tm.getThisType();
        for (Map.Entry<DBField, ColumnMapping> entry : tm.getSchemaMap().entrySet()) {
            Expression<?> path = getBeanMappingType(entry.getValue());
            addMetadata(entry.getValue(), (Path<?>) path);
            map.put(entry.getKey(), path);
        }
        createPrimaryKey(tm);
    }

    protected SQLRelationalPath(Class<? extends T> type, String variable, String schema, String table) {
        super(type, variable, schema, table);
    }

    protected SQLRelationalPath(Class<? extends T> type, PathMetadata metadata, String schema, String table) {
        super(type, metadata, schema, table);
    }

    protected void createPrimaryKey(TableMapping tm) {
        int i = 0;
        Path<?>[] pkPaths = new Path[tm.getPkFields().size()];
        for (ColumnMapping cm : tm.getPkFields()) {
            pkPaths[i] = (Path<?>) map.get(cm.getField());
            i++;
        }
        if (pkPaths.length > 0) {
            createPrimaryKey(pkPaths);
        }
    }

    protected void addMetadata(Class<? extends T> type) {
        TableMapping tm = MetaHolder.getMeta(type);
        for (Map.Entry<DBField, ColumnMapping> entry : tm.getSchemaMap().entrySet()) {
            addMetadata(entry.getValue(), (Path<?>) map.get(entry.getKey()));
        }
    }

    protected void addMetadata(ColumnMapping cm, Path<?> path) {
        String columnName = SQLUtils.getNoWrapperColumnName(cm.getRawColumnName());
        ColumnMetadata cmd = ColumnMetadata.named(columnName).ofType(cm.getSqlType());
        if (cm.getColumn() != null) {
            cmd.withSize(cm.getColumn().length());
        }
        addMetadata(path, cmd);
    }

    /**
     * 可以转换为NumberPath,BooleanPath...<br>
     * 限制:此处不可使用in数组(in数组存在类型转换失败问题);解决办法可以直接传入List或Set对象,或者使用原始的Path.
     * @param field
     * @return
     */
    public <S> SimpleExpression<S> column(DBField field) {
        return expression(field);
    }

    /**
     * @param field
     * @param <S>
     * @return
     */
    public <S> SimpleExpression<S> expression(DBField field) {
        return column(field, SimpleExpression.class);
    }

    /**
     * @param field
     * @param <P>
     * @return
     */
    public <P> Path<P> path(DBField field) {
        return column(field, Path.class);
    }

    public BooleanPath bool(DBField field) {
        return column(field, BooleanPath.class);
    }

    public StringPath string(DBField field) {
        return column(field, StringPath.class);
    }

    public <T extends Number & Comparable<?>> NumberPath<T> number(DBField field) {
        return column(field, NumberPath.class);
    }

    public <T extends Enum<T>> EnumPath<T> enums(DBField field) {
        return column(field, EnumPath.class);
    }

    public <T> SimplePath<T> simple(DBField field) {
        return column(field, SimplePath.class);
    }

    public <A, E> ArrayPath<A, E> array(DBField field) {
        return column(field, ArrayPath.class);
    }

    public <T extends Comparable> ComparablePath<T> comparable(DBField field) {
        return column(field, ComparablePath.class);
    }

    public <T extends Comparable> DatePath<T> date(DBField field) {
        return column(field, DatePath.class);
    }


    public <T extends Comparable> DateTimePath<T> dateTime(DBField field) {
        return column(field, DateTimePath.class);
    }

    public <T extends Comparable> TimePath<T> time(DBField field) {
        return column(field, TimePath.class);
    }

    /**
     * 不支持
     * @param field
     * @return
     */
    private <E, Q extends SimpleExpression<? super E>> CollectionPath<E, Q> collection(DBField field) {
        return column(field, CollectionPath.class);
    }

    /**
     * 不支持
     * @param field
     * @return
     */
    private <E, Q extends SimpleExpression<? super E>> SetPath<E, Q> set(DBField field) {
        return column(field, SetPath.class);
    }

    /**
     * 不支持
     * @param field
     * @return
     */
    private <E, Q extends SimpleExpression<? super E>> ListPath<E, Q> list(DBField field) {
        return column(field, ListPath.class);
    }

    /**
     * 不支持
     * @param field
     * @return
     */
    private <K, V, E extends SimpleExpression<? super V>> MapPath<K, V, E> map(DBField field) {
        return column(field, MapPath.class);
    }

    private <T> T column(DBField field, Class<T> clz) {
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

    protected Expression<?> getBeanMappingType(ColumnMapping cm) {
        Class<?> mirror = cm.getClz();
        String columnName = SQLUtils.getNoWrapperColumnName(cm.getRawColumnName());
        TemporalType tt = cm.getTemporal() == null ? null : cm.getTemporal().value();
        return getBeanMappingType(columnName, mirror, tt);
    }

    protected Expression<?> getBeanMappingType(String columnName, Class<?> type, TemporalType temporalType) {
        Class<?> mirror = type;
        // String and char
        if (mirror == String.class) {
            return createString(columnName);
        }
        // Boolean
        if (mirror == Boolean.class || mirror == Boolean.TYPE) {
            return createBoolean(columnName);
        }
        // Byte
        if (mirror == Byte.class || mirror == Byte.TYPE) {
            return createNumber(columnName, Byte.class);
        }
        // Short
        if (mirror == Short.class || mirror == Short.TYPE) {
            return createNumber(columnName, Short.class);
        }
        // Int
        if (mirror == Integer.class || mirror == Integer.TYPE) {
            return createNumber(columnName, Integer.class);
        }
        // Float
        if (mirror == Float.class || mirror == Float.TYPE) {
            return createNumber(columnName, Float.class);
        }
        // Double
        if (mirror == Double.class || mirror == Double.TYPE) {
            return createNumber(columnName, Double.class);
        }
        // Long
        if (mirror == Long.class || mirror == Long.TYPE) {
            return createNumber(columnName, Long.class);
        }
        // BigDecimal
        if (mirror == BigDecimal.class) {
            return createNumber(columnName, BigDecimal.class);
        }
        // BigInteger
        if (mirror == BigInteger.class) {
            return createNumber(columnName, BigInteger.class);
        }

        // Enum
        if (mirror.isEnum()) {
            return createEnum(columnName, (Class) mirror);
        }
        // Char
        if (mirror == CharSequence.class || mirror == Character.TYPE) {
            return createSimple(columnName, Character.class);
        }

        // jdk8日期
        if (mirror == LocalDate.class) {
            return createDate(columnName, LocalDate.class);
        }
        if (mirror == LocalDateTime.class) {
            return createDateTime(columnName, LocalDateTime.class);
        }
        if (mirror == LocalTime.class) {
            return createTime(columnName, LocalTime.class);
        }
        // 日期处理
        if (Date.class.isAssignableFrom(mirror)) {
            if (temporalType != null) {
                switch (temporalType) {
                    case DATE:
                        return createDate(columnName, (Class<? super Comparable>) mirror);
                    case TIME:
                        return createTime(columnName, (Class<? super Comparable>) mirror);
                    case TIMESTAMP:
                        return createDateTime(columnName, (Class<? super Comparable>) mirror);
                    default:
                        break;
                }
            }
            return createDateTime(columnName, (Class<? super Comparable>) mirror);
        }

        // byte[]
        if (mirror.isArray()) {
            return createArray(columnName, (Class<? super Object>) mirror);
        }
        // 默认情况
        return createSimple(columnName, (Class<? super Object>) mirror);
    }

    public Class<T> getClz() {
        return clz;
    }

    protected void setClz(Class<T> clz) {
        this.clz = clz;
    }

    /**
     * 克隆一个新的原始对象.
     * @param <T>
     * @return
     */
    public <T extends DBObject> SQLRelationalPath<T> cloneNew() {
        SQLRelationalPath<T> relationalPath = new SQLRelationalPath(clz);
        return relationalPath;
    }

    public <T extends DBObject> SQLRelationalPath<T> cloneNew(String variable) {
        SQLRelationalPath<T> relationalPath = new SQLRelationalPath(clz, variable);
        return relationalPath;
    }
}
