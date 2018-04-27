package sf.database.dialect.h2;

import com.querydsl.sql.H2Templates;
import com.querydsl.sql.SQLTemplates;
import org.jooq.SQLDialect;
import sf.database.dialect.DBDialect;
import sf.database.meta.ColumnMapping;
import sf.database.meta.TableMapping;
import sf.database.support.RDBMS;
import sf.tools.StringUtils;

import java.sql.Connection;

public class H2Dialect extends DBDialect {
    public static final String NAME = "H2";

    @Override
    public RDBMS getName() {
        return RDBMS.h2;
    }

    @Override
    protected String getQuotedIdentifier(String name) {
        return "\"" + StringUtils.replace(name, "\"", "\"\"") + "\"";
    }

    @Override
    public String sqlTableDrop(String table) {
        return String.format("drop table if exists %s;", getIdentifier(table));
    }

    @Override
    public String sqlTableRename(String oldName, String newName) {
        return String.format("alter table  %s rename to %s;", getIdentifier(oldName), getIdentifier(newName));
    }

    @Override
    public String sqlColumnAdd(String table, String column_definition, String column_position) {
        String sql = String.format("alter table %s add column %s", getIdentifier(table), column_definition);
        if (supportsColumnPosition() && column_position != null) {
            sql = sql + " " + column_position;
        }
        return sql;
    }

    @Override
    public String sqlColumnModify(String table, String column_definition, String column_position) {
        String sql = String.format("alter table %s alter column %s", getIdentifier(table), column_definition);
        if (supportsColumnPosition() && column_position != null) {
            sql = sql + " " + column_position;
        }
        return sql;
    }

    @Override
    public String sqlColumnDrop(String table, String column) {
        return String.format("alter table %s drop column %s;", getIdentifier(table), getIdentifier(column));
    }

    @Override
    public String sqlPageList(String sql, int offset, int limit) {
        sql = sql + " limit " + limit;
        if (offset > 0) {
            sql = sql + " offset " + offset;
        }
        return sql;
    }

    @Override
    public boolean supportsColumnPosition() {
        return true;
    }

    @Override
    public String getHibernateDialect() {
        return "org.hibernate.dialect.H2Dialect";
    }

    @Override
    public String evalFieldType(ColumnMapping mf) {
        return null;
    }

    @Override
    public boolean createEntity(Connection conn, TableMapping en) {
        return false;
    }

    @Override
    public SQLTemplates getQueryDslDialect() {
        return new H2Templates();
    }

    @Override
    public SQLDialect getJooqDialect() {
        return SQLDialect.H2;
    }
}
