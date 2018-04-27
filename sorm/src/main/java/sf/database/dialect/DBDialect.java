package sf.database.dialect;

import sf.common.log.LogUtil;
import sf.common.log.OrmLog;
import sf.database.dbinfo.*;
import sf.database.dialect.h2.H2Dialect;
import sf.database.dialect.mysql.MySqlDialect;
import sf.database.dialect.oracle.OracleDialect;
import sf.database.dialect.postgresql.PostgreSqlDialect;
import sf.database.dialect.sqlite.SqliteDialect;
import sf.database.dialect.sqlserver.SqlServerDialect;
import sf.database.meta.CascadeConfig;
import sf.database.meta.ColumnMapping;
import sf.database.meta.TableMapping;
import sf.database.support.RDBMS;
import sf.database.util.DBUtils;
import sf.tools.StringUtils;

import javax.persistence.Index;
import javax.persistence.Table;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;

public abstract class DBDialect implements IDBDialect {
    private static final Map<String, DBDialect> DIALECT_MAP = new HashMap<String, DBDialect>();

    /**
     * 各种文本属性
     */
    private Map<DBProperty, String> properties = new IdentityHashMap<DBProperty, String>();
    /**
     * 各种Boolean特性
     */
    protected Set<Feature> features = new HashSet<>();

    /**
     * 数据库关键字
     */
    protected final Set<String> keywords = new HashSet<String>();

    static {
        MySqlDialect mysql = new MySqlDialect();
        DIALECT_MAP.put(MySqlDialect.NAME.toLowerCase(), mysql);
        DIALECT_MAP.put(mysql.getName().name().toLowerCase(), mysql);

        PostgreSqlDialect postgresql = new PostgreSqlDialect();
        DIALECT_MAP.put(PostgreSqlDialect.NAME.toLowerCase(), postgresql);
        DIALECT_MAP.put(postgresql.getName().name().toLowerCase(), postgresql);

        SqliteDialect sqlite = new SqliteDialect();
        DIALECT_MAP.put(SqliteDialect.NAME.toLowerCase(), sqlite);
        DIALECT_MAP.put(sqlite.getName().name().toLowerCase(), sqlite);

        SqlServerDialect sqlserver = new SqlServerDialect();
        DIALECT_MAP.put(SqlServerDialect.NAME.toLowerCase(), sqlserver);
        DIALECT_MAP.put(sqlserver.getName().name().toLowerCase(), sqlserver);

        OracleDialect oracle = new OracleDialect();
        DIALECT_MAP.put(OracleDialect.NAME.toLowerCase(), oracle);
        DIALECT_MAP.put(oracle.getName().name().toLowerCase(), oracle);

        H2Dialect h2 = new H2Dialect();
        DIALECT_MAP.put(H2Dialect.NAME.toLowerCase(), h2);
        DIALECT_MAP.put(h2.getName().name().toLowerCase(), h2);
    }

    public static DBDialect getDialect(String name) {
        DBDialect dialect = (name == null) ? null : DIALECT_MAP.get(name.toLowerCase());
        if (dialect == null) {
            throw new IllegalStateException("Unsupport database, dialect.name = " + name);
        }
        return dialect;
    }

    public DBDialect() {

    }

    protected void setProperty(DBProperty key, String value) {
        properties.put(key, value);
    }

    public String getProperty(DBProperty key) {
        return properties.get(key);
    }

    public String getProperty(DBProperty key, String defaultValue) {
        String value = properties.get(key);
        return value == null ? defaultValue : value;
    }

    public int getPropertyInt(DBProperty key) {
        String s = properties.get(key);
        if (StringUtils.isEmpty(s)) {
            return 0;
        }
        return Integer.parseInt(s);
    }

    public long getPropertyLong(DBProperty key) {
        String s = properties.get(key);
        if (StringUtils.isEmpty(s)) {
            return 0;
        }
        return Long.parseLong(s);
    }

    public abstract RDBMS getName();

    /**
     * 返回对应的 Hibernate 的 Dialect
     */
    public abstract String getHibernateDialect();

