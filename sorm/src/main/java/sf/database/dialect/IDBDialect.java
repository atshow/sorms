package sf.database.dialect;

import com.querydsl.sql.SQLTemplates;
import org.jooq.SQLDialect;

public interface IDBDialect {

    /**
     * 检查数据库是否包含指定的关键字，用来进行检查的对象名称都是按照getColumnNameIncase转换后的，因此对于大小写统一的数据库，
     * 这里无需考虑传入的大小写问题。
     * @param name
     * @return
     */
    boolean containKeyword(String name);

    SQLTemplates getQueryDslDialect();

    default SQLDialect getJooqDialect() {
        return SQLDialect.DEFAULT;
    }
}
