package sf.database.dialect.sqlite;

import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.SQLiteTemplates;
import org.jooq.SQLDialect;
import sf.database.DBField;
import sf.database.dbinfo.Feature;
import sf.database.dialect.DBDialect;
import sf.database.dialect.DBProperty;
import sf.database.jdbc.type.TypeHandler;
import sf.database.meta.CascadeConfig;
import sf.database.meta.ColumnMapping;
import sf.database.meta.TableMapping;
import sf.database.support.RDBMS;
import sf.tools.StringUtils;

import java.sql.Connection;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SqliteDialect extends DBDialect {
    public static final String NAME = "sqlite";

    @Override
    public RDBMS getName() {
        return RDBMS.sqlite;
    }

    public SqliteDialect() {
        features.add(Feature.SUPPORT_CONCAT);
        features.add(Feature.AUTOINCREMENT_MUSTBE_PK);
        features.add(Feature.TYPE_FORWARD_ONLY);
        features.add(Feature.BATCH_GENERATED_KEY_ONLY_LAST);

        features.add(Feature.NOT_SUPPORT_TRUNCATE);
        features.add(Feature.NOT_SUPPORT_FOREIGN_KEY);
        features.add(Feature.NOT_SUPPORT_USER_FUNCTION);
        features.add(Feature.NOT_SUPPORT_SET_BINARY);
        features.add(Feature.NOT_SUPPORT_INDEX_META);
        features.add(Feature.NOT_SUPPORT_KEYWORD_DEFAULT);
        features.add(Feature.NOT_SUPPORT_ALTER_DROP_COLUMN);
        features.add(Feature.ONE_COLUMN_IN_SINGLE_DDL);

        setProperty(DBProperty.ADD_COLUMN, "ADD COLUMN");
        setProperty(DBProperty.MODIFY_COLUMN, "MODIFY COLUMN");
        setProperty(DBProperty.DROP_COLUMN, "DROP COLUMN");
        setProperty(DBProperty.CHECK_SQL, "select 1");
        setProperty(DBProperty.SELECT_EXPRESSION, "select %s");
        setProperty(DBProperty.WRAP_FOR_KEYWORD, "\"\"");
        setProperty(DBProperty.GET_IDENTITY_FUNCTION, "select last_insert_rowid()");
    }

    public String evalFieldType(ColumnMapping mf) {
        String type = "";
        if (mf.getColumn() == null || StringUtils.isBlank(getColumnDefinition(mf))) {
            String other = "";
            if (mf.getColumn() != null && mf.getColumn().unique() == true) {
                other = " UNIQUE ";
            }
            if (mf.getColumn() != null && mf.getColumn().nullable() == false) {
                other = other + " NOT NULL ";
            }
            type = _evalFieldType(mf);
            type = type + other;
        }
        String definition = getColumnDefinition(mf);
        if (StringUtils.isNotBlank(definition)) {
            type = definition;
        }
        if (mf.getType() != null) {
            Class<? extends TypeHandler> clz = mf.getType().value();
            try {
                TypeHandler handler = clz.newInstance();
                switch (handler.getSqlType()) {
                    case Types.VARCHAR:
                        type = "text";
                        break;
                    default:
                        break;
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        // 其它的参照默认字段规则 ...
        return type;
    }

    private String _evalFieldType(ColumnMapping mf) {
        String type = "";
        int length = getColumnLength(mf);
        int scale = getColumnScale(mf);
        int precision = getColumnPrecision(mf);
        switch (mf.getSqlType()) {
            case Types.VARCHAR: {
                type = "nvarchar(" + length + ")";
                break;
            }
            case Types.TINYINT: {
                type = "SMALLINT";
                break;
            }
            case Types.SMALLINT: {
                type = "SMALLINT";
                break;
            }
            case Types.INTEGER: {
                type = "INT";
                break;
            }
            case Types.BIGINT: {
                type = "decimal(8,0)";
                break;
            }
            case Types.FLOAT: {
                type = "FLOAT";
                break;
            }
            case Types.DOUBLE:
                type = "DOUBLE";
                break;
            case Types.BOOLEAN: {
                type = "tinyint(1)";
                break;
            }
            case Types.DECIMAL: {
                // BigDecimal
                if (scale > 0 && precision > 0) {
                    type = "DECIMAL(" + precision + "," + scale + ")";
                } else {
                    throw new UnsupportedOperationException("Unsupport type!");
                }
                break;
            }
            case Types.NUMERIC: {
                if (precision > 0) {
                    type = "DECIMAL(" + precision + "," + scale + ")";
                } else {
                    type = "DECIMAL(255,0)";
                }
                break;
            }
            case Types.DATE:
                type = "DATE";
                break;
            case Types.TIME:
                type = "TIME";
                break;
            case Types.TIMESTAMP:
                type = "TIMESTAMP";
                break;
            case Types.BLOB:
                type = "blob";
                break;
            case Types.CLOB: {
                if (mf.isLob() || (mf.getColumn() != null && length >= 1000)) {
                    type = "text";
                }
                break;
            }
            default:
                break;
        }
        return type;
    }

    @Override
    public boolean createEntity(Connection conn, TableMapping en) {
        StringBuilder sb = new StringBuilder("CREATE TABLE " + en.getTableName() + "(");
        // 创建字段
        for (Map.Entry<DBField, ColumnMapping> mf : en.getSchemaMap().entrySet()) {
            DBField f = mf.getKey();
            ColumnMapping cm = mf.getValue();

            sb.append('\n').append(cm.getRawColumnName());
            // Sqlite的整数型主键,一般都是自增的,必须定义为(PRIMARY KEY
            // AUTOINCREMENT),但这样就无法定义多主键!!
            if (cm.isPk() && cm.getGv() != null && (cm.getGv().strategy() != null)) {
                if (Number.class.isAssignableFrom((cm.getClz()))) {
                    sb.append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
                } else {
                    throw new RuntimeException("主键自增,但不是数字!");
                }
                continue;
            } else {
                sb.append(' ').append(_evalFieldType(cm));
            }
            sb.append(',');
        }
        // 创建主键
        List<ColumnMapping> pks = en.getPkFields();
        if (!pks.isEmpty() && pks.size() > 1) {
            sb.append('\n');
            sb.append("constraint pk_").append(en.getTableName()).append(" PRIMARY KEY (");
            boolean f = false;
            for (ColumnMapping pk : pks) {
                sb.append(f ? "," : "").append(pk.getRawColumnName());
                f = true;
            }
            sb.append(") \n ");
        }

        // 结束表字段设置
        sb.setCharAt(sb.length() - 1, ')');
        // 设置特殊引擎
//		if (en.hasMeta(META_ENGINE)) {
//			sb.append(" ENGINE=" + en.getMeta(META_ENGINE));
//		}
        // 默认采用 UTF-8 编码

        sb.append(" ");

        // 执行创建语句
        execute(sb.toString(), conn);

        // 创建索引
        List<String> list = createIndexSql(en);
        for (String sql : list) {
            execute(sql, conn);
        }

        // 创建关联表
        createRelation(conn, en);
        return true;
    }

    protected List<String> createRelation(Connection conn, ColumnMapping en) {
        List<String> sqls = new ArrayList<>();
        List<String> columns = new ArrayList<>();
        CascadeConfig cc = en.getCascadeConfig();
        if (existsTables(conn, cc.getMiddleTableName()))
            return sqls;
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE " + cc.getMiddleTableName() + "(" + "\n");
        int count = cc.getMiddleTableColumns().size();

        for (Map.Entry<String, ColumnMapping> entry : cc.getMiddleTableColumns().entrySet()) {
            columns.add(entry.getKey());
            sb.append(entry.getKey() + " " + evalFieldType(entry.getValue()) + "," + "\n");
        }
        sb.append("constraint pk_").append(cc.getMiddleTableName()).append(" PRIMARY KEY (");
        boolean f = false;
        for (String column : columns) {
            sb.append(f ? "," : "").append(column);
            f = true;
        }
        sb.append(") )");
        sqls.add(sb.toString());
        return sqls;
    }

    @Override
    protected String getQuotedIdentifier(String name) {
//        return "`" + name + "`";
        return super.getQuotedIdentifier(name);
    }

    @Override
    protected String escapeSqlValue(String value) {
        return StringUtils.replace(value, "'", "\\'");
    }

    @Override
    public String sqlTableDrop(String table) {
        return String.format("drop table if exists %s;", getIdentifier(table));
    }

    @Override
    public String sqlTableRename(String oldName, String newName) {
        return String.format("rename table  %s to %s;", getIdentifier(oldName), getIdentifier(newName));
    }

    @Override
    public String sqlColumnAdd(String table, String column_definition, String column_position) {
        String sql = String.format("alter table %s add %s", getIdentifier(table), column_definition);
        if (supportsColumnPosition() && column_position != null) {
            sql = sql + " " + column_position;
        }
        return sql + ";";
    }

    @Override
    public String sqlColumnModify(String table, String column_definition, String column_position) {
        String sql = String.format("alter table %s modify %s", getIdentifier(table), column_definition);
        if (supportsColumnPosition() && column_position != null) {
            sql = sql + " " + column_position;
        }
        return sql + ";";
    }

    @Override
    public String sqlColumnDrop(String table, String column) {
        return String.format("alter table %s drop %s;", getIdentifier(table), getIdentifier(column));
    }

    @Override
    public String sqlPageList(String sql, int offset, int limit) {
        if (offset > 0) {
            return sql + " limit " + limit + " offset " + offset;
        } else {
            return sql + " limit " + limit;
        }
    }

    @Override
    public boolean supportsColumnPosition() {
        return true;
    }

    @Override
    public String getHibernateDialect() {
        return "org.hibernate.dialect.MySQLDialect";
    }

    @Override
    public SQLTemplates getQueryDslDialect() {
        return new SQLiteTemplates();
    }

    @Override
    public SQLDialect getJooqDialect() {
        return SQLDialect.SQLITE;
    }
}