    /**
     * 是否支持在添加字段的时候，指定字段位置
     */
    public boolean supportsColumnPosition() {
        return false;
    }

    /**
     * 将字段名/表名与SQL保留字冲突的名称进行 Wrapper
     */
    public String getIdentifier(String name) {
        return getQuotedIdentifier(name);
    }

    /**
     * 将字段名/表名进行 Wrapper
     */
    protected String getQuotedIdentifier(String name) {
        return name;
    }

    /**
     * SQL 语句中的字符串值，默认编码单引号(')为双单引号(')
     */
    protected String escapeSqlValue(String value) {
        return StringUtils.replace(value, "'", "''");
    }

    /**
     * 批量运行多个SQL语句之间的分隔符。
     */
    public String getSqlStatmentSeparator() {
        return "";
    }

    public abstract String sqlTableDrop(String table);

    public abstract String sqlTableRename(String oldName, String newName);

    public abstract String sqlColumnAdd(String table, String column_definition, String column_position);

    public abstract String sqlColumnModify(String table, String column_definition, String column_position);

    public abstract String sqlColumnDrop(String table, String column);

    public List<SequenceInfo> getSequenceInfo(Connection conn, String schema, String seqName) throws SQLException {
        throw new SQLFeatureNotSupportedException("不支持");
    }

    /**
     * 获取下一个序列值得sql语句
     * @param seqName 序列名称
     * @return
     */
    public String getSeqNextValSql(String seqName) {
        throw new UnsupportedOperationException("不支持");
    }

    public boolean notHas(Feature feature) {
        return !features.contains(feature);
    }

    public boolean has(Feature feature) {
        return features.contains(feature);
    }

    /**
     * 生成分页sql
     * @param sql    原始sql
     * @param offset 开始位置，从0开始 （= (Page-1)*PageSize）
     * @param limit  返回的限制大小（= 分页大小 PageSize）
     * @return 如果不支持，返回 null
     */
    public String sqlPageList(String sql, int offset, int limit) {
        return null;
    }


    protected String createResultSetMetaSql(TableMapping en) {
        return "SELECT * FROM " + en.getTableName() + " where 1!=1";
    }


    public boolean containKeyword(String name) {
        return keywords.contains(name);
    }

    /**
     * 设置tablemapping信息
     * @param conn
     * @param en
     */
    public void setupEntityField(Connection conn, TableMapping en) {
        Statement stat = null;
        ResultSet rs = null;
        ResultSetMetaData rsmd = null;
        try {
            // 获取数据库元信息
            stat = conn.createStatement();
            rs = stat.executeQuery(createResultSetMetaSql(en));
            rsmd = rs.getMetaData();
            // 循环字段检查
            for (ColumnMapping mf : en.getMetaFields()) {
                if (mf.getColumnDef() == null) {
                    mf.setColumnDef(new ColumnDBType());
                }
                try {
                    int index = DBUtils.getColumnIndex(rsmd, mf.getRawColumnName());
                    if (ResultSetMetaData.columnNoNulls == rsmd.isNullable(index)) {
                        mf.getColumnDef().setNullable(false);
                    } else {
                        mf.getColumnDef().setNullable(true);
                    }
                    mf.setSqlType(rsmd.getColumnType(index));
                } catch (Exception e) {

                }
            }
        } catch (Exception e) {
            LogUtil.debug("Table '" + en.getTableName() + "' doesn't exist!");
        }
        // Close ResultSet and Statement
        finally {
            DBUtils.closeQuietly(rs);
            DBUtils.closeQuietly(stat);
        }
    }

    /**
     * 根据实体信息，返回某实体的建表语句
     * @param en 实体
     * @return 是否创建成功
     */
    public abstract boolean createEntity(Connection conn, TableMapping en);

    public boolean dropEntity(Connection conn, TableMapping en) {
        String tableName = en.getTableName();
        try {
            String sql = "DROP TABLE " + tableName;
            Statement statement = conn.createStatement();
            return statement.execute(sql);
        } catch (Exception e) {
            return false;
        }
    }

