package sf.database.dao;

import sf.database.datasource.RoutingDataSource;
import sf.database.dialect.DBDialect;
import sf.database.jdbc.sql.Crud;
import sf.database.transaction.DefaultTransactionManager;
import sf.database.util.DBUtils;
import sf.database.util.OrmUtils;
import sf.tools.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DaoSupport {
    protected Crud crud = Crud.getInstance();


    // 当前线程(事务)
    protected RoutingDataSource dataSource;
    protected DBDialect dialect;

    public DaoSupport(RoutingDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DaoSupport() {

    }

    /**
     * 启动一个事务(默认支持子事务)
     */
    public void start() {
        DefaultTransactionManager.start();
    }

    /**
     * 获取一个当前线程的连接(事务中)，如果没有，则新建一个。
     */
    protected Connection getConnection(DataSource ds) {
        Boolean tx = DefaultTransactionManager.inTrans();
        try {
            if (!tx) {
                return dataSource.getConnection();
            } else {
                return DefaultTransactionManager.getCurrentThreadConnection(dataSource);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从clz中获取数据源
     * @param clz
     * @return
     */
    protected DataSource getDataSource(Class<?> clz) {
        if (clz == null) {
            return dataSource;
        }
        if (dataSource.isSingleDatasource()) {
            return dataSource.getDefaultDataSource().getValue();
        }
        String bindDsName = OrmUtils.getBindDataSource(clz);
        DataSource ds = null;
        if (StringUtils.isNotBlank(bindDsName)) {
            ds = dataSource.getDataSource(bindDsName);
        }
        if (ds == null) {
            ds = dataSource.getDefaultDataSource().getValue();
        }
        return ds;
    }

    /**
     * 释放一个连接，如果 Connection 不在事务中，则关闭它，否则不处理。
     */
    protected void closeConnection(Connection conn, DataSource ds) {
        Boolean tx = DefaultTransactionManager.inTrans();
        if (!tx) {
            // not in transaction
            DBUtils.closeQuietly(conn);
        }
    }

    public DBDialect getDialect(DataSource ds) {
        if (dialect == null) {
            dialect = DBUtils.doGetDialect(getConnection(ds), true);
        }
        return dialect;
    }

    public void setDataSource(RoutingDataSource dataSource) {
        this.dataSource = dataSource;
    }
}
