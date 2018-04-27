package sf.database.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({})
@Retention(RUNTIME)
public @interface Parameter {
    /**
     * The parameter name.
     */
    String name();

    /**
     * The parameter value.
     */
    String value();
}