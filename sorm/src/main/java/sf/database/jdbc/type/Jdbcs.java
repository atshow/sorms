package sf.database.jdbc.type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sf.database.annotations.Type;
import sf.database.meta.ColumnMapping;

import javax.persistence.EnumType;
import javax.persistence.TemporalType;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 提供一些与 JDBC 有关的帮助函数
 * @author
 */
public abstract class Jdbcs {
    private static final Logger log = LoggerFactory.getLogger(Jdbcs.class);
    public static Map<String, TypeHandler> customJdbcValueMap = new ConcurrentHashMap<String, TypeHandler>();

    /**
     * 注册一个自定义JdbcValue,若adaptor为null,则取消注册
     * @param className 类名
     * @param adaptor   值适配器实例,若为null,则取消注册
     * @return 原有的值适配器
     */
    public static TypeHandler register(String className, TypeHandler adaptor) {
        if (adaptor == null)
            return customJdbcValueMap.remove(className);
        return customJdbcValueMap.put(className, adaptor);
    }

    public static TypeHandler<?> getDB2BeanMappingType(Class<?> mirror) {
        return getDB2BeanMappingType(mirror, 0);
    }

    public static TypeHandler<?> getDB2BeanMappingType(Class<?> mirror, int sqlType) {
        TypeHandler<?> custom = customJdbcValueMap.get(mirror.getName());
        if (custom != null)
            return custom;
        // String and char
        if (mirror == String.class) {
            if (sqlType == Types.ROWID) {
                return Jdbcs.Adaptor.asRowId;
            }
            return Jdbcs.Adaptor.asString;
        }
        // Int
        if (mirror == Integer.class || mirror == Integer.TYPE)
            return Jdbcs.Adaptor.asInteger;
        // Boolean
        if (mirror == Boolean.class || mirror == Boolean.TYPE)
            return Jdbcs.Adaptor.asBoolean;
        // Long
        if (mirror == Long.class || mirror == Long.TYPE)
            return Jdbcs.Adaptor.asLong;
        // Enum
        if (mirror.isEnum()) {
            if (isNumber(sqlType)) {
                return new EnumOrdinalTypeHandler(mirror);
            }
            return new EnumTypeHandler(mirror);
        }
        // Char
        if (mirror == CharSequence.class || mirror == Character.TYPE)
            return Jdbcs.Adaptor.asChar;
        // Timestamp
        if (mirror == Timestamp.class)
            return Jdbcs.Adaptor.asTimestamp;
        // Byte
        if (mirror == Byte.class || mirror == Byte.TYPE)
            return Jdbcs.Adaptor.asByte;
        // Short
        if (mirror == Short.class || mirror == Short.TYPE)
            return Jdbcs.Adaptor.asShort;
        // Float
        if (mirror == Float.class || mirror == Float.TYPE)
            return Jdbcs.Adaptor.asFloat;
        // Double
        if (mirror == Double.class || mirror == Double.TYPE)
            return Jdbcs.Adaptor.asDouble;
        // BigDecimal
        if (mirror == BigDecimal.class)
            return Jdbcs.Adaptor.asBigDecimal;
        // java.sql.Date
        if (mirror == java.sql.Date.class)
            return Jdbcs.Adaptor.asSqlDate;
        // java.sql.Time
        if (mirror == java.sql.Time.class)
            return Jdbcs.Adaptor.asSqlTime;
        // Calendar
        if (mirror == LocalDate.class)
            return Adaptor.asLocalDate;
        // java.util.Date
        if (mirror == java.util.Date.class)
            return Jdbcs.Adaptor.asDate;
        // Blob
        if (mirror == Blob.class)
            return new BlobTypeHandler();
        // Clob
        if (mirror == Clob.class)
            return new StringClobTypeHandler();
        // byte[]
        if (mirror.isArray() && mirror.getComponentType() == byte.class) {
            return Jdbcs.Adaptor.asBytes;
        }
        // inputstream
        if (mirror == InputStream.class)
            return Jdbcs.Adaptor.asBinaryStream;
        if (mirror == Reader.class)
            return Jdbcs.Adaptor.asReader;

        // 默认情况
        return Jdbcs.Adaptor.asString;
    }

