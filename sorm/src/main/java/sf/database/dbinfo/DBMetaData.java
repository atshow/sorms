package sf.database.dbinfo;

import sf.common.CaseInsensitiveMap;
import sf.common.log.LogUtil;
import sf.database.DBField;
import sf.database.DBObject;
import sf.database.dialect.DBDialect;
import sf.database.jdbc.result.ResultSets;
import sf.database.jdbc.sql.Crud;
import sf.database.meta.ColumnMapping;
import sf.database.meta.MetaHolder;
import sf.database.meta.TableMapping;
import sf.database.util.DBUtils;
import sf.dsl.Operator;
import sf.tools.StringUtils;
import sf.tools.utils.Assert;
import sf.tools.utils.SpringStringUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DBMetaData {

    private static DBMetaData instance;

    public static DBMetaData getInstance() {
        if (instance == null) {
            instance = new DBMetaData();
        }
        return instance;
    }

    private DBMetaData() {
    }

    public Map<DBField, ColumnMapping> getColumnMap(TableMapping meta) {
        Map<DBField, ColumnMapping> map = new LinkedHashMap<>(meta.getSchemaMap());
        return map;
    }

    /**
     * 返回指定的列的信息，如果没有找到该列返回null
     * @param tableName 表名
     * @param column    列名
     * @return 如果没有找到该列返回null
     * @throws SQLException
     */
    public ColumnInfo getColumn(Connection conn, String tableName, String column) throws SQLException {
        Collection<Index> indexes = getIndexes(conn, tableName);
        DatabaseMetaData databaseMetaData = conn.getMetaData();
        String schema = DBUtils.getSchema(conn);
        int n = tableName.indexOf('.');
        if (n > 0) {// 尝试从表名中计算schema
            schema = tableName.substring(0, n);
            tableName = tableName.substring(n + 1);
        }
        ResultSet rs = null;
        try {
            rs = databaseMetaData.getColumns(DBUtils.getCatalog(conn), schema, tableName, column);
            ColumnInfo result = null;
            if (rs.next()) {
                result = new ColumnInfo();
                populateColumn(result, rs, tableName, indexes);
            }
            return result;
        } finally {
            DBUtils.closeQuietly(rs);
        }
    }

    /**
     * 得到指定表的所有列
     * @param tableName 表名
     * @return 表中的列。 A collection of columns.
     * @throws SQLException
     * @see ColumnInfo
     */
    public List<ColumnInfo> getColumns(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData databaseMetaData = conn.getMetaData();
        String schema = DBUtils.getSchema(conn);
        int n = tableName.indexOf('.');
        if (n > 0) {// 尝试从表名中计算schema
            schema = tableName.substring(0, n);
            tableName = tableName.substring(n + 1);
        }
        ResultSet rs = null;
        List<ColumnInfo> list = new ArrayList<ColumnInfo>();
        Collection<Index> indexes = null;
        try {
            rs = databaseMetaData.getColumns(DBUtils.getCatalog(conn), schema, tableName, "%");
            while (rs.next()) {
                if (indexes == null) {
                    // 非常渣的Oracle驱动，如果表还不存在时，使用getIndexInfo()会报表不存在的错误（驱动尝试去分析表，不知道为啥要这么做）。
                    // 因此不确定表是否存在时，不去查询索引信息。
                    indexes = getIndexes(conn, tableName);
                }
                ColumnInfo column = new ColumnInfo();
                populateColumn(column, rs, tableName, indexes);
                list.add(column);
            }
        } finally {
            DBUtils.closeQuietly(rs);
        }
        return list;
    }

    private void populateColumn(ColumnInfo column, ResultSet rs, String tableName, Collection<Index> indexes) throws SQLException {
        /*
         * Notice: Oracle非常变态，当调用rs.getString("COLUMN_DEF")会经常抛出
         * "Stream is already closed" Exception。 百思不得其解，google了半天有人提供了回避这个问题的办法
         * （https://issues.apache.org/jira/browse/DDLUTILS-29），
         * 就是将getString("COLUMN_DEF")作为第一个获取的字段， 非常神奇的就好了。叹息啊。。。
         */
        String defaultVal = rs.getString("COLUMN_DEF");
        column.setColumnDef(SpringStringUtils.trimWhitespace(defaultVal));// Oracle会在后面加上换行等怪字符。
        column.setColumnName(rs.getString("COLUMN_NAME"));
        column.setOrdinal(rs.getInt("ORDINAL_POSITION"));
        column.setColumnSize(rs.getInt("COLUMN_SIZE"));
        column.setDecimalDigit(rs.getInt("DECIMAL_DIGITS"));
        column.setDataType(rs.getString("TYPE_NAME"));
        column.setSqlType(rs.getInt("DATA_TYPE"));
        column.setNullable(rs.getString("IS_NULLABLE").equalsIgnoreCase("YES"));
        column.setAutoincrement(rs.getString("IS_AUTOINCREMENT").equalsIgnoreCase("YES"));
        column.setGeneratedcolumn(rs.getString("IS_GENERATEDCOLUMN").equalsIgnoreCase("YES"));

        column.setRemarks(rs.getString("REMARKS"));// 这个操作容易出问题，一定要最后操作
        column.setTableName(tableName);

        if (indexes != null) {
            // 根据索引，计算该列是否为unique
            for (Index index : indexes) {
                if (index.isUnique() && index.isOnSingleColumn(column.getColumnName())) {
                    column.setUnique(true);
                    break;
                }
            }
        }
    }


    /**
     * 得到指定表的所有索引
     * @param tableName 表名
     * @return 索引信息
     * @see Index
     */
    public Collection<Index> getIndexes(Connection conn, String tableName) throws SQLException {
        // JDBC驱动不支持的情况

        String schema = DBUtils.getSchema(conn);
        int n = tableName.indexOf('.');
        if (n > -1) {
            schema = tableName.substring(0, n);
            tableName = tableName.substring(n + 1);
        }
        ResultSet rs = null;
        try {
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            rs = databaseMetaData.getIndexInfo(null, schema, tableName, false, false);
            Map<String, Index> map = new HashMap<String, Index>();
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                String cName = rs.getString("COLUMN_NAME");
                if (indexName == null || cName == null)
                    continue;
                Index index = map.get(indexName);
                if (index == null) {
                    index = new Index();
                    index.setIndexName(indexName);
                    index.setTableName(rs.getString("TABLE_NAME"));
                    index.setTableSchema(rs.getString("TABLE_SCHEM"));
                    index.setIndexQualifier(rs.getString("INDEX_QUALIFIER"));
                    index.setUnique(!rs.getBoolean("NON_UNIQUE"));
                    index.setType(rs.getInt("TYPE"));
                    map.put(indexName, index);
                }

                String asc = rs.getString("ASC_OR_DESC");
                Boolean isAsc = (asc == null ? true : asc.startsWith("A"));
                int order = rs.getInt("ORDINAL_POSITION");
                index.addColumn(cName, isAsc, order);
            }
            return map.values();
        } finally {
            DBUtils.closeQuietly(rs);
        }
    }

    /**
     * @param type      要查询的对象类型 "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY",
     *                  "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     * @param schema    Schema
     * @param matchName 匹配名称
     * @param oper      操作符，可以为null，为null时表示等于条件
     * @return 表/视图等数据库对象的信息
     * @throws SQLException
     * @see Operator
     */
    public List<TableInfo> getDatabaseObject(Connection conn, ObjectType type, String catalog, String schema, String matchName, Operator oper) throws SQLException {
        if (schema == null) {
            schema = DBUtils.getSchema(conn);
        }
        if (catalog == null) {
            catalog = DBUtils.getCatalog(conn);
        }
        if (matchName != null) {
            int n = matchName.indexOf('.');
            if (n > -1) {
                schema = matchName.substring(0, n);
                matchName = matchName.substring(n + 1);
            }

        }
        if (oper != null && oper != Operator.EQUALS) {
            if (StringUtils.isEmpty(matchName)) {
                matchName = "%";
            } else if (oper == Operator.MATCH_ANY) {
                matchName = "%" + matchName + "%";
            } else if (oper == Operator.MATCH_END) {
                matchName = "%" + matchName;
            } else if (oper == Operator.MATCH_START) {
                matchName = matchName + "%";
            }
        }
        DatabaseMetaData databaseMetaData = conn.getMetaData();
        ResultSet rs = null;
        try {
            rs = databaseMetaData.getTables(catalog, schema, matchName, type == null ? null : new String[]{type.name()});
            List<TableInfo> result = new ArrayList<TableInfo>();
            while (rs.next()) {
                TableInfo info = new TableInfo();
                info.setCatalog(rs.getString("TABLE_CAT"));
                info.setSchema(rs.getString("TABLE_SCHEM"));
                info.setName(rs.getString("TABLE_NAME"));
                info.setType(rs.getString("TABLE_TYPE"));// "TABLE","VIEW",
                // "SYSTEM TABLE",
                // "GLOBAL TEMPORARY","LOCAL TEMPORARY",
                // "ALIAS",
                // "SYNONYM".
                info.setRemarks(rs.getString("REMARKS"));
                result.add(info);
            }
            rs.close();
            for (TableInfo info : result) {
                rs = databaseMetaData.getPrimaryKeys(catalog, schema, info.getName());
                String primaryKey = "";
                boolean f = false;
                while (rs.next()) {
                    primaryKey += (f ? "," : "") + rs.getString("COLUMN_NAME");
                    f = true;
                }
                info.setPrimaryKey(primaryKey);
                rs.close();
            }
            return result;
        } finally {
            DBUtils.closeQuietly(rs);
        }
    }

    /**
     * 返回当前schema下的所有数据库表名
     * @param types 取以下参数{@link ObjectType}。可以省略，省略的情况下取Table
     * @return 所有表名
     * @throws SQLException
     */
    public List<String> getTableNames(Connection conn, String catalog, String schema, ObjectType... types) throws SQLException {
        if (types == null || types.length == 0) {
            types = new ObjectType[]{ObjectType.TABLE};
        }
        DatabaseMetaData databaseMetaData = conn.getMetaData();
        String[] ts = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            ts[i] = types[i].name();
        }
        ResultSet rs = databaseMetaData.getTables(catalog, schema, null, ts);
        try {
            List<String> result = new ArrayList<String>();
            while (rs.next()) {
                result.add(rs.getString("TABLE_NAME"));
            }
            return result;
        } finally {
            DBUtils.closeQuietly(rs);
        }
    }


    public boolean existTable(Connection conn, String tableName) throws SQLException {
        return getExistTable(conn, tableName) != null;
    }

    /**
     * 判断一张表是否存在
     * @param tableName 表名
     * @return 表存在返回表名，表不存在返回null
     * @throws SQLException
     */
    public String getExistTable(Connection conn, String tableName) throws SQLException {
        return getExists(conn, ObjectType.TABLE, tableName);
    }

    /**
     * 判断对象是否存在
     * @param type       要查找的对象{@linkplain ObjectType 类型}
     * @param objectName 对象名称
     * @return true如果对象存在返回对象名称。否则返回null
     * @throws SQLException
     * @see ObjectType
     */
    public String getExists(Connection conn, ObjectType type, String objectName) throws SQLException {
        String schema = null;
        int n = objectName.indexOf('.');
        if (n > -1) {
            schema = objectName.substring(0, n);
            objectName = objectName.substring(n + 1);
        }
        return innerExists(conn, type, schema, objectName) ? objectName : null;


    }

    private boolean innerExists(Connection conn, ObjectType type, String schema, String objectName) throws SQLException {
        if (schema == null)
            schema = DBUtils.getSchema(conn);// 如果当前schema计算不正确，会出错
        switch (type) {
            case FUNCTION:
                return existsFunction(conn, schema, objectName);
            case PROCEDURE:
                return existsProcdure(conn, schema, objectName);
            case SEQUENCE:
                List<SequenceInfo> seqs = Collections.emptyList();
                if (seqs != null) {
                    return !seqs.isEmpty();
                }
            default:
                break;
        }
        String catalog = DBUtils.getCatalog(conn);
        DatabaseMetaData databaseMetaData = conn.getMetaData();
        ResultSet rs = databaseMetaData.getTables(catalog, schema, objectName, new String[]{type.name()});

        try {
            return rs.next();
        } finally {
            DBUtils.closeQuietly(rs);
        }
    }

    /**
     * 获取当前连接的catalog
     * @param conn
     * @return
     * @throws SQLException
     */
    public String getCatalog(Connection conn) throws SQLException {
        return DBUtils.getCatalog(conn);
    }

    /**
     * 获取数据库中所有的catalog
     * @return 所有catalog
     * @throws SQLException
     */
    public String[] getCatalogs(Connection conn) throws SQLException {
        DatabaseMetaData databaseMetaData = conn.getMetaData();
        ResultSet rs = databaseMetaData.getCatalogs();
        try {
            List<String> list = ResultSets.toStringList(rs, "TABLE_CAT", 9999);
            return list.toArray(new String[list.size()]);
        } finally {
            DBUtils.closeQuietly(rs);
        }
    }

    /**
     * 获得所有的Schema
     * @return 所有的Schema
     * @throws SQLException
     */
    public String[] getSchemas(Connection conn) throws SQLException {
        DatabaseMetaData databaseMetaData = conn.getMetaData();
        ResultSet rs = databaseMetaData.getSchemas();
        try {
            List<String> list = ResultSets.toStringList(rs, "TABLE_SCHEM", 9999);
            return list.toArray(new String[list.size()]);
        } finally {
            DBUtils.closeQuietly(rs);
        }
    }

    /**
     * @return the JDBC 'DatabaseMetaData' object
     * @throws SQLException
     * @deprecated not recommended
     */
    public DatabaseMetaData get(Connection conn) throws SQLException {
        return conn.getMetaData();
    }

    /**
     * 检查是否存在指定的存储过程
     * @param schema     所在schema
     * @param objectName 存储过程名
     * @return 存储过程存在返回true，否则false
     * @throws SQLException
     */
    public boolean existsProcdure(Connection conn, String schema, String objectName) throws SQLException {
        List<Function> func = this.innerGetProcedures(conn, schema, objectName);
        return !func.isEmpty();
    }

    /**
     * 返回指定名称的函数是否存在(需要支持JDBC 4.0的驱动才可执行)
     * @param schema 所在schema
     * @param name   函数名
     * @return 函数存在返回true, 否则false
     * @throws SQLException 检测用户函数功能在 JDBC 4.0 (JDK 6)中定义，很多旧版本驱动都不支持，会抛出此异常
     * @author Jiyi
     * @since 1.7.1
     */
    public boolean existsFunction(Connection conn, String schema, String name) throws SQLException {
        List<Function> func = innerGetFunctions(conn, schema, name);
        return !func.isEmpty();
    }

    /**
     * 得到数据库中的当前用户的存储过程
     * @param schema 数据库schema，传入null表示当前schema
     * @return 存储过程
     */
    public List<Function> getProcedures(Connection conn, String schema) throws SQLException {
        return innerGetProcedures(conn, schema, null);
    }

    /**
     * 返回所有自定义数据库函数
     * @param schema 数据库schema，传入null表示当前schema
     * @return 自定义函数
     * @throws SQLException
     */
    public List<Function> getFunctions(Connection conn, String schema) throws SQLException {
        return innerGetFunctions(conn, schema, null);
    }

    private List<Function> innerGetFunctions(Connection conn, String schema, String name) throws SQLException {
        if (schema == null) {
            schema = DBUtils.getSchema(conn);
        }
        List<Function> result = new ArrayList<Function>();
        DatabaseMetaData databaseMetaData = conn.getMetaData();
        String catalog = DBUtils.getCatalog(conn);
        ResultSet rs = null;
        try {
            rs = databaseMetaData.getFunctions(catalog, schema, name);
            while (rs.next()) {
                Function function = new Function();
                function.setCatalog(rs.getString(1));
                function.setSchema(rs.getString(2));
                function.setName(rs.getString(3));
                function.setRemarks(rs.getString(4));
                function.setType(rs.getShort(5));
                function.setSpecificName(rs.getString(6));
                result.add(function);
            }
        } catch (java.sql.SQLFeatureNotSupportedException e) {
            LogUtil.warn(databaseMetaData.getDriverName() + " doesn't supprt getFunctions() defined in JDDBC 4.0.");
        } catch (AbstractMethodError e) { // Driver version is too old...
            StringBuilder sb = new StringBuilder("The driver ").append(databaseMetaData.getDriverName());
            sb.append(' ').append(databaseMetaData.getDriverVersion()).append(' ').append(databaseMetaData.getDatabaseMinorVersion());
            sb.append(" not implements JDBC 4.0, please upgrade you JDBC Driver.");
            throw new SQLException(sb.toString());
        } finally {
            DBUtils.closeQuietly(rs);
        }
        return result;
    }

    private List<Function> innerGetProcedures(Connection conn, String schema, String procdureName) throws SQLException {
        if (schema == null) {
            schema = DBUtils.getSchema(conn);
        }
        DatabaseMetaData databaseMetaData = conn.getMetaData();
        String catalog = DBUtils.getCatalog(conn);
        ResultSet rs = null;
        try {
            List<Function> result = new ArrayList<Function>();
            rs = databaseMetaData.getProcedures(catalog, schema, procdureName);
            while (rs.next()) {
                Function function = new Function(ObjectType.PROCEDURE);
                function.setCatalog(rs.getString(1));
                function.setSchema(rs.getString(2));
                function.setName(rs.getString(3));
                function.setRemarks(rs.getString(7));
                function.setType(rs.getShort(8));
                function.setSpecificName(rs.getString(9));
                result.add(function);
            }
            return result;
        } finally {
            DBUtils.closeQuietly(rs);
        }
    }

    /**
     * 得到内建其他函数名
     * @return 其他函数名（逗号分隔）
     * @throws SQLException
     */
    public String getSystemFunctions(Connection conn) throws SQLException {
        DatabaseMetaData databaseMetaData = conn.getMetaData();
        String result = databaseMetaData.getSystemFunctions();
        return result;
    }

    /**
     * 得到由JDBC驱动提供的所有数据库关键字
     * @return 关键字列表
     * @throws SQLException
     */
    public String[] getSQLKeywords(Connection conn) throws SQLException {
        DatabaseMetaData databaseMetaData = conn.getMetaData();
        return SpringStringUtils.split(databaseMetaData.getSQLKeywords(), ",");
    }

    /**
     * 判断，是否支持指定的事务隔离级别
     * @param level
     * @return
     * @throws SQLException
     */
    public boolean supportsTransactionIsolationLevel(Connection conn, int level) throws SQLException {
        return conn.getMetaData().supportsTransactionIsolationLevel(level);
    }


    /**
     * 检查并修改数据库中的表，使其和传入的实体模型保持一致。
     * @param clz 要更新的表对应的类
     * @throws SQLException
     */
    public static void refreshTable(Connection conn, Class<?> clz) throws SQLException {
        TableMapping tm = MetaHolder.getMeta(clz);
        refreshTable(conn, tm, tm.getTableName(), true, true);
    }

    /**
     * 检查并修改数据库中的表，使其和传入的实体模型保持一致。
     * @param meta  要更新的表的元数据
     * @param event 事件监听器，可以监听刷新过程的事件
     * @throws SQLException
     */
    public static void refreshTable(Connection conn, TableMapping meta) throws SQLException {
        refreshTable(conn, meta, true, true);
    }

    /**
     * 检查并修改数据库中的表，使其和传入的实体模型保持一致。
     * @param meta             要更新的表的元数据
     * @param event            事件监听器，可以监听刷新过程的事件
     * @param modifyConstraint 更改约束
     * @param modifyIndexes    更改索引
     * @throws SQLException
     * @see MetadataEventListener
     */
    public static void refreshTable(Connection conn, TableMapping meta, boolean modifyConstraint, boolean modifyIndexes) throws SQLException {
        Assert.notNull(meta, "The table definition which your want to resresh must not null.");
        refreshTable(conn, meta, meta.getTableName(), modifyConstraint, modifyIndexes);
    }

    /**
     * 更新表。此操作核对输入的元模型和数据库中表的差异，并且通过create table或alter table等语句尽可能将其修改得和元模型一直。
     *
     * <h3>注意</h3> 由于ALTER TABLE有很多限制，因此这个方法执行有很多可能会抛出错误。
     * <p>
     * 此外，由于ALTER TABLE语句是DDL，因此多个DDL执行中出现错误时，已经执行过的语句将不会被回滚。所以请尽可能通过
     * {@linkplain MetadataEventListener 监听器} 的监听事件来把握表变更的进度情况。
     * @param meta      元模型
     * @param tablename 表名
     * @param event     事件监听器，可以捕捉表对比、SQL语句执行前后等事件
     * @throws SQLException 修改表失败时抛出
     * @see MetadataEventListener 变更监听器
     */
    public static void refreshTable(Connection conn, TableMapping meta, String tablename, boolean modifyConstraint, boolean modifyIndex)
            throws SQLException {
        // 列的修改
        getInstance().modifyColumns(conn, tablename, meta);
        // 约束的修改
        if (modifyConstraint) {
            getInstance().executeDDL(conn, getInstance().calculateConstraints(conn, meta, tablename), tablename, meta);
        }
        // 索引的修改
        if (modifyIndex) {
            getInstance().executeDDL(conn, getInstance().calculateIndexes(conn, meta, tablename), tablename, meta);
        }
    }

    private void modifyColumns(Connection conn, String tablename, TableMapping meta) throws SQLException {
        DBDialect dialect = DBUtils.doGetDialect(conn, false);
        boolean supportsChangeDelete = dialect.notHas(Feature.NOT_SUPPORT_ALTER_DROP_COLUMN);
        ;
        DdlGenerator ddlGenerator = new DdlGeneratorImpl(dialect);
        if (!supportsChangeDelete) {
            LogUtil.warn("Current database [{}] doesn't support alter table column.", dialect.getName());
        }

        List<ColumnInfo> columns = DBMetaData.getInstance().getColumns(conn, tablename);
        if (columns.isEmpty()) {// 表不存在
            boolean created = false;
            created = createTable(conn, meta, tablename);
            return;
        }
        // 新增列
        Map<DBField, ColumnMapping> defined = DBMetaData.getInstance().getColumnMap(meta);

        // 在对比之前判断

        // 删除的列
        List<String> delete = new ArrayList<String>();
        // 更新的列(暂时不要调用)
        List<ColumnMapping> changed = new ArrayList<>();

        //数据库字段名,java字段名称
        CaseInsensitiveMap<DBField> map = new CaseInsensitiveMap<>();
        for (ColumnMapping cm : meta.getMetaFields()) {
            if (cm.getField() != null && SpringStringUtils.hasText(cm.getLowerColumnName())) {
                map.put(cm.getLowerColumnName(), cm.getField());
            }
        }

        // 比较差异
        for (ColumnInfo c : columns) {
            DBField field = DBUtils.getColumnByDBName(map, c.getColumnName());
            if (field == null) {
                if (supportsChangeDelete) {
                    delete.add(c.getColumnName());
                }
                continue;
            }
            ColumnMapping type = defined.remove(field);// from the metadata
            // find
            // the column defined
            Assert.notNull(type);// 不应该发生
            //change方法实现有误,默认值无法比较
           /* if (supportsChangeDelete) {
                List<ColumnChange> changes = ColumnDBType.isEqualTo(type,c,dialect);
                if (!changes.isEmpty()) {
                    changed.add(type);
                }
            }*/
        }
        Set<ColumnMapping> insert = new HashSet<>();
        for (Map.Entry<DBField, ColumnMapping> e : defined.entrySet()) {
            String columnName = e.getValue().getRawColumnName();
            insert.add(e.getValue());
        }
        // 比较完成后，只剩下三类变更的列数据
        executeDDL(conn, ddlGenerator.toTableModifyClause(meta, tablename, insert, changed, delete), tablename, meta);
    }

    private void executeDDL(Connection conn, List<String> alterTableSQLs, String tablename, TableMapping meta) throws SQLException {
        try {
            int n = 0;
            for (String sql : alterTableSQLs) {
                long start = System.currentTimeMillis();
                boolean success = true;
                try {
                    Crud.getInstance().getCrudSql().execute(conn, sql);
                } catch (SQLException e) {
                    success = false;
                    throw e;
                }
                if (success) {
                    long cost = System.currentTimeMillis() - start;
                }
                n++;
            }
        } finally {

        }
    }

    /**
     * 清除表中的所有数据。truncate是DDL不能回滚。
     * @param meta      要清除的表的元数据
     * @param tablename 表名
     * @throws SQLException
     */
    public void truncate(Connection conn, TableMapping meta, List<String> tablename) throws SQLException {
        DBDialect d = DBUtils.doGetDialect(conn, false);
        try {
            if (d.has(Feature.NOT_SUPPORT_TRUNCATE)) {
                for (String table : tablename) {
                    Crud.getInstance().getCrudSql().execute(conn, "delete from " + table);
                }
            } else {
                for (String table : tablename) {
                    Crud.getInstance().getCrudSql().execute(conn, "truncate table " + table);
                }
            }
        } finally {
        }
    }

    /**
     * 计算约束
     * @param conn
     * @param meta
     * @param tablename
     * @return
     * @throws SQLException
     */
    private List<String> calculateConstraints(Connection conn, TableMapping meta, String tablename) throws SQLException {
        List<String> sqls = new ArrayList<String>();
        // 该张表上全部的约束
        return sqls;
    }

    /**
     * 计算索引
     * @param conn
     * @param meta
     * @param tablename
     * @return
     * @throws SQLException
     */
    private List<String> calculateIndexes(Connection conn, TableMapping meta, String tablename) throws SQLException {
        List<String> sqls = new ArrayList<String>();
        return sqls;
    }

    /**
     * 创建表
     * @param clz 建表的CLass
     * @return true建表成功，false表已存在
     * @throws SQLException
     */
    public static <T extends DBObject> boolean createTable(Connection conn, Class<T> clz) throws SQLException {
        TableMapping meta = MetaHolder.getMeta(clz);
        return createTable(conn, meta, meta.getTableName());
    }

    /**
     * 创建表
     * @param meta 表结构描述。 The metadata of the table.
     * @return Ture if the table created successful, or vv.
     * @throws SQLException
     */
    public static boolean createTable(Connection conn, TableMapping meta) throws SQLException {
        return createTable(conn, meta, null);
    }

    /**
     * 创建表
     * @param meta      表结构描述。 The metadata of the table.
     * @param tablename 表名。 The name of the table.
     * @return 如果表被创建返回true，如果表已存在返回false。<br>
     * Ture if the table created successful, or vv.
     * @throws SQLException
     * @see {@link TableMapping}
     */
    public static boolean createTable(Connection conn, TableMapping meta, String tablename) throws SQLException {
        if (tablename == null) {
            tablename = meta.getTableName();
        }
        boolean created = false;
        if (!DBMetaData.getInstance().existTable(conn, tablename)) {
            DBDialect dialect = DBUtils.doGetDialect(conn, false);
            try {
                // 建表
                dialect.createEntity(conn, meta);
                // create sequence

                // 创建外键约束等
                // TODO
                // exe.executeSql(sqls.getOtherContraints());
                // create indexes
            } finally {
            }
            created = true;
        }
        // 额外创建表
        return created;
    }
}
