package sf.tools;

/**
 * 主要提供jdk环境参数读取相关.
 */
public class JDKUtils {
    /**
     * jdk 版本获取
     */
    public static int majorJavaVersion = 5;

    static {
        String javaVersion = System.getProperty("java.version");
        // version String should look like "1.4.2_10"
        if (javaVersion.contains("1.10.")) {
            majorJavaVersion = 10;
        } else if (javaVersion.contains("1.9.")) {
            majorJavaVersion = 9;
        } else if (javaVersion.contains("1.8.")) {
            majorJavaVersion = 8;
        } else if (javaVersion.contains("1.7.")) {
            majorJavaVersion = 7;
        } else if (javaVersion.contains("1.6.")) {
            majorJavaVersion = 6;
        } else {
            // else leave 1.5 as default (it's either 1.5 or unknown)
            majorJavaVersion = 5;
        }
    }

    public enum JavaVersion {
        Java5, Java6, Java7, Java8, Java9, Java10, Java11;
    }
}
