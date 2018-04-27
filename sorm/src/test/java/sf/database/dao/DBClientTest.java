package sf.database.dao;

import org.junit.Test;

class DBClientTest extends DBClient {
    @Test
    void testUseContext() {
        DBClient db1 = new DBClient();
        db1.useContext("aaaa");
        DBClient db2 = new DBClient();
        db2.useContext("bbb");
        System.out.println(db1.dbContexts.get().getDataSource());
        System.out.println(db2.dbContexts.get().getDataSource());
    }
}
