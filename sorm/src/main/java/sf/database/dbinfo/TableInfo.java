package sf.database.dbinfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 一张数据库表或视图。<br/>
 * Indicates a table/view in database.
 * @author
 */
public class TableInfo {
    /**
     * TABLE_CAT String => 表类别（可为 null）
     */
    private String catalog;
    /**
     * TABLE_SCHEM String => 表模式（可为 null）
     */
    private String schema;
    /**
     * TABLE_NAME String => 表名称
     */
    private String name;
    /**
     * REMARKS String => 表的解释性注释
     */
    private String remarks;
    /**
     * TABLE_TYPE String => 表类型。典型的类型是 "TABLE"、"VIEW"、"SYSTEM TABLE"、
     * "GLOBAL TEMPORARY"、"LOCAL TEMPORARY"、"ALIAS" 和 "SYNONYM"。
     */
    private String type;
    /**
     * TYPE_CAT String => 类型的类别（可为 null）
     */
    private String typeCatelog;
    /**
     * TYPE_SCHEM String => 类型模式（可为 null）
     */
    private String typeSchema;
    /**
     * TYPE_NAME String => 类型名称（可为 null）
     */
    private String typeName;
    /**
     * SELF_REFERENCING_COL_NAME String => 有类型表的指定 "identifier" 列的名称（可为 null）
     */
    private String selfRefencingColName;//
    /**
     * REF_GENERATION String => 指定在 SELF_REFERENCING_COL_NAME 中创建值的方式。这些值为
     * "SYSTEM"、"USER" 和 "DERIVED"。（可能为 null）
     */
    private String refGeneration;

    private List<ColumnInfo> columnInfos = new ArrayList<ColumnInfo>(); // 字段
    // meta

    private String primaryKey;

    /**
     * 数据库表所属catalog
     * @return catalog
     */
    public String getCatalog() {
        return catalog;
    }

    /**
     * 设置Catalog
     * @param catalog
     */
    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    /**
     * 获得表所在schema
     * @return the schema of table
     */
    public String getSchema() {
        return schema;
    }

    /**
     * 设置 schema
     * @param schema
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * 获得表/视图(等)的名称
     * @return 名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置名称
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获得表的备注信息
     * @return 备注
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * 设置备注
     * @param remarks 备注
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * 获得表的类型
     * @return 类型
     */
    public String getType() {
        return type;
    }

    /**
     * 设置表类型
     * @param type 类型
     */
    public void setType(String type) {
        this.type = type;
    }

    public String getTypeCatelog() {
        return typeCatelog;
    }

    public void setTypeCatelog(String typeCatelog) {
        this.typeCatelog = typeCatelog;
    }

    public String getTypeSchema() {
        return typeSchema;
    }

    public void setTypeSchema(String typeSchema) {
        this.typeSchema = typeSchema;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getSelfRefencingColName() {
        return selfRefencingColName;
    }

    public void setSelfRefencingColName(String selfRefencingColName) {
        this.selfRefencingColName = selfRefencingColName;
    }

    public String getRefGeneration() {
        return refGeneration;
    }

    public void setRefGeneration(String refGeneration) {
        this.refGeneration = refGeneration;
    }

    public List<ColumnInfo> getColumnInfos() {
        return columnInfos;
    }

    public void setColumnInfos(List<ColumnInfo> columnInfos) {
        this.columnInfos = columnInfos;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }
}