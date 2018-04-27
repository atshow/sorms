package sf.database.annotations;


import sf.database.jdbc.type.TypeHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 扩展数据类型的注解，可用于支持新的数据映射方式
 * @author
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Type {
    /**
     * 自定义的java和数据库类型映射实现
     * @return
     */
    Class<? extends TypeHandler> value();

    /**
     * Any configuration parameters for the named type.
     */
    Parameter[] parameters() default {};
}
