package sf.database.meta;

import sf.database.DBCascadeField;
import sf.database.DBField;
import sf.database.annotations.Comment;
import sf.database.annotations.FetchDBField;
import sf.database.annotations.Type;
import sf.database.annotations.UniqueKeyGenerator;
import sf.database.dbinfo.ColumnDBType;
import sf.database.jdbc.sql.IdentifierGenerator;
import sf.database.jdbc.type.TypeHandler;
import sf.tools.reflect.PropertyHold;

import javax.persistence.*;

/**
 * 对应的java实体类的字段
 * @author shixiafeng
 */
public class ColumnMapping implements IColumnMapping {
    /**
     * 原始的ColumnName(对应数据库)
     */
    public String rawColumnName;
    protected transient String cachedEscapeColumnName;
    private transient String lowerColumnName;// 数据库列名，小写，不转义
    private transient String upperColumnName;// 数据库列名，大写，不转义

    /**
     *
     */
    private TypeHandler<Object> handler;
    // 对应java类
    protected TableMapping meta;
    /**
     * java字段名
     */
    private String fieldName;
    protected DBField field;// field对象
    protected DBCascadeField cascadeField;//级联对象,描述类中的级联关系(一对一,一对多,多对多,多对一)
    protected ColumnDBType columnDef;// 数据库定义
    /**
     * 得到java类型
     */
    protected Class<?> clz;
    private boolean pk;// 是否为主键
    protected PropertyHold fieldAccessor;// 获得字段访问器

    /**
     * 得到默认未设置或修饰过的值
     */
    private Object unsavedValue;

    private int sqlType;// 对应java.sql.Types的值

    private boolean unsavedValueDeclared;
    private boolean notInsert;
    private boolean notUpdate;

    private Column column;
    private GeneratedValue gv;
    private TableGenerator tableGenerator;
    private SequenceGenerator sequenceGenerator;
    private UniqueKeyGenerator uniqueKeyGenerator;//自定义主键生成策略
    private IdentifierGenerator identifierGenerator;//和上面的主键策略合用.

    //是否有Version标记,主要是乐观锁功能.对应@see javax.persistence.Version
    private boolean version;
    private boolean lob;// 是否是lob类型
    private Temporal temporal;// 日期处理
    private Enumerated enumerated;// 枚举处理
    private Comment comment;// 注释
    private OrderBy orderBy;
    private Transient jpaTransient;
    private boolean transient1;// 是否是transient

    //自定义注解
    private Type type;
    private FetchDBField fetchDBField;

    // 表间关系处理
    private OneToMany oneToMany;
    private ManyToMany manyToMany;
    private OneToOne oneToOne;
    private ManyToOne manyToOne;

    private JoinColumn joinColumn;
    private JoinColumns joinColumns;
    private JoinTable joinTable;

    /**
     * 级联关系配置,只有处于级联关系下才有值
     */
    private CascadeConfig cascadeConfig;

    public String getRawColumnName() {
        return rawColumnName;
    }

    public void setRawColumnName(String rawColumnName) {
        this.rawColumnName = rawColumnName;
    }

    public String getCachedEscapeColumnName() {
        return cachedEscapeColumnName;
    }

    public void setCachedEscapeColumnName(String cachedEscapeColumnName) {
        this.cachedEscapeColumnName = cachedEscapeColumnName;
    }

    public String getLowerColumnName() {
        return lowerColumnName;
    }

    public void setLowerColumnName(String lowerColumnName) {
        this.lowerColumnName = lowerColumnName;
    }

    public String getUpperColumnName() {
        return upperColumnName;
    }

