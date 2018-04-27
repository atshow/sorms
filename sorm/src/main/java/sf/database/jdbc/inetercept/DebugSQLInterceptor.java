package sf.database.jdbc.inetercept;

import sf.database.template.sql.SQLContext;
import sf.database.template.sql.SQLParameter;
import sf.tools.EnumUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Debug重新美化版本
 * @author darren xiandafu
 * @version 2016年8月25日
 */
public class DebugSQLInterceptor implements SQLInterceptor {

    List<String> includes = null;

    //debug 输入优先输出的类，而不是SQLManager或者是BaseMapper
    String preferredShowClass;

    public DebugSQLInterceptor() {
    }

    public DebugSQLInterceptor(List<String> includes) {
        this.includes = includes;
    }

    public DebugSQLInterceptor(String preferredShowClass) {
        this.preferredShowClass = preferredShowClass;
    }

    public DebugSQLInterceptor(List<String> includes, String preferredShowClass) {
        this.preferredShowClass = preferredShowClass;
        this.includes = includes;
    }

    @Override
    public void before(SQLContext ctx) {
        String sqlId = ctx.getSqlId();
        if (this.isDebugEanble(sqlId)) {
            ctx.putEnv("debug.time", System.currentTimeMillis());
        }
        if (this.isSimple(sqlId)) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        String lineSeparator = System.getProperty("line.separator", "\n");
        sb.append("┏━━━━━ Debug [").append(this.getSqlId(formatSql(sqlId))).append("] ━━━").append(lineSeparator)
                .append("┣ SQL：\t " + formatSql(ctx.getAfterSql()) + lineSeparator)
                .append("┣ 参数：\t " + formatParas(ctx.getParas())).append(lineSeparator);
        RuntimeException ex = new RuntimeException();
        StackTraceElement[] traces = ex.getStackTrace();
        int index = lookBusinessCodeInTrace(traces, ctx);
        StackTraceElement bussinessCode = traces[index];
        String className = bussinessCode.getClassName();
        String mehodName = bussinessCode.getMethodName();
        int line = bussinessCode.getLineNumber();
        sb.append("┣ 位置：\t " + className + "." + mehodName + "(" + bussinessCode.getFileName() + ":" + line + ")" + lineSeparator);

        ctx.putEnv("logs", sb);
    }


    protected int lookBusinessCodeInTrace(StackTraceElement[] traces, SQLContext ctx) {


        String className = getTraceClassName();
        for (int i = traces.length - 1; i >= 0; i--) {
            String name = traces[i].getClassName();
            if (className != null && className.equals(name)) {
                return i;

            } else if (name.equals(ctx.getExecClz().getName())) {
                return i;
            }
        }
        //不可能到这里
        throw new RuntimeException();

    }

    /**
     * 如果自己封装了beetlsql 有自己的util，并不想打印util类，而是业务类，可以在这里写util类
     * @return
     */
    protected String getTraceClassName() {
        return preferredShowClass;
    }

    @Override
    public void after(SQLContext ctx) {
        String sqlId = ctx.getSqlId();
        if (this.isSimple(sqlId)) {
            this.simpleOut(ctx);
            return;
        }
        long time = System.currentTimeMillis();
        long start = (Long) ctx.getEnv("debug.time");
        String lineSeparator = System.getProperty("line.separator", "\n");
        StringBuilder sb = (StringBuilder) ctx.getEnv("logs");
        sb.append("┣ 时间：\t " + (time - start) + "ms").append(lineSeparator);
        if (ctx.isUpdate()) {
            sb.append("┣ 更新：\t [");
            if (ctx.getResult().getClass().isArray()) {
                int[] ret = (int[]) ctx.getResult();
                for (int i = 0; i < ret.length; i++) {
                    if (i > 0) sb.append(",");
                    sb.append(ret[i]);
                }
            } else {
                sb.append(ctx.getResult());
            }
            sb.append("]").append(lineSeparator);
        } else {
            if (ctx.getResult() instanceof Collection) {
                sb.append("┣ 结果：\t [").append(((Collection) ctx.getResult()).size()).append("]").append(lineSeparator);
            } else {
                sb.append("┣ 结果：\t [").append(ctx.getResult()).append("]").append(lineSeparator);
            }

        }
        sb.append("┗━━━━━ Debug [").append(this.getSqlId(formatSql(ctx.getSqlId()))).append("] ━━━").append(lineSeparator);
        println(sb.toString());

    }

    protected boolean isDebugEanble(String sqlId) {
        if (this.includes == null) return true;
        for (String id : includes) {
            if (sqlId.startsWith(id)) {
                return true;
            }
        }
        return false;
    }

    protected List<String> formatParas(List<SQLParameter> list) {
        List<String> data = new ArrayList<String>(list.size());
        for (SQLParameter para : list) {
            Object obj = para.getValue();
            if (obj == null) {
                data.add(null);
            } else if (obj instanceof String) {
                String str = (String) obj;
                if (str.length() > 60) {
                    data.add(str.substring(0, 60) + "...(" + str.length() + ")");
                } else {
                    data.add(str);
                }
            } else if (obj instanceof Date) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                data.add(sdf.format((Date) obj));
            } else if (obj instanceof Enum) {
                Object value = EnumUtils.getValueByEnum(obj);
                data.add(String.valueOf(value));
            } else {
                data.add(obj.toString());
            }
        }
        return data;
    }

    protected void println(String str) {
        System.out.println(str);
    }

    protected String getSqlId(String sqlId) {
        if (sqlId.length() > 50) {
            sqlId = sqlId.substring(0, 50);
            sqlId = sqlId + "...";
        }
        return sqlId;
    }

    @Override
    public void exception(SQLContext ctx, Exception ex) {
        String sqlId = ctx.getSqlId();
        if (this.isSimple(sqlId)) {
            this.simpleOutException(ctx, ex);
            return;
        }
        String lineSeparator = System.getProperty("line.separator", "\n");
        StringBuilder sb = (StringBuilder) ctx.getEnv("logs");
        sb.append("┗━━━━━ Debug [ ERROR:").append(ex != null ? ex.getMessage() : "").append("] ━━━").append(lineSeparator);
        println(sb.toString());

    }

    protected String formatSql(String sql) {
        return sql.replaceAll("--.*", "").replaceAll("\\s+", " ");
    }

    protected boolean isSimple(String sqlId) {
        return false;
    }

    protected void simpleOut(SQLContext ctx) {
        String sqlId = ctx.getSqlId();
        StringBuilder sb = new StringBuilder();
        sb.append("--Sql:").append(sqlId).append(", paras:").append(formatParas(ctx.getParas()));
        this.println(sb.toString());
        return;
    }

    protected void simpleOutException(SQLContext ctx, Exception ex) {
        String sqlId = ctx.getSqlId();
        StringBuilder sb = new StringBuilder();
        sb.append("--Sql Error:");
        sb.append(ex != null ? ex.getMessage() : "");
        sb.append(" 位于 ").append(sqlId).append(", paras:").append(formatParas(ctx.getParas()));

        this.println(sb.toString());
        return;
    }


}
