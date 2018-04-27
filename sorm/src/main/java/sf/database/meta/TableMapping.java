package sf.database.meta;

import org.jooq.Record;
import sf.common.CaseInsensitiveMap;
import sf.database.DBCascadeField;
import sf.database.DBField;
import sf.database.annotations.Comment;
import sf.jooq.tables.JooqTable;
import sf.querydsl.SQLRelationalPath;

import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.*;

public class TableMapping implements ITableMapping {
    protected String schema;
    protected String catalog;
    protected String tableName;
    protected String bindDsName;//数据源
    protected List<UniqueConstraint> uniques;
    protected List<Index> indexs;
    protected Table table;
    private Comment comment;// 注释

    protected final List<ColumnMapping> metaFields = new ArrayList<ColumnMapping>();
    /**
     * 不区分大小写,列名和字段对应
     */
    protected final Map<String, ColumnMapping> metaFieldMap = new CaseInsensitiveMap<>();
    protected List<DBField> lobNames;

    protected final Map<DBField, ColumnMapping> schemaMap = new LinkedHashMap<DBField, ColumnMapping>();
    protected final Map<String, DBField> fields = new HashMap<String, DBField>(10, 0.6f);// java类枚举名称到field对应
    protected final Map<String, DBField> lowerFields = new HashMap<String, DBField>(10, 0.6f);// 小写

    /**
     * 级联字段描述
     */
    protected final Map<String, DBCascadeField> cascadeFields = new HashMap<String, DBCascadeField>(10, 0.6f);

    /**
     * 乐观锁 @Version字段的快速索引.
     * @see javax.persistence.Version
     */
    protected final Map<DBField, ColumnMapping> versionMap = new LinkedHashMap<DBField, ColumnMapping>();

    protected boolean cacheable;
    protected boolean useOuterJoin = true;


    /**
     * 记录当前Schema所对应的实体类。
     */
    private Class<?> thisType;

    private List<ColumnMapping> pkFields;// 记录主键列

    private final Map<DBField, String> fieldToColumn = new IdentityHashMap<DBField, String>();// 提供Field到列名的转换
    private final Map<String, String> lowerColumnToFieldName = new HashMap<String, String>();// 提供Column名称到Field的转换，不光包括元模型字段，也包括了非元模型字段但标注了Column的字段(key全部存小写)


    /**
     * 一对一的列
     */
    private List<ColumnMapping> allOne2One;

    /**
     * 对于queryDSL的对象
     */
    private SQLRelationalPath<?> relationalPath;
    private JooqTable<Record> jooqTable;

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

    public List<ColumnMapping> getMetaFields() {
        return metaFields;
    }


    public Map<String, DBField> getFields() {
        return fields;
    }

    public Map<String, DBCascadeField> getCascadeFields() {
        return cascadeFields;
    }


    public Map<String, DBField> getLowerFields() {
        return lowerFields;
    }

    public boolean isCacheable() {
        return cacheable;
    }

    public void setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
    }

    public boolean isUseOuterJoin() {
        return useOuterJoin;
    }

    public void setUseOuterJoin(boolean useOuterJoin) {
        this.useOuterJoin = useOuterJoin;
    }

    public Class<?> getThisType() {
        return thisType;
    }

    public void setThisType(Class<?> thisType) {
        this.thisType = thisType;
    }

    public List<ColumnMapping> getPkFields() {
        return pkFields;
    }

    public void setPkFields(List<ColumnMapping> pkFields) {
        this.pkFields = pkFields;
    }

    public Map<DBField, ColumnMapping> getSchemaMap() {
        return schemaMap;
    }

    public Map<DBField, String> getFieldToColumn() {
        return fieldToColumn;
    }

    public Map<String, String> getLowerColumnToFieldName() {
        return lowerColumnToFieldName;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public List<UniqueConstraint> getUniques() {
        return uniques;
    }

    public void setUniques(List<UniqueConstraint> uniques) {
        this.uniques = uniques;
    }

    public List<Index> getIndexs() {
        return indexs;
    }

    public void setIndexs(List<Index> indexs) {
        this.indexs = indexs;
    }

    public List<DBField> getLobNames() {
        return lobNames;
    }

    public void setLobNames(List<DBField> lobNames) {
        this.lobNames = lobNames;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public Map<String, ColumnMapping> getMetaFieldMap() {
        return metaFieldMap;
    }

    public List<ColumnMapping> getAllOne2One() {
        return allOne2One;
    }

    public void setAllOne2One(List<ColumnMapping> allOne2One) {
        this.allOne2One = allOne2One;
    }

    public Map<DBField, ColumnMapping> getVersionMap() {
        return versionMap;
    }

    public SQLRelationalPath<?> getRelationalPath() {
        return relationalPath;
    }

    public void setRelationalPath(SQLRelationalPath<?> relationalPath) {
        this.relationalPath = relationalPath;
    }

    public JooqTable<Record> getJooqTable() {
        return jooqTable;
    }

    public void setJooqTable(JooqTable<Record> jooqTable) {
        this.jooqTable = jooqTable;
    }

}
