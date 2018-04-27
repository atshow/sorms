package sf.database.jdbc.inetercept;

import sf.database.template.sql.SQLContext;

import java.util.concurrent.ConcurrentLinkedQueue;

public class SQLInterceptorHelp {
    public static ConcurrentLinkedQueue<SQLInterceptor> inters = new ConcurrentLinkedQueue();

    public static SQLContext callInterceptorAsBefore(SQLContext ctx) {
        for (SQLInterceptor in : inters) {
            in.before(ctx);
        }
        return ctx;
    }

    public static void callInterceptorAsAfter(SQLContext ctx, Object result) {
        if (inters == null)
            return;
        if (!ctx.isUpdate()) {
            ctx.setResult(result);

        } else {
            ctx.setResult(result);
        }
        for (SQLInterceptor in : inters) {
            in.after(ctx);
        }
        return;
    }

    public static void callInterceptorAsException(SQLContext ctx, Exception ex) {
        if (ctx == null) {
            return;
        }
        if (inters == null)
            return;

        for (SQLInterceptor in : inters) {
            in.exception(ctx, ex);
        }
        return;
    }
}
