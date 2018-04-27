package sf.database.meta.dynamic;


import sf.database.jdbc.type.TypeHandler;

import java.io.Serializable;
import java.util.Objects;

/**
 * ColumnMetadata
 * Provides metadata like the column name, JDBC type and constraints
 */
public final class DynamicColumnMapping implements Serializable {

    private static final long serialVersionUID = -5678865742525938470L;

    /**
     * Creates default column meta data with the given column name, but without
     * any type or constraint information. Use the fluent builder methods to
     * further configure it.
     * @throws NullPointerException if the name is null
     */
    public static DynamicColumnMapping named(String name) {
        return new DynamicColumnMapping().setName(name);
    }

    private static final int UNDEFINED = -1;

    /**
     * 名称
     */
    private String name;

    private Integer jdbcType;

    /**
     * 对应java类型
     */
    private Class<?> clz;

    /**
     * 对应java字段名称
     */
    private String field;

    private boolean nullable;

    private int size = UNDEFINED;

    private int decimalDigits = UNDEFINED;

    private String comment;

    /**
     * 是否唯一
     */
    private boolean unique;
    private boolean pk;// 是否为主键

    private Object defaultValue;

    /**
     * 转换类
     */
    private TypeHandler<Object> handler;

    public String getName() {
        return name;
    }

    public int getJdbcType() {
        return jdbcType;
    }

    public boolean hasJdbcType() {
        return jdbcType != null;
    }


    public boolean isNullable() {
        return nullable;
    }


    /**
     * For char or date types this is the maximum number of characters, for numeric or decimal types this is precision.
     * @return size
     */
    public int getSize() {
        return size;
    }

    public boolean hasSize() {
        return size != UNDEFINED;
    }


    /**
     * the number of fractional digits
     * @return digits
     */
    public int getDigits() {
        return decimalDigits;
    }

    public boolean hasDigits() {
        return decimalDigits != UNDEFINED;
    }

    public DynamicColumnMapping setName(String name) {
        this.name = name;
        return this;
    }

    public DynamicColumnMapping setJdbcType(Integer jdbcType) {
        this.jdbcType = jdbcType;
        return this;
    }

    public DynamicColumnMapping setNullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    public DynamicColumnMapping setSize(int size) {
        this.size = size;
        return this;
    }

    public int getDecimalDigits() {
        return decimalDigits;
    }

    public DynamicColumnMapping setDecimalDigits(int decimalDigits) {
        this.decimalDigits = decimalDigits;
        return this;
    }

    public boolean isUnique() {
        return unique;
    }

    public DynamicColumnMapping setUnique(boolean unique) {
        this.unique = unique;
        return this;
    }

    public Class<?> getClz() {
        return clz;
    }

    public DynamicColumnMapping setClz(Class<?> clz) {
        this.clz = clz;
        return this;
    }

    public String getField() {
        return field;
    }

    public DynamicColumnMapping setField(String field) {
        this.field = field;
        return this;
    }

    public boolean isPk() {
        return pk;
    }

    public DynamicColumnMapping setPk(boolean pk) {
        this.pk = pk;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public DynamicColumnMapping setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public TypeHandler<Object> getHandler() {
        return handler;
    }

    public void setHandler(TypeHandler<Object> handler) {
        this.handler = handler;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof DynamicColumnMapping) {
            DynamicColumnMapping md = (DynamicColumnMapping) o;
            return name.equals(md.name)
                    && Objects.equals(jdbcType, md.jdbcType)
                    && nullable == md.nullable
                    && size == md.size
                    && decimalDigits == md.decimalDigits;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }


}
