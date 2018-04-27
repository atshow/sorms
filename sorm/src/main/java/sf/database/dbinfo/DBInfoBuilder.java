package sf.database.dbinfo;

import sf.tools.StringUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class DBInfoBuilder {

    protected DataSource dataSource;
    protected Set<String> excludedTables = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

    protected Connection conn = null;
    protected DatabaseMetaData dbMeta = null;

    protected String[] removedTableNamePrefixes = null;

    protected String driverName;
    protected String driverVersion;
    protected int driverMajorVersion;

    public DBInfoBuilder(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource can not be null.");
        }
        this.dataSource = dataSource;
    }

    public void addExcludedTable(String... excludedTables) {
        if (excludedTables != null) {
            for (String table : excludedTables) {
                this.excludedTables.add(table);
            }
        }
    }

    /**
     * 设置需要被移除的表名前缀，仅用于生成 modelName 与 baseModelName 例如表名 "osc_account"，移除前缀
     * "osc_" 后变为 "account"
     */
    public void setRemovedTableNamePrefixes(String... removedTableNamePrefixes) {
        this.removedTableNamePrefixes = removedTableNamePrefixes;
    }

    public List<TableInfo> build() {
        System.out.println("Build TableInfo ...");
        try {
            conn = dataSource.getConnection();
            dbMeta = conn.getMetaData();
            this.driverName = dbMeta.getDriverName();
            this.driverVersion = dbMeta.getDriverVersion();
            this.driverMajorVersion = dbMeta.getDatabaseMajorVersion();

            List<TableInfo> ret = new ArrayList<TableInfo>();
            buildTableNames(ret);
            for (TableInfo TableInfo : ret) {
                buildPrimaryKey(TableInfo);
                buildColumnMetas(TableInfo);
            }
            return ret;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
        }
    }

    /**
     * 通过继承并覆盖此方法，跳过一些不希望处理的 table，定制更加灵活的 table 过滤规则
     * @return 返回 true 时将跳过当前 tableName 的处理
     */
    protected boolean isSkipTable(String tableName) {
        return false;
    }

    /**
     * 不同数据库 dbMeta.getTables(...) 的 schemaPattern 参数意义不同<br>
     * 1：oracle 数据库这个参数代表 dbMeta.getUserName() <br>
     * 2：postgresql 数据库中需要在 jdbcUrl中配置
     * schemaPatter，例如：jdbc:postgresql://localhost:15432/djpt?currentSchema=
     * public,sys,app 最后的参数就是搜索schema的顺序，DruidPlugin 下测试成功 <br>
     * 3：开发者若在其它库中发现工作不正常，可通过继承 MetaBuilder并覆盖此方法来实现功能
     */
    protected ResultSet getTablesResultSet() throws SQLException {
        String schemaPattern = dbMeta.getDriverName().toLowerCase().contains("oracle") ? dbMeta.getUserName() : null;
        return dbMeta.getTables(conn.getCatalog(), schemaPattern, null, new String[]{"TABLE", "VIEW"});
    }

    protected void buildTableNames(List<TableInfo> ret) throws SQLException {
        ResultSet rs = getTablesResultSet();
        while (rs.next()) {
            String tableName = rs.getString("TABLE_NAME");

            if (excludedTables.contains(tableName)) {
                System.out.println("Skip table :" + tableName);
                continue;
            }
            if (isSkipTable(tableName)) {
                System.out.println("Skip table :" + tableName);
                continue;
            }

            TableInfo info = new TableInfo();
            info.setName(tableName);
            info.setRemarks(rs.getString("REMARKS"));
            info.setCatalog(rs.getString("TABLE_CAT"));
            info.setSchema(rs.getString("TABLE_SCHEM"));
            info.setType("TABLE_TYPE");
            ret.add(info);
        }
        rs.close();
    }

    protected void buildPrimaryKey(TableInfo TableInfo) throws SQLException {
        ResultSet rs = dbMeta.getPrimaryKeys(conn.getCatalog(), null, TableInfo.getName());
        String primaryKey = "";
        int index = 0;
        while (rs.next()) {
            if (index++ > 0)
                primaryKey += ",";
            primaryKey += rs.getString("COLUMN_NAME");
        }
        TableInfo.setPrimaryKey(primaryKey);
        rs.close();
    }

    /**
     * 文档参考： http://dev.mysql.com/doc/connector-j/en/connector-j-reference-type-
     * conversions.html
     * <p>
     * JDBC 与时间有关类型转换规则，mysql 类型到 java 类型如下对应关系： DATE java.sql.Date DATETIME
     * java.sql.Timestamp TIMESTAMP[(M)] java.sql.Timestamp TIME java.sql.Time
     * <p>
     * 对数据库的 DATE、DATETIME、TIMESTAMP、TIME 四种类型注入 new
     * java.util.Date()对象保存到库以后可以达到“秒精度” 为了便捷性，getter、setter 方法中对上述四种字段类型采用
     * java.util.Date，可通过定制 TypeMapping 改变此映射规则
     */
    protected void buildColumnMetas(TableInfo tableInfo) throws SQLException {
        String sql = forTableBuilderDoBuild(tableInfo.getName());
        Statement stm = conn.createStatement();
        ResultSet rs = stm.executeQuery(sql);
        ResultSetMetaData rsmd = rs.getMetaData();

        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            ColumnInfo cm = getColumn(tableInfo, rsmd.getColumnName(i));
            tableInfo.getColumnInfos().add(cm);
        }

        rs.close();
        stm.close();
    }

    public String forTableBuilderDoBuild(String tableName) {
        String sql = null;
        if (this.driverName.toLowerCase().contains("mysql")) {
            sql = "select * from `" + tableName + "` where 1 = 2";
        } else {
            sql = "select * from " + tableName + " where 1 = 2";
        }
        return sql;
    }

    /**
     * 返回指定的列的信息，如果没有找到该列返回null
     * @param tableName 表名
     * @param column    列名
     * @return 如果没有找到该列返回null
     * @throws SQLException
     */
    public ColumnInfo getColumn(TableInfo tableInfo, String column) throws SQLException {

        DatabaseMetaData databaseMetaData = conn.getMetaData();

        String schema = tableInfo.getSchema();
        String tableName = tableInfo.getName();
        int n = tableName.indexOf('.');
        if (n > 0) {// 尝试从表名中计算schema
            schema = tableName.substring(0, n);
            tableName = tableName.substring(n + 1);
        }
        ResultSet rs = null;
        try {
            rs = databaseMetaData.getColumns(null, schema, tableName, column);
            ColumnInfo result = null;
            if (rs.next()) {
                result = new ColumnInfo();
                populateColumn(result, rs, tableName);
            }
            return result;
        } finally {
            rs.close();
        }
    }

    private void populateColumn(ColumnInfo column, ResultSet rs, String tableName) throws SQLException {
        /*
         * Notice: Oracle非常变态，当调用rs.getString("COLUMN_DEF")会经常抛出
         * "Stream is already closed" Exception。 百思不得其解，google了半天有人提供了回避这个问题的办法
         * （https://issues.apache.org/jira/browse/DDLUTILS-29），
         * 就是将getString("COLUMN_DEF")作为第一个获取的字段， 非常神奇的就好了。叹息啊。。。
         */
        String defaultVal = rs.getString("COLUMN_DEF");
        column.setColumnDef(StringUtils.trimToNull(defaultVal));// Oracle会在后面加上换行等怪字符。
        column.setColumnName(rs.getString("COLUMN_NAME"));
        column.setOrdinal(rs.getInt("ORDINAL_POSITION"));
        column.setColumnSize(rs.getInt("COLUMN_SIZE"));
        column.setDecimalDigit(rs.getInt("DECIMAL_DIGITS"));
        column.setDataType(rs.getString("TYPE_NAME"));
        column.setSqlType(rs.getInt("DATA_TYPE"));
        column.setNullable(rs.getString("IS_NULLABLE").equalsIgnoreCase("YES"));
        column.setRemarks(rs.getString("REMARKS"));// 这个操作容易出问题，一定要最后操作
        column.setTableName(tableName);
    }

}
