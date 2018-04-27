package sf.database.jdbc.inetercept;

import sf.database.OrmContext;

import java.util.concurrent.ConcurrentLinkedQueue;

public class OrmInterceptorHelp {
    public static ConcurrentLinkedQueue<OrmInterceptor> inters = new ConcurrentLinkedQueue();

    public static OrmContext callInterceptorAsBefore(OrmContext ctx) {
        for (OrmInterceptor in : inters) {
            in.before(ctx);
        }
        return ctx;
    }

    public static void callInterceptorAsAfter(OrmContext ctx, Object result) {
        if (inters == null)
            return;
        if (!ctx.isUpdate()) {
            ctx.setResult(result);

        } else {
            ctx.setResult(result);
        }
        for (OrmInterceptor in : inters) {
            in.after(ctx);
        }
        return;
    }

    public static void callInterceptorAsException(OrmContext ctx, Exception ex) {
        if (ctx == null) {
            return;
        }
        if (inters == null)
            return;

        for (OrmInterceptor in : inters) {
            in.exception(ctx, ex);
        }
        return;
    }
}
