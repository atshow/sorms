package sf.database.dialect.sqlserver;

import com.querydsl.sql.SQLServer2012Templates;
import com.querydsl.sql.SQLTemplates;

public class SQLServer2012Dialect extends SqlServerDialect {
    public SQLTemplates getQueryDslDialect() {
        return new SQLServer2012Templates();
    }
}
