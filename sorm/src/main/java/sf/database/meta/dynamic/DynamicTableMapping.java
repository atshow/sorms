package sf.database.meta.dynamic;

import sf.common.CaseInsensitiveMap;
import sf.database.VarObject;
import sf.tools.utils.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicTableMapping {

    protected String catalog;
    /**
     * schema of the table. (it is always the username in Oracle)
     */
    protected String schema;
    /**
     * name of the table.
     */
    protected String tableName;
    /**
     * Always operate the table in the named datasource.
     */
    protected String bindDsName;

    private Class<?> type = VarObject.class;

    /**
     * 数据库列名和字段对应.
     */
    protected final Map<String, DynamicColumnMapping> columnMap = new CaseInsensitiveMap();


    /**
     * 字段名和字段定义对应.
     */
    protected final Map<String, DynamicColumnMapping> fieldToColumn = new HashMap<>();

    protected final List<DynamicColumnMapping> pkFields = new ArrayList<DynamicColumnMapping>();

    protected final List<DynamicColumnMapping> orderdColumns = new ArrayList<DynamicColumnMapping>();


    /**
     * 定义一个列。
     * <p>
     * @param column 列数据库列
     */
    public void addColumn(DynamicColumnMapping column) {
        Assert.notNull(column, "列不能为空");
        Assert.notNull(column.getClz(), "类型不能为空");
        Assert.notNull(column.getJdbcType(), " 数据类型不能为空");
        Assert.notNull(column.getField(), "java字段不能为空");
        Assert.notNull(column.getName(), "数据库字段不能为空");
        columnMap.put(column.getName(), column);
        fieldToColumn.put(column.getField(), column);
        if (column.isPk()) {
            pkFields.add(column);
        }
    }

    public void addColumn(DynamicColumnMapping column, boolean isPk) {
        column.setPk(isPk);
        addColumn(column);
    }


    /**
     * 删除指定的列
     * @param fieldName 当列名\字段名 不同时，这个方法按照字段名删除
     * @return false如果没找到此列
     */
    public boolean removeColumnByFieldName(String fieldName) {
        DynamicColumnMapping column = fieldToColumn.remove(fieldName);
        if (column == null)
            return false;
        columnMap.remove(column.getName());
        return true;
    }

    /**
     * 按数据库字段名称删除指定的列
     * @param name
     * @return false如果没找到此列
     */
    public boolean removeColumnByName(String name) {
        DynamicColumnMapping column = columnMap.remove(name);
        if (column == null)
            return false;
        fieldToColumn.remove(column.getField());
        return true;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getBindDsName() {
        return bindDsName;
    }

    public void setBindDsName(String bindDsName) {
        this.bindDsName = bindDsName;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public Map<String, DynamicColumnMapping> getColumnMap() {
        return columnMap;
    }

    public Map<String, DynamicColumnMapping> getFieldToColumn() {
        return fieldToColumn;
    }

    public List<DynamicColumnMapping> getPkFields() {
        return pkFields;
    }

    public List<DynamicColumnMapping> getOrderdColumns() {
        return orderdColumns;
    }
}
