package sf.database.util;

import sf.common.CaseInsensitiveMap;
import sf.common.log.LogUtil;
import sf.database.connection.ConnectionHelp;
import sf.database.dbinfo.ConnectInfo;
import sf.database.dialect.DBDialect;
import sf.database.dialect.DBProperty;

import javax.sql.DataSource;
import java.sql.*;
import java.util.regex.Pattern;

public final class DBUtils {

    public static void closeQuietly(Connection conn) {
        try {
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
        }
    }

    public static void closeQuietly(Statement stmt) {
        try {
            if (stmt != null)
                stmt.close();
        } catch (SQLException e) {
        }
    }

    public static void closeQuietly(ResultSet rs) {
        try {
            if (rs != null)
                rs.close();
        } catch (SQLException e) {

        }
    }


    /**
     * 根据已有的连接解析连接信息
     * @param conn
     * @return
     * @throws SQLException
     */
    public static ConnectInfo tryAnalyzeInfo(Connection conn) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        ConnectInfo info = new ConnectInfo();
        info.setUser(meta.getUserName());
        info.setUrl(meta.getURL());
        info.setDatabaseProductName(meta.getDatabaseProductName());
        info.setDatabaseProductVersion(meta.getDatabaseProductVersion());
        info.setDatabaseMajorVersion(meta.getDatabaseMajorVersion());
        info.setDriverVersion(meta.getDriverVersion());
        info.setDriverMajorVersion(meta.getDriverMajorVersion());
        // 解析，获得profile, 解析出数据库名等信息
        return info;
    }

    public static boolean doGetTableExist(Connection conn, String tabelName) {
        ResultSet rs = null;
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            rs = metaData.getTables(null, null, tabelName.toUpperCase(), new String[]{"TABLE"});
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtils.closeQuietly(rs);
        }
    }

    public static DBDialect doGetDialect(DataSource dataSource) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            return DBUtils.doGetDialect(conn, true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtils.closeQuietly(conn);
        }
    }

    /**
     * @param conn
     * @param close 是否关闭
     * @return
     */
    public static DBDialect doGetDialect(Connection conn, boolean close) {
        try {
            String name = conn.getMetaData().getDatabaseProductName();
            return DBDialect.getDialect(name);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (close) {
                DBUtils.closeQuietly(conn);
            }
        }
    }

    /**
     * 获取schema 不报错
     * @param conn
     * @return
     */
    public static String getSchema(Connection conn) {
        try {
            return ConnectionHelp.getPhysicalConnection(conn).getSchema();
        } catch (SQLException e) {

        } finally {
        }
        return null;
    }

    /**
     * 获取catalog不报错
     * @param conn
     * @return
     */
    public static String getCatalog(Connection conn) {
        try {
            return conn.getCatalog();
        } catch (SQLException e) {

        } finally {
        }
        return null;
    }

    // 从一个标准的select语句中，生成一个 select count(*) 语句
    public static String getSqlSelectCount(String sql) {
        String countSql = sql.replaceAll("\\s+", " ");
        int pos = countSql.toLowerCase().indexOf(" from ");
        countSql = countSql.substring(pos);

        pos = countSql.toLowerCase().lastIndexOf(" order by ");
        int lastpos = countSql.toLowerCase().lastIndexOf(")");
        if (pos != -1 && pos > lastpos) {
            countSql = countSql.substring(0, pos);
        }

        String regex = "(left|right|inner) join (fetch )?\\w+(\\.\\w+)*";
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        countSql = p.matcher(countSql).replaceAll("");

        countSql = "select count(*) " + countSql;
        return countSql;
    }

    /**
     * 获取colName所在的行数
     * @param meta       从连接中取出的ResultSetMetaData
     * @param columnName 字段名
     * @return 所在的索引, 如果不存在就抛出异常
     * @throws SQLException 指定的colName找不到
     */
    public static int getColumnIndex(ResultSetMetaData meta, String columnName) throws SQLException {
        if (meta == null)
            return 0;
        int columnCount = meta.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (meta.getColumnName(i).equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        LogUtil.info(String.format("Can not find @Column(%s) in table/view (%s)", columnName, meta.getTableName(1)));
        throw new SQLException(String.format("Can not find @Column(%s)", columnName));
    }

    /**
     * 通过数据库列名获取指定map中的key值(数据库关键字的处理)
     * @param map
     * @param columnName 数据库的列名
     * @return
     */
    public static <T> T getColumnByDBName(CaseInsensitiveMap<T> map, String columnName) {
        T o = map.get(columnName);
        //以下为数据库关键字的处理
        if (o == null) {
            o = map.get("[" + columnName + "]");
        }
        if (o == null) {
            o = map.get("`" + columnName + "`");
        }
        if (o == null) {
            o = map.get("\"" + columnName + "\"");
        }
        return o;
    }

    /**
     * 如果列名或表名碰到了数据库的关键字，那么就要增加引号一类字符进行转义
     * @param profile
     * @param name
     * @return
     */
    public static final String escapeColumn(DBDialect profile, String name) {
        if (name == null)
            return name;
        String w = profile.getProperty(DBProperty.WRAP_FOR_KEYWORD);
        if (w != null && profile.containKeyword(name)) {
            StringBuilder sb = new StringBuilder(name.length() + 2);
            sb.append(w.charAt(0)).append(name).append(w.charAt(1));
            return sb.toString();
        }
        return name;
    }
}
