package sf.querydsl;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;

import javax.inject.Provider;
import javax.sql.DataSource;
import java.sql.Connection;

public class OrmSQLQueryFactory extends SQLQueryFactory {
    public OrmSQLQueryFactory(SQLTemplates templates, Provider<Connection> connection) {
        super(templates, connection);
    }

    public OrmSQLQueryFactory(Configuration configuration, Provider<Connection> connProvider) {
        super(configuration, connProvider);
    }

    public OrmSQLQueryFactory(Configuration configuration, DataSource dataSource) {
        super(configuration, dataSource);
    }

    public OrmSQLQueryFactory(Configuration configuration, DataSource dataSource, boolean release) {
        super(configuration, dataSource, release);
    }

    /**
     * @param tableClass 注册自定义类型,主要是对Map映射为json的支持.
     * @return
     */
    public SQLQuery<?> queryCustom(Class<?>... tableClass) {
        //注入自定义类型
        if (tableClass != null && tableClass.length > 0) {
            for (Class<?> clz : tableClass) {
                QueryDSL.setReturnType(clz, configuration);
            }
        }
        return new OrmSQLQuery<Void>(connection, configuration);
    }

    @Override
    public SQLQuery<?> query() {
        //替换默认的SQLQuery
        return queryCustom();
    }
}
