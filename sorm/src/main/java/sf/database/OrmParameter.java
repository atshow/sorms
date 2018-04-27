package sf.database;

import sf.database.meta.ColumnMapping;
import sf.database.meta.TableMapping;

/**
 * orm 参数描述，包含值，对应的名称
 */
public class OrmParameter {
    /**
     * 值
     */
    private Object value;
    /**
     * 字段(可以代表参数名称,在命名查询中有用)
     */
    private DBField field;

    private ColumnMapping columnMapping;

    private TableMapping tableMapping;

    public OrmParameter() {

    }

    public OrmParameter(Object value, ColumnMapping columnMapping) {
        this.value = value;
        this.columnMapping = columnMapping;
        this.field = columnMapping.getField();
    }

    public String toString() {
        if (value != null) {
            return value.toString();
        } else {
            return "";
        }
    }

    public Object getValue() {
        return value;
    }

    public OrmParameter setValue(Object value) {
        this.value = value;
        return this;
    }

    public DBField getField() {
        return field;
    }

    public ColumnMapping getColumnMapping() {
        return columnMapping;
    }

    public OrmParameter setColumnMapping(ColumnMapping columnMapping) {
        this.field = columnMapping.getField();
        this.columnMapping = columnMapping;
        return this;
    }

    public TableMapping getTableMapping() {
        return tableMapping;
    }

    public void setTableMapping(TableMapping tableMapping) {
        this.tableMapping = tableMapping;
    }
}
