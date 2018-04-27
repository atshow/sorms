package sf.database.jdbc.inetercept;

import sf.database.template.sql.SQLContext;

public interface SQLInterceptor {
    public void before(SQLContext ctx);

    /**
     * 如果正常执行，调用after
     * @param ctx
     */
    public void after(SQLContext ctx);

    /**
     * 如果异常，将调用exception
     * @param ctx
     * @param ex
     * @since 2.8.0
     */
    public void exception(SQLContext ctx, Exception ex);
}
