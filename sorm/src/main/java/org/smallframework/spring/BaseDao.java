package org.smallframework.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import sf.database.jpa.SefEntityManagerFactory;
import sf.database.jpa.SefEntityManagerImpl;
import sf.tools.utils.Assert;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Collections;

/**
 * 所有DAO的基类
 * @author
 */
public class BaseDao {
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private SefEntityManagerFactory sefEmf;

    @PostConstruct
    public void init() {
        Assert.notNull(entityManagerFactory, "");
        if (sefEmf == null) {
            sefEmf = (SefEntityManagerFactory) entityManagerFactory;
        }
    }

    /**
     * 获得EntityManager
     * @return
     */
    protected final EntityManager getEntityManager() {
        TransactionMode tx = sefEmf.getTxType();
        EntityManager em;
        switch (tx) {
            case JPA:
            case JTA:
                em = EntityManagerFactoryUtils.doGetTransactionalEntityManager(entityManagerFactory, null);
                if (em == null) { // 当无事务时。Spring返回null
                    em = entityManagerFactory.createEntityManager(null, Collections.EMPTY_MAP);
                }
                break;
            case JDBC:
                ConnectionHolder conn = (ConnectionHolder) TransactionSynchronizationManager
                        .getResource(sefEmf.getDataSource());
                if (conn == null) {// 基于数据源的Spring事务
                    em = entityManagerFactory.createEntityManager(null, Collections.EMPTY_MAP);
                } else {
                    // connection不能随便关闭
                    em = new SefEntityManagerImpl(conn.getConnection());
                }
                break;
            default:
                throw new UnsupportedOperationException(tx.name());
        }
        return em;
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        Assert.notNull(entityManagerFactory, "");
        this.entityManagerFactory = entityManagerFactory;
        this.sefEmf = (SefEntityManagerFactory) entityManagerFactory;
    }
}