    public void createRelation(Connection conn, TableMapping en) {
        List<String> sqls = new ArrayList<>(5);
        for (ColumnMapping lf : en.getMetaFields()) {
            if (lf.getManyToMany() != null) {
                List<String> sqlArr = createRelation(conn, lf);
                sqls.addAll(sqlArr);
            }
        }
        for (String sql : sqls) {
            execute(sql, conn);
        }
    }

    protected List<String> createRelation(Connection conn, ColumnMapping en) {
        List<String> sqls = new ArrayList<>();
        List<String> columns = new ArrayList<>();
        CascadeConfig cc = en.getCascadeConfig();
        if (existsTables(conn, cc.getMiddleTableName()))
            return sqls;
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE " + cc.getMiddleTableName() + "(");
        boolean f = false;
        for (Map.Entry<String, ColumnMapping> entry : cc.getMiddleTableColumns().entrySet()) {
            columns.add(entry.getKey());
            sql.append(f ? ",\n" : "\n").append(entry.getKey() + " " + evalFieldType(entry.getValue()));
            f = true;
        }
        sql.append(")");
        sqls.add(sql.toString());
        sqls.add(getPrimaryKeySql(cc.getMiddleTableName(), columns));
        return sqls;
    }

    public void dropRelation(Connection conn, TableMapping en) {
        List<String> sqls = new ArrayList<>(5);
        for (ColumnMapping lf : en.getMetaFields()) {
            if (lf.getManyToMany() != null) {
                CascadeConfig cc = lf.getCascadeConfig();
                if (!existsTables(conn, cc.getMiddleTableName()))
                    continue;
                String sql = "DROP TABLE " + cc.getMiddleTableName();
                sqls.add(sql);
            }
        }
        for (String sql : sqls) {
            execute(sql, conn);
        }
    }


    /**
     * 该表是否存在
     * @param conn
     * @param tableName
     * @return
     */
    public boolean existsTables(Connection conn, final String tableName) {
        try {
            return DBMetaData.getInstance().existTable(conn, tableName);
        } catch (SQLException e) {
            LogUtil.exception(e);
        }
        return false;
    }

    private static String DEFAULT_COMMENT_TABLE = "comment on table %1$s is '%2$s'";
    private static String DEFAULT_COMMENT_COLUMN = "comment on column %1$s.%2$s is '%3$s'";


    /**
     * 创建索引,一组
     * @param tm
     * @return
     */
    public List<String> createIndexSql(TableMapping tm) {
        List<String> indexSqls = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        Table table = tm.getTable();
        Index[] indexs = table.indexes();
        if (indexs != null) {
            for (Index index : indexs) {
                if (index.unique()) {
                    sb.append("Create UNIQUE Index ");
                } else {
                    sb.append("Create Index ");
                }
                if (index.name().contains("$")) {
                    sb.append(index.name());
                } else {
                    sb.append(index.name());
                }
                sb.append(" ON ").append(tm.getTableName()).append("(");
                sb.append(index.columnList());
                sb.append(")");
                indexSqls.add(sb.toString());
                sb.delete(0, sb.length() - 1);
            }
        }
        return indexSqls;
    }


    public void addComment(Connection con, TableMapping en) {
        List<String> sqls = new ArrayList<String>();

        // 表注释
        if (en.getComment() != null && en.getComment().value() != null) {
            //table,tableComment
            String s = String.format(DEFAULT_COMMENT_TABLE, en.getTableName(), en.getComment().value());
            sqls.add(s);
        }
        for (ColumnMapping mf : en.getMetaFields()) {
            if (mf.getComment() != null && mf.getComment().value() != null) {
                //table,column,columnComment
                String s = String.format(DEFAULT_COMMENT_COLUMN, en.getTableName(), mf.getRawColumnName(), mf.getComment().value());
                sqls.add(s);
            }
        }

        // 执行创建语句
        for (String sql : sqls) {
            execute(sql, con);
        }
    }

    protected String getPrimaryKeySql(String tablename, List<String> pkColumnsEntity) {
        // 没有的加上主键
        Constraint con = new Constraint();
        con.setTableName(tablename);
        con.setName("pk_" + tablename);
        con.setType(ConstraintType.P);
        con.setColumns(pkColumnsEntity);
        DdlGenerator ddl = new DdlGeneratorImpl(this);
        return ddl.addConstraint(con); // 添加新主键约束
    }


    public String evalFieldType(ColumnMapping mf) {
        int length = getColumnLength(mf);
        int precision = getColumnPrecision(mf);
        switch (mf.getSqlType()) {
            case Types.CHAR:
                return "CHAR(" + length + ")";
            case Types.BOOLEAN:
                return "BOOLEAN";

            case Types.VARCHAR:
                return "VARCHAR(" + length + ")";

            case Types.CLOB:
                return "TEXT";

            case Types.BINARY:
            case Types.BLOB:
                return "BLOB";

            case Types.TIMESTAMP:
                return "TIMESTAMP";
            case Types.DATE:
                return "DATE";
            case Types.TIME:
                return "TIME";

            case Types.INTEGER:
                // 用户自定义了宽度
                if (length > 0)
                    return "INT(" + length + ")";
                // 用数据库的默认宽度
                return "INT";

            case Types.FLOAT:
                // 用户自定义了精度
                if (length > 0 && precision > 0) {
                    return "NUMERIC(" + length + "," + precision + ")";
                }
                // 用默认精度
                if (mf.getClz() == Double.class || mf.getClz() == Double.TYPE)
                    return "NUMERIC(15,10)";
                return "FLOAT";

            case Types.ARRAY:
                return "ARRAY";
            default:
                throw new UnsupportedOperationException("Unsupport colType '%s' of field '%s' in '%s' ");
        }
    }

    public static int getColumnLength(ColumnMapping mf) {
        return mf.getColumn() == null ? 255 : mf.getColumn().length();
    }

    public static int getColumnPrecision(ColumnMapping mf) {
        return mf.getColumn() == null ? 0 : mf.getColumn().precision();
    }

    public static int getColumnScale(ColumnMapping mf) {
        return mf.getColumn() == null ? 0 : mf.getColumn().scale();
    }

    public static String getColumnDefinition(ColumnMapping mf) {
        return mf.getColumn() == null ? null : mf.getColumn().columnDefinition();
    }

    protected static String gSQL(String ptn, String table, String field) {
        return MessageFormat.format(ptn, table, field);
    }


    public String createAddColumnSql(TableMapping en, ColumnMapping mf) {
        StringBuilder sb = new StringBuilder("ALTER TABLE ");
        sb.append(en.getTableName()).append(" ADD ");

        sb.append("COLUMN ");
        sb.append(mf.getRawColumnName()).append(" ").append(evalFieldType(mf));

        sb.append(" UNSIGNED");

        sb.append(" NOT NULL");

        sb.append(" NULL DEFAULT NULL");

        if (mf.getComment() != null && mf.getComment().value() != null) {
            sb.append(" COMMENT '").append(mf.getComment().value()).append("'");
        }
        // sb.append(';');
        return sb.toString();
    }

    public List<String> getIndexNames(TableMapping en, Connection conn) throws SQLException {
        List<String> names = new ArrayList<String>();
        String showIndexs = "show index from " + en.getTableName();
        PreparedStatement ppstat = conn.prepareStatement(showIndexs);
        ResultSet rest = ppstat.executeQuery();
        while (rest.next()) {
            String index = rest.getString(3);
            names.add(index);
        }
        return names;
    }

    protected boolean execute(String sql, Connection conn) {
        try {
            OrmLog.commonArrayLog(sql);
            long start = System.currentTimeMillis();
            Statement statement = conn.createStatement();
            boolean b = statement.execute(sql);
            OrmLog.resultLog(start, null);
            return b;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void execute(List<String> sqls, Connection conn) {
        for (String sql : sqls) {
            execute(sql, conn);
        }
    }
}
