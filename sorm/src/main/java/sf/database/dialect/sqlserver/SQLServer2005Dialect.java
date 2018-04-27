package sf.database.dialect.sqlserver;

import com.querydsl.sql.SQLServer2005Templates;
import com.querydsl.sql.SQLTemplates;

public class SQLServer2005Dialect extends SqlServerDialect {
    public SQLTemplates getQueryDslDialect() {
        return new SQLServer2005Templates();
    }
}
