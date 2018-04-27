/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package sf.database.annotations;

import sf.database.jdbc.sql.IdentifierGenerator;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * 唯一键生成策略,只对标注@Id的生效
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface UniqueKeyGenerator {
    /**
     * unique generator name.
     */
    String name() default "";

    /**
     * 定义了targetClass,以targetClass为准
     * @return
     */
    String strategy() default "";

    /**
     * 生成策略,对应的类
     */
    Class<? extends IdentifierGenerator> targetClass();

    /**
     * Optional generator parameters.
     */
    Parameter[] parameters() default {};
}
