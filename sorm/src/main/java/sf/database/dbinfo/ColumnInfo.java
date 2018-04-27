package sf.database.dbinfo;

import sf.database.dialect.DBDialect;
import sf.tools.StringUtils;

public class ColumnInfo {

    public boolean isPrimaryKey; // 是否主键
    /**
     * 顺序
     */
    private int ordinal;
    // 表名
    private String tableName;
    // 列名
    private String columnName;
    // 字段备注
    private String remarks;
    // 数据类型
    private String dataType;
    /**
     * java.sql.Types中的常量之一
     */
    private int sqlType;
    /**
     * 列宽
     */
    private int columnSize;
    /**
     * 十进制数位
     */
    private int decimalDigit;
    private boolean nullable;// 是否允许空值
    // COLUMN_DEF String => 该列的默认值，当值在单引号内时应被解释为一个字符串（可为 null）
    private String columnDef;
    /**
     * 是否unique，数据库支持 unique index和 unique constraint两种方式实现。
     * 当然，在大部分些数据库上其实是同一种实现方式。
     */
    private boolean unique;

    private boolean autoincrement;
    private boolean generatedcolumn;

    public ColumnDBType toColumnType(DBDialect profile) {
        ColumnDBType ct = new ColumnDBType();
        ct.setNullable(nullable);
        ct.setSqlType(sqlType);
        if (StringUtils.isNotEmpty(columnDef)) {
            //这样设置不对
            ct.setDefaultValue(columnDef);
        }
        // System.out.println(this.dataType+" -> "+ ct.toString());
        return ct;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public void setColumnSize(int columnSize) {
        this.columnSize = columnSize;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getDecimalDigit() {
        return decimalDigit;
    }

    public void setDecimalDigit(int decimalDigit) {
        this.decimalDigit = decimalDigit;
    }

    public String getColumnDef() {
        return columnDef;
    }

    public void setColumnDef(String columnDef) {
        this.columnDef = columnDef;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(columnName);
        sb.append("  ").append(this.dataType);
        return sb.toString();
    }

    public int getSqlType() {
        return sqlType;
    }

    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public boolean isAutoincrement() {
        return autoincrement;
    }

    public void setAutoincrement(boolean autoincrement) {
        this.autoincrement = autoincrement;
    }

    public boolean isGeneratedcolumn() {
        return generatedcolumn;
    }

    public void setGeneratedcolumn(boolean generatedcolumn) {
        this.generatedcolumn = generatedcolumn;
    }
}
