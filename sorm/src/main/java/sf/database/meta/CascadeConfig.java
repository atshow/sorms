package sf.database.meta;

import sf.database.OrmContext;

import java.util.Map;

public class CascadeConfig {

    /**
     * 中间表,JoinTable使用
     */
    private String middleTableName;
    /**
     * 中间表对应的字段,JoinTable使用
     */
    private Map<String, ColumnMapping> middleTableColumns;

    /**
     * JoinColumns,JoinColumn使用
     */
    private Map<ColumnMapping, ColumnMapping> fromToColumns;

    /**
     * 主
     */
    private TableMapping fromTable;
    /**
     * 从
     */
    private TableMapping toTable;

    /**
     * 子对象查询语句
     */
    private OrmContext selectSubObject;

    /**
     * 子对象删除
     */
    private OrmContext deleteSubObject;

    /**
     * 插入关系语句,JoinTable使用
     */
    private OrmContext insertRelation;
    /**
     * 删除关系语句,JoinTable使用
     */
    private OrmContext deleteRelation;

    /**
     * 级联类型
     */
    private LinkType type;

    /**
     * 是否使用mappedBy,表示放弃关系维护.
     */
    private boolean useMappedBy;

    public enum LinkType {
        JoinTable, JoinColumns
    }


    public String getMiddleTableName() {
        return middleTableName;
    }

    public void setMiddleTableName(String middleTableName) {
        this.middleTableName = middleTableName;
    }

    public Map<String, ColumnMapping> getMiddleTableColumns() {
        return middleTableColumns;
    }

    public void setMiddleTableColumns(Map<String, ColumnMapping> middleTableColumns) {
        this.middleTableColumns = middleTableColumns;
    }

    public Map<ColumnMapping, ColumnMapping> getFromToColumns() {
        return fromToColumns;
    }

    public void setFromToColumns(Map<ColumnMapping, ColumnMapping> fromToColumns) {
        this.fromToColumns = fromToColumns;
    }

    public TableMapping getFromTable() {
        return fromTable;
    }

    public void setFromTable(TableMapping fromTable) {
        this.fromTable = fromTable;
    }

    public TableMapping getToTable() {
        return toTable;
    }

    public void setToTable(TableMapping toTable) {
        this.toTable = toTable;
    }


    public OrmContext getInsertRelation() {
        return insertRelation;
    }

    public void setInsertRelation(OrmContext insertRelation) {
        this.insertRelation = insertRelation;
    }

    public OrmContext getSelectSubObject() {
        return selectSubObject;
    }

    public void setSelectSubObject(OrmContext selectSubObject) {
        this.selectSubObject = selectSubObject;
    }

    public OrmContext getDeleteRelation() {
        return deleteRelation;
    }

    public void setDeleteRelation(OrmContext deleteRelation) {
        this.deleteRelation = deleteRelation;
    }

    public OrmContext getDeleteSubObject() {
        return deleteSubObject;
    }

    public void setDeleteSubObject(OrmContext deleteSubObject) {
        this.deleteSubObject = deleteSubObject;
    }

    public LinkType getType() {
        return type;
    }

    public void setType(LinkType type) {
        this.type = type;
    }

    public boolean isUseMappedBy() {
        return useMappedBy;
    }

    public void setUseMappedBy(boolean useMappedBy) {
        this.useMappedBy = useMappedBy;
    }
}
