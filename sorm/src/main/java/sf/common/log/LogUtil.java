package sf.common.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LogUtil {
    private static Logger log = LoggerFactory.getLogger("ORM LOG");

    public LogUtil() {

    }

    /**
     * 将异常信息输入日志
     * @param message
     * @param t
     */
    public static void exception(String message, Throwable t) {
        log.error(message, t);
    }

    public static void error(Object o) {
        if (log.isErrorEnabled()) {
            String msg = o.toString();
            log.error(msg);
        }
    }

    /**
     * 将异常堆栈打入日志 改起来影响比较大，所以就不改了。
     * @param t
     */
    public static void exception(Throwable t) {
        log.error("", t);
    }

    public static org.slf4j.Logger getLog() {
        return log;
    }

    public static void fatal(Object o) {
        String msg = o == null ? "" : o.toString();
        log.error(msg);
    }

    public static void error(String s, Object... o) {
        log.error(s, o);
    }

    /**
     * 以标准的slf4j的格式输出warn
     * @param s
     * @param o
     */
    public static void warn(String s, Object... o) {
        log.warn(s, o);
    }

    /**
     * 以标准的slf4j的格式输出info
     * @param s
     * @param o
     */
    public static void info(String s, Object... o) {
        log.info(s, o);
    }

    /**
     * 以标准的slf4j的格式输出debug
     */
    public static void debug(String s, Object... o) {
        log.debug(s, o);
    }

    public static void warn(Object o) {
        if (log.isWarnEnabled()) {
            String msg = o == null ? "" : o.toString();
            log.warn(msg);
        }
    }

    public static void info(Object o) {
        if (log.isInfoEnabled()) {
            String msg = o == null ? "" : o.toString();
            log.info(msg);
        }
    }

    /**
     * 将指定的对象显示输出
     * @param objs
     */
    public static void shows(Object... objs) {
        info(objs);
    }

    public static void show(ResultSet rs) {
        try {
            show((Object) rs);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
            }
        }
    }

    /**
     * 将指定的对象显示输出
     * @param o
     */
    public static void show(Object o) {
        info(o);
    }

    public static void debug(Object o) {
        if (log.isDebugEnabled()) {
            String msg = o == null ? "" : o.toString();
            log.error(msg);
        }
    }
}
