package sf.common.log;

import java.util.Collection;
import java.util.List;

public class OrmLog {
    public static boolean open = true;
    public static String lineSeparator = System.getProperty("line.separator", "\n");

    public static void commonListLog(String sql, List<Object> paras) {
        if (open) {
            StringBuilder sb = new StringBuilder();

            sb.append(lineSeparator);
            sb.append("SQL: " + sql);
            if (paras != null) {
                for (Object p : paras) {
                    sb.append(lineSeparator + "Param ====>>> " + p.toString());
                }
            }
            println(sb.toString());
        }
    }

    public static void commonArrayLog(String sql, Object... paras) {
        if (open) {
            StringBuilder sb = new StringBuilder();
            sb.append(lineSeparator);
            sb.append("SQL: " + sql);
            if (paras != null) {
                for (Object p : paras) {
                    sb.append(lineSeparator + "Param ====>>> " + p.toString());
                }
            }
            println(sb.toString());
        }
    }

    public static void batchCommonLog(String sql, int all, int current, Object... paras) {
        if (open) {
            StringBuilder sb = new StringBuilder();
            sb.append("Batch:" + current + "/" + all + " " + sql);
            if (paras != null) {
                for (Object p : paras) {
                    sb.append(lineSeparator + "Param ====>>> " + p.toString());
                }
            }
            println(sb.toString());
        }
    }

    public static void resultLog(long start) {
        resultLog(start, null);
    }

    public static void resultLog(long start, Object result) {
        if (open) {
            StringBuilder sb = new StringBuilder();
            sb.append("Time cost: " + (System.currentTimeMillis() - start) + "ms");
            if (result != null) {
                if (result instanceof Collection) {
                    sb.append(lineSeparator + "Result:  [").append(((Collection<?>) result).size()).append("]").append(lineSeparator);
                } else {
                    sb.append(lineSeparator + "Result:  [").append(result).append("]").append(lineSeparator);
                }
            }
            println(sb.toString());
        }
    }

    protected static void println(String str) {
        System.out.println(str);
    }
}
