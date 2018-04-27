package sf.database.dao;

/**
 * sql执行上下文
 */
public class DBContext {
    /**
     * 数据源名称
     */
    private String dataSource;

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
}
