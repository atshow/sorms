package sf.database.jdbc.type;

import sf.database.dialect.DBDialect;
import sf.database.meta.ColumnMapping;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface TypeHandler<T> {
    /**
     * 从结果集里获取一个字段的值
     * @param rs          结果集
     * @param columnLabel 列名
     * @return 字段值
     * @throws SQLException
     */
    T get(ResultSet rs, String columnLabel) throws SQLException;

    /**
     * 从结果集里获取一个字段的值
     * @param rs          结果集
     * @param columnIndex 索引
     * @return 字段值
     * @throws SQLException
     */
    T get(ResultSet rs, int columnIndex) throws SQLException;

    T get(CallableStatement cs, int parameterIndex) throws SQLException;

    T get(CallableStatement cs, String parameterName) throws SQLException;

    /**
     * 为缓冲语句设置值
     * <p>
     * 一个值可以被设置到多个占位符中
     * @param ps    缓冲语句
     * @param value 值
     * @param index 占位符位置，从 1 开始
     * @return 实际被设置到Statement中的值。<br>
     * 许多数据类型在设置到JDBC
     * Statement中时需要转换类型，例如java.util.Date需要转换为java.sql.Date。
     * @throws SQLException
     */
    Object set(PreparedStatement ps, Object value, int index) throws SQLException;

    /**
     * 为了支持悲观锁中的数据类型转换
     * @param rs
     * @param columnLabel
     * @param value
     * @throws SQLException
     */
    void update(ResultSet rs, String columnLabel, Object value) throws SQLException;

    void update(ResultSet rs, int columnIndex, Object value) throws SQLException;

    /**
     * 类型
     * @return
     */
    Class<T> getDefaultJavaType();

    /**
     * 设置返回的class,大部分的情况下不需要实现,只对枚举或需要自定义返回类型的情况使用
     * 和getDefaultJavaType() 方法成对出现
     * @param clz 设置java的数据类型
     */
    default void setDefaultJavaType(Class<T> clz) {
        throw new UnsupportedOperationException(" not support!");
    }

    /**
     * 设置返回的class,大部分的情况下不需要实现,只对范型返回类型的情况使用
     * @param columnMapping
     */
    default void setColumnMapping(ColumnMapping columnMapping) {

    }

    /**
     * java.sql.Types的值
     * @return
     */
    int getSqlType();

    /**
     * 获取sql表达式
     * @param value
     * @param profile
     * @return
     */
    default String getSqlExpression(T value, DBDialect profile) {
        return null;
    }
}
