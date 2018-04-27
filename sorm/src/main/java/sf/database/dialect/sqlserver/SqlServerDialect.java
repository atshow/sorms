package sf.database.dialect.sqlserver;


import com.querydsl.sql.SQLServerTemplates;
import com.querydsl.sql.SQLTemplates;
import org.jooq.SQLDialect;
import org.jooq.impl.IdentifiersHelp;
import sf.database.dbinfo.Feature;
import sf.database.dbinfo.SequenceInfo;
import sf.database.dialect.DBDialect;
import sf.database.dialect.DBProperty;
import sf.database.jdbc.sql.Crud;
import sf.database.meta.ColumnMapping;
import sf.database.meta.TableMapping;
import sf.database.support.RDBMS;
import sf.tools.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SqlServerDialect extends DBDialect {
    public static final String NAME = "Microsoft SQL Server";


    @Override
    public RDBMS getName() {
        return RDBMS.sqlserver;
    }

    public SqlServerDialect() {
        features.add(Feature.COLUMN_DEF_ALLOW_NULL);
        features.add(Feature.CONCAT_IS_ADD);
        features.add(Feature.NOT_SUPPORT_KEYWORD_DEFAULT);
        features.add(Feature.BATCH_GENERATED_KEY_BY_FUNCTION);
        features.add(Feature.SUPPORT_COMMENT);
        // features.add(Feature.NO_BIND_FOR_INSERT);
        // features.add(Feature.NO_BIND_FOR_SELECT);

        setProperty(DBProperty.ADD_COLUMN, "ADD");
        setProperty(DBProperty.MODIFY_COLUMN, "ALTER COLUMN");
        setProperty(DBProperty.DROP_COLUMN, "DROP COLUMN");
        setProperty(DBProperty.ADD_CONSTRAINT, "ADD CONSTRAINT");
        setProperty(DBProperty.CHECK_SQL, "select 1");
        setProperty(DBProperty.GET_IDENTITY_FUNCTION, "SELECT @@IDENTITY");
        setProperty(DBProperty.WRAP_FOR_KEYWORD, "[]");
        setProperty(DBProperty.DROP_INDEX_TABLE_PATTERN, "%2$s.%1$s");
    }

    @Override
    protected String getQuotedIdentifier(String name) {
        return "[" + name + "]";
    }

    @Override
    public String getSqlStatmentSeparator() {
        return "go" + "\n";
    }

    @Override
    public String sqlTableDrop(String table) {
        String sql = "if exists (select * from dbo.sysobjects where id=object_id(N'%s') and objectproperty(id,N'IsUserTable')=1) drop table %s;";
        table = getIdentifier(table);
        return String.format(sql, table, table);
    }

    @Override
    public String sqlTableRename(String oldName, String newName) {
        oldName = getIdentifier(oldName);
        newName = getIdentifier(newName);
        return "alter table  " + oldName + " rename to " + newName + ";";
    }

    @Override
    public String sqlColumnAdd(String table, String column_definition, String column_position) {
        return String.format("alter table %s add %s;", getIdentifier(table), column_definition);
    }

    @Override
    public String sqlColumnModify(String table, String column_definition, String column_position) {
        return String.format("alter table %s alter column %s;", getIdentifier(table), column_definition);
    }

    @Override
    public String sqlColumnDrop(String table, String column) {
        return String.format("alter table %s drop column %s;", getIdentifier(table), getIdentifier(column));
    }

    @Override
    public String sqlPageList(String sql, int offset, int limit) {
        if (offset == 0) {
            sql = "select top " + limit + " * from (" + sql + ") as temp";
        } else {
            sql = sql.replaceAll("\\s+", " ");
            // 从原始 sql 中获取 order by 子句
            int orderby_pos = sql.toLowerCase().lastIndexOf(" order by ");
            String sorts = null;
            if (orderby_pos > 0) {
                sorts = sql.substring(orderby_pos);
                if (sorts.indexOf(")") > 0) {
                    sorts = null; // skip the nested order by
                }
            }
            if (sorts == null) {
                sorts = "order by current_timestamp";
            }
            //@formatter:off
            sql = "select * from ("
                    + "  select top " + (offset + limit) + " row_number() over(" + sorts + ") as row, * from (" + sql + ")"
                    + ") as temp where row > " + offset;
            //@formatter:on
        }
        return sql;
    }

    @Override
    public String getHibernateDialect() {
        return "org.hibernate.dialect.SQLServerDialect";
    }

    @Override
    public boolean createEntity(Connection conn, TableMapping en) {
        return false;
    }

    @Override
    public String evalFieldType(ColumnMapping mf) {
        return null;
    }

    @Override
    public List<SequenceInfo> getSequenceInfo(Connection conn, String schema, String seqName) throws SQLException {
        schema = StringUtils.isBlank(schema) ? "%" : schema;
        seqName = StringUtils.isBlank(seqName) ? "%" : seqName;
        String sql = "SELECT CONVERT(varchar,seq.name),CONVERT(int,seq.cache_size),"
                + "CONVERT(bigint,seq.current_value),"
                + "CONVERT(bigint,seq.minimum_value),"
                + "CONVERT(bigint,seq.start_value),"
                + "CONVERT(int,seq.increment),"
                + "CONVERT(varchar,m.name) as schema_name from sys.sequences seq, sys.schemas m WHERE seq.SCHEMA_ID=m.SCHEMA_ID "
                + "AND seq.name LIKE ? AND m.name LIKE ?";
        List<SequenceInfo> result = new ArrayList<SequenceInfo>();
        Crud.getInstance().getCrudSql().selectResultSet(conn, (rs) -> {
            while (rs.next()) {
                SequenceInfo seq = new SequenceInfo();
                seq.setCatalog(null);
                seq.setSchema(rs.getString(1));
                seq.setName(rs.getString(2));
                seq.setMinValue(rs.getLong(3));
                //	seq.setMaxValue(rs.getLong(4));
                seq.setStep(rs.getInt(5));
                seq.setCacheSize(rs.getInt(6));
                seq.setCurrentValue(rs.getLong(7));
                result.add(seq);
            }
            return null;
        }, sql, schema, seqName);
        return result;
    }

    @Override
    public SQLTemplates getQueryDslDialect() {
        return new SQLServerTemplates();
    }

    @Override
    public SQLDialect getJooqDialect() {
        IdentifiersHelp.addOuterDB();
        return SQLDialect.valueOf(IdentifiersHelp.SQLSERVER);
    }
}
