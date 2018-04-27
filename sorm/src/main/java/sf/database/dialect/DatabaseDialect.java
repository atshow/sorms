package sf.database.dialect;

import sf.database.support.RDBMS;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public interface DatabaseDialect {
    /**
     * 得到RDBMS的名称
     * @return
     */
    RDBMS getName();

    /**
     * 得到该数据库上该种数据类型的真实实现类型。
     * 比如，在不支持boolean类型的数据库上，会以char类型代替boolean；在不支持blob的数据库上，会以varbinary类型代替blob
     * @param vType
     * @return
     */
    int getImplementationSqlType(int sqlType);

    /**
     * 将表达式或值转换为文本形式的缺省值描述
     * @param defaultValue
     * @param sqlType
     * @return
     */
    String toDefaultString(Object defaultValue, int sqlType);

    /**
     * 像Oracle，其Catlog是不用的，那么返回null mySQL没有Schema，每个database是一个catlog，那么返回值
     * 同时修正返回的大小写
     * @param schema
     * @return
     */
    String getCatlog(String schema);

    /**
     * 对于表名前缀的XX. MYSQL是作为catlog的，不是作为schema的 同时修正返回的大小写
     * @param schema
     * @return
     */
    String getSchema(String schema);

    /**
     * 获取数据库的默认驱动类
     * @param url Derby根据连接方式的不同，会有两种不同的DriverClass，因此需要传入url
     * @return 驱动类
     */
    String getDriverClass(String url);

    /**
     * 生成数据库连接字串
     * @param host     <=0则会使用默认端口
     * @param port
     * @param filepath
     * @param dbname
     * @return
     */
    String generateUrl(String host, int port, String pathOrName);

    /**
     * Oracle会将所有未加引号的数据库对象名称都按照大写对象名来处理，MySQL则对表名一律转小写，列名则保留原来的大小写。
     * 为了体现这一数据库策略的不同，这里处理大小写的问题。
     * <p>
     * 目前的原则是：凡是涉及
     * schema/table/view/sequence/dbname等转换的，都是用此方法，凡是设计列名转换，列别名定义的都用
     * {@link #getColumnNameIncase}方法
     * @param name
     * @return
     */
    String getObjectNameToUse(String name);

    /**
     * @param name
     * @return
     * @since 3.0 2013-7新增，数据库对于表明和列名的大小写策略并不总是一致。因此今后要将列名处理的场合逐渐由原来的函数移到这里来实现
     * <p>
     * 季怡2013-7新增了{@link #getColumnNameIncase}
     * 目的是将列名的大小写策略和表/视图/schema名等策略区分开来。因为mysql似乎两者表现并不一致。
     */
    String getColumnNameToUse(String name);

    /**
     * 检查数据库是否包含指定的关键字，用来进行检查的对象名称都是按照getColumnNameIncase转换后的，因此对于大小写统一的数据库，
     * 这里无需考虑传入的大小写问题。
     * @param name
     * @return
     */
    boolean containKeyword(String name);

    /**
     * 针对非绑定变量SQL，生成SQL语句所用的文本值。 Java -> SQL String
     */
    String getSqlDateExpression(Date value);

    /**
     * 针对非绑定变量SQL，生成SQL语句所用的文本值。 Java -> SQL String
     */
    String getSqlTimeExpression(Date value);

    /**
     * 针对非绑定变量SQL，生成SQL语句所用的文本值。 Java -> SQL String
     */
    String getSqlTimestampExpression(Date value);

    /**
     * 允许数据库方言对Statement再进行一次包装
     * @param stmt
     * @param isInJpaTx
     * @return
     */
    Statement wrap(Statement stmt, boolean isInJpaTx) throws SQLException;

    /**
     * 允许数据库方言对PreparedStatement再进行一次包装
     * @param stmt
     * @param isInJpaTx
     * @return
     */
    PreparedStatement wrap(PreparedStatement stmt, boolean isInJpaTx) throws SQLException;
}
