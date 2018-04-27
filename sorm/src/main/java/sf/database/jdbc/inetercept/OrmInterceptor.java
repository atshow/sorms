package sf.database.jdbc.inetercept;

import sf.database.OrmContext;

public interface OrmInterceptor {
    public void before(OrmContext ctx);

    /**
     * 如果正常执行，调用after
     * @param ctx
     */
    public void after(OrmContext ctx);

    /**
     * 如果异常，将调用exception
     * @param ctx
     * @param ex
     * @since 2.8.0
     */
    public void exception(OrmContext ctx, Exception ex);
}
