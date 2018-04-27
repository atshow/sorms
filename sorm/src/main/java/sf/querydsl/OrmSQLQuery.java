package sf.querydsl;

import com.querydsl.core.QueryMetadata;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.SQLTemplates;

import javax.inject.Provider;
import java.sql.Connection;

public class OrmSQLQuery<T> extends SQLQuery<T> {


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public OrmSQLQuery() {

    }

    public OrmSQLQuery(SQLTemplates templates) {
        super(templates);
    }

    public OrmSQLQuery(Connection conn, SQLTemplates templates) {
        super(conn, templates);
    }

    public OrmSQLQuery(Connection conn, SQLTemplates templates, QueryMetadata metadata) {
        super(conn, templates, metadata);
    }

    public OrmSQLQuery(Configuration configuration) {
        super(configuration);
    }

    public OrmSQLQuery(Connection conn, Configuration configuration) {
        super(conn, configuration);
    }

    public OrmSQLQuery(Connection conn, Configuration configuration, QueryMetadata metadata) {
        super(conn, configuration, metadata);
    }

    public OrmSQLQuery(Provider<Connection> connProvider, Configuration configuration) {
        super(connProvider, configuration);
    }

    public OrmSQLQuery(Provider<Connection> connProvider, Configuration configuration, QueryMetadata metadata) {
        super(connProvider, configuration, metadata);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public SQLSerializer serialize(boolean forCountRow) {
        return super.serialize(forCountRow);
    }
}
