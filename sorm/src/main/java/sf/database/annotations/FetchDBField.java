package sf.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 级联需要抓取的字段,只对级联关系生效
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FetchDBField {
    /**
     * 抓取的字段名称(数组格式)
     * @return
     */
    String[] value();
}
