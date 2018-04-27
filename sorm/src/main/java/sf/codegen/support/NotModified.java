package sf.codegen.support;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * 标识不要做增强
 */
@Target(TYPE)
public @interface NotModified {

}
