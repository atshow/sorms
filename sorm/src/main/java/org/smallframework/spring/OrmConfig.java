package org.smallframework.spring;

import sf.common.log.LogUtil;
import sf.common.log.OrmLog;
import sf.database.DBObject;
import sf.database.dao.DBClient;
import sf.database.jdbc.rowmapper.BeanRowMapper;
import sf.database.support.QuerableEntityScanner;
import sf.database.template.TemplateRender;
import sf.database.util.OrmValueUtils;
import sf.tools.StringUtils;

import javax.sql.DataSource;
import java.util.Map;

public class OrmConfig {
    /**
     * 多数据源。分库分表时可以使用。 在Spring配置时，可以使用这样的格式来配置
     *
     * <pre>
     * <code>
     * &lt;property name="dataSources"&gt;
     * 	&lt;map&gt;
     * 	 &lt;entry key="dsname1" value-ref="ds1" /&gt;
     * 	 &lt;entry key="dsname2" value-ref="ds2" /&gt;
     * 	&lt;/map&gt;
     * &lt;/property&gt;
     * </code>
     * </pre>
     */
    private Map<String, DataSource> dataSources;

    /**
     * 单数据源。
     */
    protected DataSource dataSource;

    /**
     * 多数据源时的缺省数据源名称
     */
    private String defaultDatasource;

    /**
     * 事务支持类型
     * @see #
     */
    private TransactionMode transactionMode;


    /**
     * 指定扫描若干包,配置示例如下—— <code><pre>
     * &lt;list&gt;
     *  &lt;value&gt;org.sf.test&lt;/value&gt;
     *  &lt;value&gt;org.sf.entity&lt;/value&gt;
     * &lt;/list&gt;
     * </pre></code>
     */
    private String[] packagesToScan;

    /**
     * 对配置了包扫描的路径进行增强检查，方便单元测试
     */
    private boolean enhanceScanPackages = true;

    /**
     * 扫描已知的若干注解实体类，配置示例如下—— <code><pre>
     * &lt;list&gt;
     *  &lt;value&gt;org.sf.testp.jta.Product&lt;/value&gt;
     *  &lt;value&gt;org.sf.testp.jta.Users&lt;/value&gt;
     * &lt;/list&gt;
     * </pre></code>
     */
    private String[] annotatedClasses;


    /**
     * 扫描到实体后，如果数据库中不存在，是否建表 <br>
     * 默认开启
     */
    private boolean createTable = true;

    /**
     * 扫描到实体后，如果数据库中存在对应表，是否修改表 <br>
     * 默认开启
     */
    private boolean alterTable = true;

    /**
     * 扫描到实体后，如果准备修改表，如果数据库中的列更多，是否允许删除列 <br>
     * 默认关闭
     */
    private boolean allowDropColumn;


    //以下为新增.
    /**
     * 连接
     */
    private DBClient dbClient;

    /**
     * sql日志开关
     */
    private boolean showSql = true;

    /**
     * 是否开启快速bean转换
     */
    private boolean fastBeanMethod = false;

    /**
     * 是否使用tail获取额外属性.
     */
    private boolean useTail = false;

    /**
     * sql模板位置
     */
    private String sqlTemplate = "/jetx.sql";

    public void init() {
        //设置日志开关
        OrmLog.open = showSql;
        OrmValueUtils.fast = fastBeanMethod;
        BeanRowMapper.useTail = useTail;
        if (StringUtils.isBlank(sqlTemplate)) {
            TemplateRender.DEFAULT_TEMPLATE = sqlTemplate;
        }

        if (packagesToScan != null || annotatedClasses != null) {
            QuerableEntityScanner qe = new QuerableEntityScanner();
            if (transactionMode == TransactionMode.JTA) {
                // JTA事务下，DDL语句必须在已启动后立刻就做，迟了就被套进JTA的事务中，出错。
                qe.setCheckSequence(false);
            }
            qe.setImplClasses(DBObject.class);
            qe.setAllowDropColumn(allowDropColumn);
            qe.setAlterTable(alterTable);
            qe.setCreateTable(createTable);
            qe.setDbClient(dbClient);
            if (annotatedClasses != null)
                qe.registeEntity(annotatedClasses);
            if (packagesToScan != null) {
                String joined = StringUtils.join(packagesToScan, ",");
                qe.setPackageNames(joined);
                LogUtil.info("Starting scan orm entity from package: {}", joined);
                qe.doScan();
            }
        }
    }


    public Map<String, DataSource> getDataSources() {
        return dataSources;
    }

    public void setDataSources(Map<String, DataSource> dataSources) {
        this.dataSources = dataSources;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getDefaultDatasource() {
        return defaultDatasource;
    }

    public void setDefaultDatasource(String defaultDatasource) {
        this.defaultDatasource = defaultDatasource;
    }

    public TransactionMode getTransactionMode() {
        return transactionMode;
    }

    public void setTransactionMode(TransactionMode transactionMode) {
        this.transactionMode = transactionMode;
    }

    public String[] getPackagesToScan() {
        return packagesToScan;
    }

    public void setPackagesToScan(String[] packagesToScan) {
        this.packagesToScan = packagesToScan;
    }

    public boolean isEnhanceScanPackages() {
        return enhanceScanPackages;
    }

    public void setEnhanceScanPackages(boolean enhanceScanPackages) {
        this.enhanceScanPackages = enhanceScanPackages;
    }

    public String[] getAnnotatedClasses() {
        return annotatedClasses;
    }

    public void setAnnotatedClasses(String[] annotatedClasses) {
        this.annotatedClasses = annotatedClasses;
    }

    public boolean isCreateTable() {
        return createTable;
    }

    public void setCreateTable(boolean createTable) {
        this.createTable = createTable;
    }

    public boolean isAlterTable() {
        return alterTable;
    }

    public void setAlterTable(boolean alterTable) {
        this.alterTable = alterTable;
    }

    public boolean isAllowDropColumn() {
        return allowDropColumn;
    }

    public void setAllowDropColumn(boolean allowDropColumn) {
        this.allowDropColumn = allowDropColumn;
    }

    public DBClient getDbClient() {
        return dbClient;
    }

    public void setDbClient(DBClient dbClient) {
        this.dbClient = dbClient;
    }

    public boolean isShowSql() {
        return showSql;
    }

    public void setShowSql(boolean showSql) {
        this.showSql = showSql;
    }

    public boolean isFastBeanMethod() {
        return fastBeanMethod;
    }

    public void setFastBeanMethod(boolean fastBeanMethod) {
        this.fastBeanMethod = fastBeanMethod;
    }

    public boolean isUseTail() {
        return useTail;
    }

    public void setUseTail(boolean useTail) {
        this.useTail = useTail;
    }

    public String getSqlTemplate() {
        return sqlTemplate;
    }

    public void setSqlTemplate(String sqlTemplate) {
        this.sqlTemplate = sqlTemplate;
    }
}
