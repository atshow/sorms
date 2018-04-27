package org.smallframework.springboot;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = SmallOrmProperties.ORM_PREFIX)
public class SmallOrmProperties {

    public static final String ORM_PREFIX = "smallorm";

    /**
     * 指定扫描若干包,配置示例如下—— <code><pre>
     */
    private String packages;

    /**
     * 对配置了包扫描的路径进行增强检查，方便单元测试
     */
    private boolean enhanceScanPackages = true;


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

    public String getPackages() {
        return packages;
    }

    public void setPackages(String packages) {
        this.packages = packages;
    }

    public boolean isEnhanceScanPackages() {
        return enhanceScanPackages;
    }

    public void setEnhanceScanPackages(boolean enhanceScanPackages) {
        this.enhanceScanPackages = enhanceScanPackages;
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