    public void setUpperColumnName(String upperColumnName) {
        this.upperColumnName = upperColumnName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public DBField getField() {
        return field;
    }

    public void setField(DBField field) {
        this.field = field;
    }

    public DBCascadeField getCascadeField() {
        return cascadeField;
    }

    public void setCascadeField(DBCascadeField cascadeField) {
        this.cascadeField = cascadeField;
    }

    public Class<?> getClz() {
        return clz;
    }

    public void setClz(Class<?> clz) {
        this.clz = clz;
    }

    public boolean isPk() {
        return pk;
    }

    public void setPk(boolean pk) {
        this.pk = pk;
    }

    public PropertyHold getFieldAccessor() {
        return fieldAccessor;
    }

    public void setFieldAccessor(PropertyHold fieldAccessor) {
        this.fieldAccessor = fieldAccessor;
    }

    public Object getUnsavedValue() {
        return unsavedValue;
    }

    public void setUnsavedValue(Object unsavedValue) {
        this.unsavedValue = unsavedValue;
    }

    public TableMapping getMeta() {
        return meta;
    }

    public void setMeta(TableMapping meta) {
        this.meta = meta;
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public GeneratedValue getGv() {
        return gv;
    }

    public void setGv(GeneratedValue gv) {
        this.gv = gv;
    }

    public boolean isLob() {
        return lob;
    }

    public void setLob(boolean lob) {
        this.lob = lob;
    }

    public Temporal getTemporal() {
        return temporal;
    }

    public void setTemporal(Temporal temporal) {
        this.temporal = temporal;
    }

    public Enumerated getEnumerated() {
        return enumerated;
    }

    public void setEnumerated(Enumerated enumerated) {
        this.enumerated = enumerated;
    }

    public int getSqlType() {
        return sqlType;
    }

    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
    }

    public TypeHandler<Object> getHandler() {
        return handler;
    }

    public void setHandler(TypeHandler<Object> handler) {
        this.handler = handler;
    }

    public OneToMany getOneToMany() {
        return oneToMany;
    }

    public void setOneToMany(OneToMany oneToMany) {
        this.oneToMany = oneToMany;
    }

    public ManyToMany getManyToMany() {
        return manyToMany;
    }

    public void setManyToMany(ManyToMany manyToMany) {
        this.manyToMany = manyToMany;
    }

    public OneToOne getOneToOne() {
        return oneToOne;
    }

    public void setOneToOne(OneToOne oneToOne) {
        this.oneToOne = oneToOne;
    }

    public ManyToOne getManyToOne() {
        return manyToOne;
    }

    public void setManyToOne(ManyToOne manyToOne) {
        this.manyToOne = manyToOne;
    }

    public JoinColumn getJoinColumn() {
        return joinColumn;
    }

    public void setJoinColumn(JoinColumn joinColumn) {
        this.joinColumn = joinColumn;
    }

    public JoinColumns getJoinColumns() {
        return joinColumns;
    }

    public void setJoinColumns(JoinColumns joinColumns) {
        this.joinColumns = joinColumns;
    }

    public JoinTable getJoinTable() {
        return joinTable;
    }

    public void setJoinTable(JoinTable joinTable) {
        this.joinTable = joinTable;
    }

    public boolean isUnsavedValueDeclared() {
        return unsavedValueDeclared;
    }

    public void setUnsavedValueDeclared(boolean unsavedValueDeclared) {
        this.unsavedValueDeclared = unsavedValueDeclared;
    }

    public boolean isNotInsert() {
        return notInsert;
    }

    public void setNotInsert(boolean notInsert) {
        this.notInsert = notInsert;
    }

    public boolean isNotUpdate() {
        return notUpdate;
    }

    public void setNotUpdate(boolean notUpdate) {
        this.notUpdate = notUpdate;
    }

    public ColumnDBType getColumnDef() {
        return columnDef;
    }

    public void setColumnDef(ColumnDBType columnDef) {
        this.columnDef = columnDef;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public SequenceGenerator getSequenceGenerator() {
        return sequenceGenerator;
    }

    public void setSequenceGenerator(SequenceGenerator sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
    }

    public TableGenerator getTableGenerator() {
        return tableGenerator;
    }

    public void setTableGenerator(TableGenerator tableGenerator) {
        this.tableGenerator = tableGenerator;
    }

    public OrderBy getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(OrderBy orderBy) {
        this.orderBy = orderBy;
    }

    public boolean isTransient1() {
        return transient1;
    }

    public void setTransient1(boolean transient1) {
        this.transient1 = transient1;
    }

    public Transient getJpaTransient() {
        return jpaTransient;
    }

    public void setJpaTransient(Transient jpaTransient) {
        this.jpaTransient = jpaTransient;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public CascadeConfig getCascadeConfig() {
        return cascadeConfig;
    }

    public void setCascadeConfig(CascadeConfig cascadeConfig) {
        this.cascadeConfig = cascadeConfig;
    }

    public FetchDBField getFetchDBField() {
        return fetchDBField;
    }

    public void setFetchDBField(FetchDBField fetchDBField) {
        this.fetchDBField = fetchDBField;
    }

    public UniqueKeyGenerator getUniqueKeyGenerator() {
        return uniqueKeyGenerator;
    }

    public void setUniqueKeyGenerator(UniqueKeyGenerator uniqueKeyGenerator) {
        this.uniqueKeyGenerator = uniqueKeyGenerator;
    }

    public IdentifierGenerator getIdentifierGenerator() {
        return identifierGenerator;
    }

    public void setIdentifierGenerator(IdentifierGenerator identifierGenerator) {
        this.identifierGenerator = identifierGenerator;
    }

    public boolean isVersion() {
        return version;
    }

    public void setVersion(boolean version) {
        this.version = version;
    }

    public boolean isCascade() {
        return oneToMany != null || oneToOne != null || manyToMany != null || manyToOne != null;
    }

    @Override
    public String toString() {
        return "ColumnMapping{" +
                "fieldName='" + fieldName + '\'' +
                '}';
    }
}
