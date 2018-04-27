package sf.database.jpa;

import org.smallframework.spring.TransactionMode;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import javax.sql.DataSource;
import java.util.Map;

public class SefEntityManagerFactory implements EntityManagerFactory {

    private DataSource dataSource;
    private TransactionMode txType;

    @Override
    public EntityManager createEntityManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EntityManager createEntityManager(Map map) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Metamodel getMetamodel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isOpen() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    @Override
    public Map<String, Object> getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Cache getCache() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addNamedQuery(String name, Query query) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
        // TODO Auto-generated method stub

    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public TransactionMode getTxType() {
        return txType;
    }

    public void setTxType(TransactionMode txType) {
        this.txType = txType;
    }

}