    @SuppressWarnings("unchecked")
    public static <T> TypeHandler<T> getBean2DBMappingType(ColumnMapping column) {
        Class<?> mirror = column.getClz();
        TypeHandler<?> custom = customJdbcValueMap.get(mirror.getName());
        if (custom != null) {
            return (TypeHandler<T>) custom;
        }
        Type type = column.getType();
        if (type != null) {
            //自定义转换类型必须提供空参构造.
            try {
                TypeHandler<T> typeHandler = (TypeHandler<T>) type.value().newInstance();
                Class<? extends TypeHandler> typeClass = type.value();
                for (Method m : typeClass.getDeclaredMethods()) {
                    //需要设置自定义返回类型
                    if (m.getName().contains("setDefaultJavaType")) {
                        typeHandler.setDefaultJavaType((Class<T>) mirror);
                        break;
                    }
                    typeHandler.setColumnMapping(column);
                }
                return typeHandler;
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        if (mirror == String.class) {
            // String char
            if (column.isLob()) {
                return (TypeHandler<T>) Jdbcs.Adaptor.asClob;
            }
            return (TypeHandler<T>) Jdbcs.Adaptor.asString;
        }
        if (mirror == Integer.class || mirror == Integer.TYPE) {// int
            return (TypeHandler<T>) Jdbcs.Adaptor.asInteger;
        }
        // Boolean
        if (mirror == Boolean.class || mirror == Boolean.TYPE) {
            return (TypeHandler<T>) Jdbcs.Adaptor.asBoolean;
        }
        // Long
        if (mirror == Long.class || mirror == Long.TYPE) {
            return (TypeHandler<T>) Jdbcs.Adaptor.asLong;
        }
        // Enum
        if (mirror.isEnum()) {
            if (column.getEnumerated() != null && column.getEnumerated().value() == EnumType.ORDINAL) {
                return new EnumOrdinalTypeHandler(mirror);
            }
            return new EnumTypeHandler(mirror);
        }
        // Char
        if (mirror == CharSequence.class || mirror == Character.TYPE) {
            return (TypeHandler<T>) Jdbcs.Adaptor.asChar;
        }
        // Byte
        if (mirror == Byte.class || mirror == Byte.TYPE) {
            return (TypeHandler<T>) Jdbcs.Adaptor.asByte;
        }
        // Short
        if (mirror == Short.class || mirror == Short.TYPE) {
            return (TypeHandler<T>) Jdbcs.Adaptor.asShort;
        }
        // Float
        if (mirror == Float.class || mirror == Float.TYPE) {
            return (TypeHandler<T>) Jdbcs.Adaptor.asFloat;
        }
        // Double
        if (mirror == Double.class || mirror == Double.TYPE) {
            return (TypeHandler<T>) Jdbcs.Adaptor.asDouble;
        }
        // BigDecimal
        if (mirror == BigDecimal.class) {
            return (TypeHandler<T>) Jdbcs.Adaptor.asBigDecimal;
        }
        //BigInteger
        if (mirror == BigInteger.class) {
            return (TypeHandler<T>) Adaptor.asBigInteger;
        }

        if (mirror.isAssignableFrom(Date.class)) {
            if (column.getTemporal() != null) {
                if (column.getTemporal().value() == TemporalType.TIME) {
                    return (TypeHandler<T>) Jdbcs.Adaptor.asSqlTime;
                } else if (column.getTemporal().value() == TemporalType.TIMESTAMP) {
                    return (TypeHandler<T>) Jdbcs.Adaptor.asTimestamp;
                }
            }
            return (TypeHandler<T>) Jdbcs.Adaptor.asSqlDate;
        }
        // Calendar
        if (mirror == LocalDate.class) {
            return (TypeHandler<T>) Jdbcs.Adaptor.asLocalDate;
        }
        if (mirror == LocalTime.class) {
            return (TypeHandler<T>) Adaptor.asLocalTime;
        }
        if (mirror == LocalDateTime.class) {
            return (TypeHandler<T>) Adaptor.asLocalDateTime;
        }
        if (mirror == Instant.class) {
            return (TypeHandler<T>) Jdbcs.Adaptor.asInstant;
        }

        // byte[]
        if (mirror.isArray() && mirror.getComponentType() == byte.class) {
            if (column.isLob()) {
                return (TypeHandler<T>) Jdbcs.Adaptor.asBlob;
            }
            return (TypeHandler<T>) Jdbcs.Adaptor.asBytes;
        }
        // inputstream
        if (mirror == InputStream.class) {
            return (TypeHandler<T>) Jdbcs.Adaptor.asBinaryStream;
        }
        if (mirror == Reader.class) {
            return (TypeHandler<T>) Jdbcs.Adaptor.asReader;
        }

        // 默认情况
        return (TypeHandler<T>) Jdbcs.Adaptor.asString;
    }

    /**
     * 判断是否是数字类型
     * @param sqlType
     * @return
     */
    public static boolean isNumber(int sqlType) {
        boolean isNumber = false;
        switch (sqlType) {
            case Types.BIT:
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
            case Types.DECIMAL:
            case Types.NUMERIC:
                isNumber = true;
                break;
            default:
                break;
        }
        return isNumber;
    }

    /**
     * 判断是否是字符串类型
     * @param sqlType
     * @return
     */
    public static boolean isString(int sqlType) {
        boolean isString = false;
        switch (sqlType) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.ROWID:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.NCLOB:
            case Types.CLOB:
                isString = true;
                break;
            default:
                break;
        }
        return isString;
    }

    /**
     * 判断是否是字节数组类型
     * @param sqlType
     * @return
     */
    public static boolean isByteArray(int sqlType) {
        boolean isByteArray = false;
        switch (sqlType) {
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                isByteArray = true;
                break;
            default:
                break;
        }
        return isByteArray;
    }

    public static class Adaptor {

        public static final TypeHandler<byte[]> asBlob = new BlobTypeHandler();
        public static final TypeHandler<String> asClob = new StringClobTypeHandler();

        /**
         * 空值适配器
         */
        @SuppressWarnings("rawtypes")
        public static final TypeHandler asNull = new NullType();

        /**
         * 字符串适配器
         */
        public static final TypeHandler<String> asString = new StringVARCHARType();

        /**
         * 字符适配器
         */
        public static final TypeHandler<Character> asChar = new CharacterVARCHARType();

        /**
         * 整型适配器
         */
        public static final TypeHandler<Integer> asInteger = new IntegerType();

        /**
         * 大数适配器
         */
        public static final TypeHandler<BigDecimal> asBigDecimal = new BigDecimalNUMERICType();
        public static final TypeHandler<BigInteger> asBigInteger = new BigIntegerNUMERICType();

        /**
         * 布尔适配器
         * <p>
         * 对 Oracle，Types.BOOLEAN 对于 setNull 是不工作的 因此 OracleExpert 会用一个新的
         * Adaptor 处理自己这种特殊情况
         */
        public static final TypeHandler<Boolean> asBoolean = new BooleanBOOLEANType();

        /**
         * 长整适配器
         */
        public static final TypeHandler<Long> asLong = new LongBIGINTType();

        /**
         * 字节适配器
         */
        public static final TypeHandler<Byte> asByte = new ByteTINYINTType();

        /**
         * 短整型适配器
         */
        public static final TypeHandler<Short> asShort = new ShortSMALLINTType();

        /**
         * 浮点适配器
         */
        public static final TypeHandler<Float> asFloat = new FloatREALType();

        /**
         * 双精度浮点适配器
         */
        public static final TypeHandler<Double> asDouble = new DoubleDOUBLEType();

        /**
         * 日历适配器
         */
        public static final TypeHandler<LocalDate> asLocalDate = new LocalDateDATEType();
        public static final TypeHandler<LocalTime> asLocalTime = new LocalTimeTIMEType();
        public static final TypeHandler<LocalDateTime> asLocalDateTime = new LocalDateTimeTIMESTAMPType();
        public static final TypeHandler<Instant> asInstant = new InstantTIMESTAMPType();

        /**
         * 时间戳适配器
         */
        public static final TypeHandler<Date> asTimestamp = new DateTIMESTAMPType();

        /**
         * 日期适配器
         */
        public static final TypeHandler<Date> asDate = asTimestamp;

        /**
         * Sql 日期适配器
         */
        public static final TypeHandler<Date> asSqlDate = new DateDATEType();
        /**
         * Sql 时间适配器
         */
        public static final TypeHandler<Date> asSqlTime = new DateTIMEType();

        /**
         * 默认对象适配器
         */
        public static final TypeHandler<Object> asObject = new ObjectJAVAOBJECTType();

        /**
         * 字节数组适配器
         */
        public static final TypeHandler<byte[]> asBytes = new ByteArrayBINARYType();

        public static final TypeHandler<InputStream> asBinaryStream = new InputStreamBINARYType();

        public static final TypeHandler<Reader> asReader = new ReaderBINARYType();

        public static final TypeHandler<Object> asArray = new ObjectARRAYType();

        public static final TypeHandler<String> asRowId = new StringROWIDType();

    }

    public static void setCharacterStream(int index, Reader obj, PreparedStatement stat) throws SQLException {
        stat.setCharacterStream(index, obj);
    }

}
