package sf.database.annotations;

import sf.tools.Configuration;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 加在类上，用于将这个类所对应的实体绑定到某个数据源上。（当多数据源时下，针对该表的操作即自动路由到指定的datasource上执行）
 * <p>
 * 如果当{@link Configuration.Item#DB_SINGLE_DATASOURCE}配置为true时，此配置被自动忽略
 * <p>
 * 实际使用中，一般要配合datasource重定向功能使用。
 * @see Configuration.Item#DB_SINGLE_DATASOURCE
 * @see Configuration.Item#DB_DATASOURCE_MAPPING
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface BindDataSource {
    /**
     * @return
     * @see BindDataSource
     */
    String value() default "";
}
