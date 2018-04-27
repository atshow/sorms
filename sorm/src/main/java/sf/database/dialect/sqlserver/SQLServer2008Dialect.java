package sf.database.dialect.sqlserver;

import com.querydsl.sql.SQLServer2008Templates;
import com.querydsl.sql.SQLTemplates;

public class SQLServer2008Dialect extends SqlServerDialect {
    public SQLTemplates getQueryDslDialect() {
        return new SQLServer2008Templates();
    }
}
