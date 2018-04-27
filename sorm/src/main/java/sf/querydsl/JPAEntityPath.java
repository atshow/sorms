package sf.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.*;
import sf.database.DBField;
import sf.database.DBObject;
import sf.database.jdbc.extension.ObjectJsonMapping;
import sf.database.meta.ColumnMapping;
import sf.database.meta.MetaHolder;
import sf.database.meta.TableMapping;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * JPQL和HQL使用.
 */
public class JPAEntityPath<T extends DBObject> extends EntityPathBase<T> {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final Map<DBField, Expression<?>> map = new HashMap<>();

    public Map<DBField, Expression<?>> getMap() {
        return map;
    }

    public JPAEntityPath(Class<? extends DBObject> type) {
        super((Class<? extends T>) type, PathMetadataFactory.forVariable(MetaHolder.getMeta(type).getTableName()));
        TableMapping tm = MetaHolder.getMeta(type);
        for (Map.Entry<DBField, ColumnMapping> entry : tm.getSchemaMap().entrySet()) {
            Expression<?> path = getBeanMappingType(entry.getValue());
            map.put(entry.getKey(), path);
        }
    }

    public JPAEntityPath(Class<? extends T> type, String variable) {
        super(type, variable);
    }

    public JPAEntityPath(Class<? extends T> type, PathMetadata metadata) {
        super(type, metadata);
    }

    public JPAEntityPath(Class<? extends T> type, PathMetadata metadata, @Nullable PathInits inits) {
        super(type, metadata, inits);
    }

    /**
     * 可以转换为NumberPath,BooleanPath...
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


    public <E, Q extends SimpleExpression<? super E>> CollectionPath<E, Q> collection(DBField field) {
        return column(field, CollectionPath.class);
    }


    public <E, Q extends SimpleExpression<? super E>> SetPath<E, Q> set(DBField field) {
        return column(field, SetPath.class);
    }


    public <E, Q extends SimpleExpression<? super E>> ListPath<E, Q> list(DBField field) {
        return column(field, ListPath.class);
    }

    public <K, V, E extends SimpleExpression<? super V>> MapPath<K, V, E> map(DBField field) {
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
        String columnName = cm.getFieldName();
        // String and char
        if (mirror == String.class)
            return createString(columnName);

        // Boolean
        if (mirror == Boolean.class || mirror == Boolean.TYPE)
            return createBoolean(columnName);
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

        // jdk8日期
        if (mirror == LocalDate.class)
            return createDate(columnName, LocalDate.class);
        if (mirror == LocalDateTime.class)
            return createDateTime(columnName, LocalDateTime.class);
        if (mirror == LocalTime.class)
            return createTime(columnName, LocalTime.class);

        //日期处理
        if (Date.class.isAssignableFrom(mirror)) {
            if (cm.getTemporal() != null) {
                switch (cm.getTemporal().value()) {
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

        if (cm.getType() != null && cm.getType().value() == ObjectJsonMapping.class) {
            Class<?>[] rawTypes = cm.getFieldAccessor().getRawTypes();
            if (Set.class.isAssignableFrom(cm.getClz())) {
                Class raw = Object.class;
                if (rawTypes != null && rawTypes.length > 0) {
                    raw = rawTypes[0];
                }
                return createSet(columnName, raw, (Class) StringPath.class, PathInits.DIRECT2);

            } else if (Map.class.isAssignableFrom(cm.getClz())) {
                Class raw1 = Object.class;
                Class raw2 = Object.class;
                if (rawTypes != null && rawTypes.length > 0) {
                    raw1 = rawTypes[0];
                    if (rawTypes.length >= 2) {
                        raw2 = rawTypes[1];
                    }
                }
                //支持map转string
                return this.createMap(columnName, raw1, raw2, (Class) StringPath.class);
            } else if (List.class.isAssignableFrom(cm.getClz())) {
                Class raw = Object.class;
                if (rawTypes != null && rawTypes.length > 0) {
                    raw = rawTypes[0];
                }
                return createList(columnName, raw, (Class) StringPath.class, PathInits.DIRECT2);
            }
        }
        // 默认情况
        return createSimple(columnName, (Class<? super Object>) mirror);
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

}
