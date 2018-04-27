package sf.database.dialect;

import jetbrick.util.builder.EqualsBuilder;
import sf.tools.StringUtils;

/**
 * 代表一个数据库字段类型
 */
public final class SqlType {
    private final String name;
    private final Integer length;
    private final Integer scale;

    /**
     * @param defination examples: text, char(20), decimal(10,2)
     * @return
     */
    public static SqlType create(String defination) {
        String name = StringUtils.substringBefore(defination, "(");
        Integer length = null;
        Integer scale = null;
        if (defination.indexOf(")") > 0) {
            if (defination.indexOf(",") > 0) {
                length = Integer.valueOf(StringUtils.substringBetween(defination, "(", ",").trim());
                scale = Integer.valueOf(StringUtils.substringBetween(defination, ",", ")").trim());
            } else {
                length = Integer.valueOf(StringUtils.substringBetween(defination, "(", ")").trim());
            }
        }
        return new SqlType(name, length, scale);
    }

    public SqlType(String name, Integer length, Integer scale) {
        this.name = name;
        this.length = length;
        this.scale = scale;
    }

    public String getName() {
        return name;
    }

    public Integer getLength() {
        return length;
    }

    public Integer getScale() {
        return scale;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;

        SqlType rhs = (SqlType) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(name, rhs.name);
        builder.append(length, rhs.length);
        builder.append(scale, rhs.scale);
        return builder.isEquals();
    }

    @Override
    public String toString() {
        if (length != null) {
            if (scale == null) {
                return name + "(" + length.toString() + ")";
            } else {
                return name + "(" + length.toString() + "," + scale.toString() + ")";
            }
        } else {
            return name;
        }
    }
}
