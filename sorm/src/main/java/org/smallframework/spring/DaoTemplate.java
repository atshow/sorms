package org.smallframework.spring;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.spring.SpringConnectionProvider;
import com.querydsl.sql.spring.SpringExceptionTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import sf.database.dao.DBClient;
import sf.database.util.DBUtils;
import sf.querydsl.OrmSQLQueryFactory;

import javax.inject.Provider;
import javax.sql.DataSource;
import java.sql.Connection;

/**
 * spring dao 封装类
 * @author sxf
 */
public class DaoTemplate extends DBClient {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DaoTemplate.class);

    private TransactionStatus status;
    private PlatformTransactionManager transactionManager;
    private TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();

    public DaoTemplate(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected Connection getConnection(DataSource ds) {
        return DataSourceUtils.getConnection(ds);
    }

    @Override
    protected void closeConnection(Connection conn, DataSource ds) {
        DataSourceUtils.releaseConnection(conn, ds);
    }

    @Override
    public void start() {
        // 不支持该操作
        throw new UnsupportedOperationException("no support!");
    }

    public DaoTemplate() {
    }

    public boolean isTransaction() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    public boolean isCurrentTransactionReadOnly() {
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly();
    }

    public boolean isSynchronizationActive() {
        return TransactionSynchronizationManager.isSynchronizationActive();
    }

    public void beginTransaction() {
        if (status == null || status.isCompleted()) {
            status = this.getTransactionManager().getTransaction(
                    transactionDefinition);
            if (status.isNewTransaction()) {
                LOGGER.info("DaoTemplate开启新事务");
            } else {
                LOGGER.info("DaoTemplate未开启新事务,将使用之前开启的事务");
            }
        }
    }

    public void commitTransaction() {
        if (status != null && !status.isCompleted()) {
            LOGGER.info("DaoTemplate提交事务");
            this.getTransactionManager().commit(status);
        }
    }

    public void rollbackTransaction() {
        if (status != null && !status.isCompleted()) {
            LOGGER.info("DaoTemplate事务将回滚");
            this.getTransactionManager().rollback(status);
        }
    }

    public PlatformTransactionManager getTransactionManager() {
        if (transactionManager == null) {
            transactionManager = new DataSourceTransactionManager(
                    getDataSource(null));
        }
        return transactionManager;
    }

    public void setTransactionManager(
            PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public TransactionDefinition getTransactionDefinition() {
        return transactionDefinition;
    }

    public void setTransactionDefinition(
            TransactionDefinition transactionDefinition) {
        this.transactionDefinition = transactionDefinition;
    }

    @Override
    protected OrmSQLQueryFactory createSQLQueryFactory(DataSource ds) {
        Connection conn = getConnection(ds);
        SQLTemplates sqlTemplates = DBUtils.doGetDialect(conn, false).getQueryDslDialect();
        Configuration configuration = new Configuration(sqlTemplates);
        configuration.setExceptionTranslator(new SpringExceptionTranslator());
        Provider<Connection> provider = new SpringConnectionProvider(ds);
        OrmSQLQueryFactory queryFactory = new OrmSQLQueryFactory(configuration, provider);
        return queryFactory;
    }
}
