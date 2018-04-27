package sf.database.transaction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 默认的事物管理器，可以管理单个，多个数据源事物，但并不是分布式事物管理器
 * 不能保证事务统一，只能尽量。
 * @author xiandafu
 */
public class DefaultTransactionManager {
    static ThreadLocal<Boolean> inTrans = new ThreadLocal<Boolean>() {
        protected Boolean initialValue() {
            return false;
        }
    };

    static ThreadLocal<Map<DataSource, Connection>> conns = new ThreadLocal<Map<DataSource, Connection>>();


    public static void start() {
        inTrans.set(true);
    }

    public static void commit() throws SQLException {
        Map<DataSource, Connection> map = conns.get();
        try {
            if (map == null) return;
            SQLException e = null;
            for (Connection conn : map.values()) {
                try {
                    conn.commit();
                } catch (SQLException ex) {
                    e = ex;
                } finally {
                    try {
                        conn.close();
                    } catch (SQLException ex) {
                        System.err.println("commit error of connection " + conn + " " + ex.getMessage());
                    }
                }
            }
            if (e != null) throw e;
        } finally {
            clear();
        }
    }

    public static void rollback() throws SQLException {
        Map<DataSource, Connection> map = conns.get();
        SQLException e = null;
        if (map == null) return;
        try {
            for (Connection conn : map.values()) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    e = ex;
                } finally {
                    try {
                        conn.close();
                    } catch (SQLException ex) {
                        // TODO Auto-generated catch block
                        ex.printStackTrace();
                    }
                }
            }
            if (e != null) throw e;
        } finally {
            clear();
        }
    }

    public static void clear() {
        conns.remove();
        inTrans.remove();
    }

    public static Connection getCurrentThreadConnection(DataSource ds) throws SQLException {
        Map<DataSource, Connection> map = conns.get();
        Connection conn = null;
        if (map == null) {
            map = new HashMap<DataSource, Connection>();
            conn = ds.getConnection();
            //如果用户还有不需要事物，且每次都提交的操作，这个需求很怪，不管了
            conn.setAutoCommit(false);
            map.put(ds, conn);
            conns.set(map);
        } else {
            conn = map.get(ds);
            if (conn != null) return conn;
            conn = ds.getConnection();
            conn.setAutoCommit(false);
            map.put(ds, conn);
        }
        return conn;
    }

    public static boolean inTrans() {
        return inTrans.get();
    }
}
