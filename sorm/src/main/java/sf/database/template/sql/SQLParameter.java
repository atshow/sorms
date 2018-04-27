package sf.database.template.sql;

import sf.database.jdbc.type.TypeHandler;

/**
 * sql 参数描述，包含值，对应的名称，如
 * <pre>
 * where id=${p(id)}
 * </pre>
 * 值是id对应的某个java对象，名字就是”id“
 */
public class SQLParameter {
    /**
     * 值
     */
    private Object value;
    /**
     * 表达式(可以代表参数名称,在命名查询中有用)
     */
    private String expression;
    public NameType type = NameType.NAME_GENEARL;
    private TypeHandler handler;

    /**
     * 命名类型
     */
    public enum NameType {
        /**
         * 普通
         */
        NAME_GENEARL,
        /**
         * 表达式
         */
        NAME_EXPRESSION,
        /**
         * 未知
         */
        NAME_UNKONW
    }

    //默认为0，不做处理，否则，会将目标对象转成期望的方式插入到数据库，比如long转short ？？
    public int jdbcType = 0;

    public SQLParameter() {

    }

    public SQLParameter(String expression, Object value) {
        this.expression = expression;
        this.value = value;
        this.type = NameType.NAME_GENEARL;
    }

    public SQLParameter(Object value) {
        this.value = value;
        this.type = NameType.NAME_UNKONW;
    }

    public SQLParameter(String expression, Object value, NameType type) {
        this(expression, value);
        this.type = type;
    }

    public String toString() {
        if (value != null) {
            return value.toString();
        } else {
            return "";
        }
    }

    public int getJdbcType() {
        return jdbcType;
    }

    public SQLParameter setJdbcType(int jdbcType) {
        this.jdbcType = jdbcType;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public SQLParameter setValue(Object value) {
        this.value = value;
        return this;
    }

    public String getExpression() {
        return expression;
    }

    public SQLParameter setExpression(String expression) {
        this.expression = expression;
        return this;
    }

    public TypeHandler getHandler() {
        return handler;
    }

    public SQLParameter setHandler(TypeHandler handler) {
        this.handler = handler;
        return this;
    }
}
