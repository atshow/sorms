package sf.dsl.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * 扩展数据类型的注解，可用于支持新的数据映射方式
 * @author
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface DSLSelect {
    String value();

}
