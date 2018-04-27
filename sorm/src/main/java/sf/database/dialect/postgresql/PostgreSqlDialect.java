package sf.database.dialect.postgresql;

import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLTemplates;
import org.jooq.SQLDialect;
import sf.database.DBField;
import sf.database.dbinfo.Feature;
import sf.database.dialect.DBDialect;
import sf.database.dialect.DBProperty;
import sf.database.jdbc.type.TypeHandler;
import sf.database.meta.ColumnMapping;
import sf.database.meta.TableMapping;
import sf.database.support.RDBMS;
import sf.tools.StringUtils;

import java.sql.Connection;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PostgreSqlDialect extends DBDialect {
    public static final String NAME = "PostgreSQL";

    @Override
    public RDBMS getName() {
        return RDBMS.postgresql;
    }

    public PostgreSqlDialect() {
        features.addAll(Arrays.asList(Feature.ALTER_FOR_EACH_COLUMN, Feature.COLUMN_ALTERATION_SYNTAX, Feature.SUPPORT_CONCAT, Feature.SUPPORT_SEQUENCE, Feature.SUPPORT_LIMIT, Feature.AI_TO_SEQUENCE_WITHOUT_DEFAULT,
                Feature.SUPPORT_COMMENT));
        setProperty(DBProperty.ADD_COLUMN, "ADD COLUMN");
        setProperty(DBProperty.MODIFY_COLUMN, "ALTER COLUMN");
        setProperty(DBProperty.DROP_COLUMN, "DROP COLUMN");
        setProperty(DBProperty.CHECK_SQL, "select 1");
        setProperty(DBProperty.SEQUENCE_FETCH, "select nextval('%s')");
        setProperty(DBProperty.WRAP_FOR_KEYWORD, "\"\"");
        setProperty(DBProperty.GET_IDENTITY_FUNCTION, "SELECT currval('%tableName%_%columnName%_seq')");
    }

    public String evalFieldType(ColumnMapping mf) {
        String type = "";
        Class<?> mirror = mf.getClz();
        if (mf.getColumn() == null || StringUtils.isBlank(getColumnDefinition(mf))) {
            String other = "";
            if (mf.getColumn() != null && mf.getColumn().unique() == true) {
                other = " UNIQUE ";
            }
            if (mf.getColumn() != null && mf.getColumn().nullable() == false) {
                other = other + " NOT NULL ";
            }

            type = _evalFieldType(mf) + other;
        }
        if (StringUtils.isNotBlank(getColumnDefinition(mf))) {
            type = mf.getColumn().columnDefinition();
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
                type = "varchar(" + length + ")";
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
                type = "BIGINT";
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
                type = "BOOLEAN";
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
                    type = "NUMERIC(" + precision + "," + scale + ")";
                } else {
                    type = "NUMERIC(255,0)";
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
                type = "bytea";
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

            // 非主键的 @Name，应该加入唯一性约束


            // 自增主键特殊形式关键字
            if (cm.isPk() && cm.getGv() != null && (cm.getGv().strategy() != null)) {
                if (Number.class.isAssignableFrom((cm.getClz()))) {
                    sb.append(" SERIAL");
                } else {
                    throw new RuntimeException("主键自增,但不是数字!");
                }
            } else {
                sb.append(' ').append(evalFieldType(cm));
                // 非主键的 @Name，应该加入唯一性约束


                // 普通字段
            }
            sb.append(',');
        }
        // 创建主键
        List<ColumnMapping> pks = en.getPkFields();
        if (!pks.isEmpty()) {
            sb.append('\n');
            sb.append(String.format("CONSTRAINT %s_pkey PRIMARY KEY (",
                    en.getTableName().replace('.', '_').replace('"', '_')));
            boolean f = false;
            for (ColumnMapping pk : pks) {
                sb.append(f ? "," : "").append(pk.getRawColumnName());
                f = true;
            }
            sb.append(") \n ");
            sb.append("\n ");
        }

        // 结束表字段设置
        sb.setCharAt(sb.length() - 1, ')');
        // 设置特殊引擎
//		if (en.hasMeta(META_ENGINE)) {
//			sb.append(" ENGINE=" + en.getMeta(META_ENGINE));
//		}
        // 默认采用 UTF-8 编码

        sb.append(" ");
        // 表名注释

        // 执行创建语句
        execute(sb.toString(), conn);

        // 创建索引
        List<String> list = createIndexSql(en);
        for (String sql : list) {
            execute(sql, conn);
        }

        // 创建关联表
        createRelation(conn, en);

        // 添加注释(表注释与字段注释)
        addComment(conn, en);
        return true;
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
        return "org.hibernate.dialect.PostgreSQL94Dialect";
    }

    public String getSeqNextValSql(String seqName) {
        return "nextval(\"" + seqName + "\")";
    }

    @Override
    public SQLTemplates getQueryDslDialect() {
        return new PostgreSQLTemplates();
    }

    @Override
    public SQLDialect getJooqDialect() {
        return SQLDialect.POSTGRES;
    }
}
