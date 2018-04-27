package sf.database.dialect.oracle;


import com.querydsl.sql.OracleTemplates;
import com.querydsl.sql.SQLTemplates;
import org.jooq.SQLDialect;
import sf.common.log.LogUtil;
import sf.database.DBField;
import sf.database.dbinfo.Feature;
import sf.database.dbinfo.SequenceInfo;
import sf.database.dialect.DBDialect;
import sf.database.dialect.DBProperty;
import sf.database.dialect.SqlType;
import sf.database.dialect.SubStyleType;
import sf.database.jdbc.sql.Crud;
import sf.database.jdbc.type.TypeHandler;
import sf.database.meta.ColumnMapping;
import sf.database.meta.TableMapping;
import sf.database.support.RDBMS;
import sf.tools.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OracleDialect extends DBDialect {
    public static final String NAME = "Oracle";
    //指定oracle表空间的TableMeta' key

    //oracle 创建表时指定表空间的默认sql
    private static String CTS = "tablespace %s\n" +
            "  pctfree 10\n" +
            "  initrans 1\n" +
            "  maxtrans 255\n" +
            "  storage\n" +
            "  (\n" +
            "    initial 64K\n" +
            "    minextents 1\n" +
            "    maxextents unlimited\n" +
            "  )";

    private static String CSEQ = "CREATE SEQUENCE {0}_{1}_SEQ  MINVALUE 1"
            + " MAXVALUE 999999999999 INCREMENT BY 1 START"
            + " WITH 1 CACHE 20 NOORDER  NOCYCLE";
    private static String DSEQ = "DROP SEQUENCE {0}_{1}_SEQ";

    private static String CTRI = "create or replace trigger {0}_{1}_ST"
            + " BEFORE INSERT ON {0}"
            + " FOR EACH ROW"
            + " BEGIN "
            + " IF :new.{1} IS NULL THEN"
            + " SELECT {0}_{1}_seq.nextval into :new.{1} FROM dual;"
            + " END IF;"
            + " END {0}_{1}_ST;";

    @Override
    public RDBMS getName() {
        return RDBMS.oracle;
    }

    public OracleDialect() {
        features.addAll(Arrays.asList(Feature.AUTOINCREMENT_NEED_SEQUENCE, Feature.USER_AS_SCHEMA, Feature.REMARK_META_FETCH, Feature.BRUKETS_FOR_ALTER_TABLE, Feature.SUPPORT_CONCAT, Feature.SUPPORT_CONNECT_BY, Feature.DROP_CASCADE, Feature.SUPPORT_SEQUENCE,
                Feature.EMPTY_CHAR_IS_NULL, Feature.SUPPORT_COMMENT, Feature.COLUMN_DEF_ALLOW_NULL));

        features.add(Feature.SELECT_ROW_NUM);
        setProperty(DBProperty.ADD_COLUMN, "ADD");
        setProperty(DBProperty.MODIFY_COLUMN, "MODIFY");
        setProperty(DBProperty.DROP_COLUMN, "DROP");
        setProperty(DBProperty.CHECK_SQL, "SELECT 1 FROM DUAL");
        setProperty(DBProperty.SEQUENCE_FETCH, "SELECT %s.NEXTVAL FROM DUAL");
        setProperty(DBProperty.SELECT_EXPRESSION, "SELECT %s FROM DUAL");
        setProperty(DBProperty.WRAP_FOR_KEYWORD, "\"\"");
        setProperty(DBProperty.OTHER_VERSION_SQL, "select 'USER_LANGUAGE',userenv('language') from dual");
    }


    public boolean createEntity(Connection conn, TableMapping en) {
        StringBuilder sb = new StringBuilder("CREATE TABLE " + en.getTableName() + "(");
        // 创建字段
        for (Map.Entry<DBField, ColumnMapping> entry : en.getSchemaMap().entrySet()) {
            DBField f = entry.getKey();
            ColumnMapping mf = entry.getValue();
            if (mf.getJpaTransient() != null)
                continue;
            sb.append('\n').append(mf.getRawColumnName());
            sb.append(' ').append(evalFieldType(mf));


            if (mf.isPk() && en.getPkFields().size() == 1)
                sb.append(" primary key ");
        }
        sb.append(',');


        // 结束表字段设置
        sb.setCharAt(sb.length() - 1, ')');

        //指定表空间
        if (en.getTable() != null && en.getTable().schema() != null) {
            sb.append(String.format(CTS, en.getTable().schema()));
        }

        List<String> sqls = new ArrayList<String>();
        sqls.add(sb.toString());

        // 创建复合主键
        List<ColumnMapping> pks = en.getPkFields();
        if (pks.size() > 1)

        {
            StringBuilder pkNames = new StringBuilder();
            for (ColumnMapping pk : pks) {
                pkNames.append(pk.getRawColumnName()).append(',');
            }
            pkNames.setLength(pkNames.length() - 1);

            String pkNames2 = en.getTableName();

            String sql = String.format("alter table %s add constraint pk_%s primary key (%s)",
                    en.getTableName(),
                    pkNames2,
                    pkNames);
            sqls.add(sql);
        }
        // // 处理非主键unique
        // for (MappingField mf : en.getMappingFields()) {
        // if(!mf.isPk())
        // continue;
        // String sql =
        // gSQL("alter table ${T} add constraint unique_key_${F} unique (${F});",
        // en.getTableName(),mf.getColumnName());
        // sqls.add(Sqls.create(sql));
        // }
        // 处理AutoIncreasement
        for (ColumnMapping cm : en.getMetaFields()) {
            if (cm.getGv() != null && cm.getGv().strategy() != null) {
                if (Number.class.isAssignableFrom((cm.getClz()))) {
                    // 序列
                    sqls.add(gSQL(CSEQ, en.getTableName(), cm.getRawColumnName()));
                    // 触发器
                    sqls.add(gSQL(CTRI, en.getTableName(), cm.getRawColumnName()));
                }
            }
        }

        // 创建索引
        List<String> list = createIndexSql(en);
        sqls.addAll(list);

        // TODO 详细处理Clob
        // TODO 详细处理Blob

        // 执行创建语句
        for (String sql : list) {
            execute(sql, conn);
        }

        // 创建关联表
        createRelation(conn, en);

        // 添加注释(表注释与字段注释)
        addComment(conn, en);

        return true;
    }


    public String evalFieldType(ColumnMapping mf) {
        String type = "";
        Class<?> mirror = mf.getClz();
        if (mf.getColumn() == null || StringUtils.isBlank(getColumnDefinition(mf))) {
            String other = "";
            if (mf.getColumn() != null && mf.getColumn().unique()) {
                other = " UNIQUE ";
            }
            if (mf.getColumn() != null && !mf.getColumn().nullable()) {
                other = other + " NOT NULL ";
            }
            type = _evalFieldType(mf);
            type = type + other;
        }
        //覆盖columnDefinition
        if (StringUtils.isNotBlank(getColumnDefinition(mf))) {
            type = getColumnDefinition(mf);
        }
        //处理自定义映射
        if (mf.getType() != null) {
            Class<? extends TypeHandler> clz = mf.getType().value();
            try {
                TypeHandler handler = clz.newInstance();
                switch (handler.getSqlType()) {
                    case Types.VARCHAR:
                        type = "nclob";
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
        switch (mf.getSqlType()) {
            case Types.CHAR: {
                int length = getColumnLength(mf);
                type = "nchar(" + length + ")";
                break;
            }
            case Types.VARCHAR: {
                int length = getColumnLength(mf);
                type = "NVARCHAR2(" + length + ")";
                break;
            }
            case Types.TINYINT: {
                type = "NUMBER";
                break;
            }
            case Types.SMALLINT: {
                type = "NUMBER";
                break;
            }
            case Types.INTEGER: {
                // 用户自定义了宽度
                if (getColumnLength(mf) > 0) {
                    type = "NUMBER(" + getColumnLength(mf) + ")";
                } else {
                    // 用数据库的默认宽度
                    type = "number(20, 0)";
                }
                break;
            }
            case Types.BIGINT: {
                type = "number(38, 0)";
                break;
            }
            case Types.FLOAT: {
                // 用户自定义了精度
                int scale = getColumnScale(mf);
                int precision = getColumnPrecision(mf);
                if (precision > 0 && scale > 0) {
                    type = "NUMBER(" + precision + "," + scale + ")";
                } else {
                    type = "NUMBER";
                }
                break;
            }
            case Types.DOUBLE:
                // 用默认精度
                type = "number(38,10)";
                break;
            case Types.BOOLEAN: {
                type = "number(1)";
                break;
            }
            case Types.DECIMAL: {
                // BigDecimal
                int scale = getColumnScale(mf);
                int precision = getColumnPrecision(mf);
                if (scale > 0 && precision > 0) {
                    type = "NUMBER(" + precision + "," + scale + ")";
                } else {
                    throw new UnsupportedOperationException("Unsupport type!");
                }
                break;
            }
            case Types.NUMERIC: {
                int scale = getColumnScale(mf);
                int precision = getColumnPrecision(mf);
                if (precision > 0) {
                    type = "NUMERIC(" + precision + "," + scale + ")";
                } else {
                    type = "NUMERIC(255,0)";
                }
                break;
            }
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                type = "DATE";
                break;
            case Types.BLOB:
                type = "BLOB";
                break;
            case Types.CLOB: {
                int length = getColumnLength(mf);
                if (mf.isLob() || (mf.getColumn() != null && length >= 1000)) {
                    type = "nclob";
                }
                break;
            }
            default:
                type = super.evalFieldType(mf);
        }
        return type;
    }

    @Override
    public boolean dropEntity(Connection conn, TableMapping en) {
        if (super.dropEntity(conn, en)) {
            if (en.getPkFields().isEmpty())
                return true;
            List<String> sqls = new ArrayList<String>();
            for (ColumnMapping pk : en.getPkFields()) {
                if (pk.getGv() != null && pk.getGv().strategy() != null) {
                    String sql = gSQL(DSEQ, en.getTableName(), pk.getRawColumnName());
                    sqls.add(sql);
                }
            }
            try {
                execute(sqls, conn);
                return true;
            } catch (Exception e) {
            }
        }
        return false;
    }

    @Override
    protected String getQuotedIdentifier(String name) {
        return "\"" + name + "\"";
    }

    @Override
    public String sqlTableDrop(String table) {
        return String.format("drop table %s;", getIdentifier(table));
    }

    @Override
    public String sqlTableRename(String oldName, String newName) {
        oldName = getIdentifier(oldName);
        newName = getIdentifier(newName);
        return "alter table  " + oldName + " rename to " + newName + ";";
    }

    @Override
    public String sqlColumnAdd(String table, String column_definition, String column_position) {
        return String.format("alter table %s add column %s;", getIdentifier(table), column_definition);
    }

    @Override
    public String sqlColumnModify(String table, String column_definition, String column_positions) {
        return String.format("alter table %s alter column %s;", getIdentifier(table), column_definition);
    }

    @Override
    public String sqlColumnDrop(String table, String column) {
        return String.format("alter table %s drop column %s;", getIdentifier(table), getIdentifier(column));
    }

    @Override
    public String sqlPageList(String sql, int offset, int limit) {
        //@formatter:off
        sql = "select * from ("
                + "  select t.*, ROWNUM row from ("
                + sql
                + "  ) t where ROWNUM <= " + (offset + limit) + ")";
        //@formatter:on
        if (offset > 0) {
            sql = sql + " where row > " + offset;
        }
        return sql;
    }

    @Override
    public String getHibernateDialect() {
        return "org.hibernate.dialect.OracleDialect";
    }


    public String asSqlType(String type, Integer length, Integer scale) {
        if (SubStyleType.CHAR.equals(type)) {
            return new SqlType("nchar", length, null).toString();
        } else if (SubStyleType.VARCHAR.equals(type)) {
            return new SqlType("nvarchar2", length, null).toString();
        } else if (SubStyleType.TEXT.equals(type)) {
            return "nclob";
        } else if (SubStyleType.BOOLEAN.equals(type)) {
            return "number(1)";
        } else if (SubStyleType.INT.equals(type)) {
            return "number(10)";
        } else if (SubStyleType.LONG.equals(type)) {
            return "number(20, 0)";
        } else if (SubStyleType.BIGINT.equals(type)) {
            return "number(38, 0)";
        } else if (SubStyleType.DOUBLE.equals(type)) {
            return "number(38,10)";
        } else if (SubStyleType.DECIMAL.equals(type)) {
            return new SqlType("number", length, scale).toString();
        } else if (SubStyleType.DATETIME.equals(type)) {
            return "date";
        } else if (SubStyleType.TIMESTAMP.equals(type)) {
            return "date";
        } else if (SubStyleType.DATE.equals(type)) {
            return "date";
        } else if (SubStyleType.TIME.equals(type)) {
            return "date";
        } else if (SubStyleType.CLOB.equals(type)) {
            return "nclob";
        } else if (SubStyleType.INPUTSTREAM.equals(type)) {
            return "blob";
        }
        return null;
    }

    public String getSeqNextValSql(String seqName) {
        return seqName + ".nextval";
    }

    @Override
    public List<SequenceInfo> getSequenceInfo(Connection conn, String schema, String seqName) {
        String sql = "select sequence_owner,sequence_name,min_value,max_value,increment_by,cache_size,last_number from all_sequences where sequence_owner like ? and sequence_name like ?";
        schema = StringUtils.isBlank(schema) ? "%" : schema.toUpperCase();
        seqName = StringUtils.isBlank(seqName) ? "%" : seqName.toUpperCase();
        try {
            List<SequenceInfo> result = new ArrayList<SequenceInfo>();
            Crud.getInstance().getCrudSql().selectResultSet(conn, (rs) -> {
                while (rs != null && rs.next()) {
                    SequenceInfo seq = new SequenceInfo();
                    seq.setCatalog(null);
                    seq.setSchema(rs.getString(1));
                    seq.setName(rs.getString(2));
                    seq.setMinValue(rs.getLong(3));
//						seq.setMaxValue(rs.getLong(4));
                    seq.setStep(rs.getInt(5));
                    seq.setCacheSize(rs.getInt(6));
                    seq.setCurrentValue(rs.getLong(7));
                    result.add(seq);
                }
                return null;
            }, sql, schema, seqName);
        } catch (SQLException e) {
            LogUtil.error("Error while getting sequence info [{}.{}].", schema, seqName, e);
        }
        return null;
    }

    @Override
    public SQLTemplates getQueryDslDialect() {
        return new OracleTemplates();
    }

    @Override
    public SQLDialect getJooqDialect() {
        return SQLDialect.DEFAULT;
    }
}
