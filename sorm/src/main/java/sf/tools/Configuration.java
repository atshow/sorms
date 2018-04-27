package sf.tools;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Configuration {
    private static String fileName = "orm.properties";
    private static Map<String, String> cache = new HashMap<String, String>();
    private static File file;
    static org.slf4j.Logger log = LoggerFactory.getLogger(Configuration.class);


    public enum Item {
        // ////////////数据库基本操作设置/////////////////
        /**
         * SQL调试开关，默认false，开启后输出各种日志
         */
        DB_DEBUG,

        // /////////////////其他HttpClient选项//////////////////
        /**
         * 启用HTTP客户端调试
         */
        HTTP_DEBUG,
        /**
         * 全局禁用代理服务器
         */
        HTTP_DISABLE_PROXY,
        /**
         * HTTP选项，默认下载路径
         */
        HTTP_DOWNLOAD_PATH,
        /**
         * HTTP选项，全局超时
         */
        HTTP_TIMEOUT,
        /**
         * HTTP选项 超时、重试次数
         */
        HTTP_RETRY,

        // ///////////////其他选项//////////////////
        /**
         * 使用标准日志输出，默认直接print到控制台。设置为true时，日志写入到slf4j，false时则直接输出到标准控制台
         */
        COMMON_DEBUG_ADAPTER,
        /**
         * 当启用了COMMON_DEBUG_ADAPTER后，再开启本选项，可以将System.out和System.err流也重定向
         */
        SYSOUT_REDIRECT, //
        // ////////////////其他不常用属性///////////////////
        /**
         * 当使用ServletExchange返回json时，带上json头
         */
        HTTP_SEND_JSON_HEADER,
        /**
         * 在控制台输出列的时候显示列的数值类型
         */
        CONSOLE_SHOW_COLUMN_TYPE, //
        /**
         * SQLplus显示选项
         */
        CONSOLE_SHOW_RESULT_LIMIT, //
        /**
         * 默认日志路径
         * @deprecated 旧功能，不建议使用
         */
        LOG_PATH
    }
}
