package sf.database.jpa;

import javax.persistence.EntityTransaction;

public class SefEntityTransaction implements EntityTransaction {

    @Override
    public void begin() {
        // TODO Auto-generated method stub

    }

    @Override
    public void commit() {
        // TODO Auto-generated method stub

    }

    @Override
    public void rollback() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRollbackOnly() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean getRollbackOnly() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isActive() {
        // TODO Auto-generated method stub
        return false;
    }

}
