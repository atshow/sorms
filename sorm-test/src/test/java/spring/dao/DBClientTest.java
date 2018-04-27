package spring.dao;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.smallframework.spring.DaoTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import sf.SefApplication;
import sf.codegen.EntityEnhancerASM;
import sf.database.template.jetbrick.JetbrickHandler;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"application.properties"}, classes = {
        SefApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@Rollback
public class DBClientTest {
    static {
        // new EntityEnhancer().enhance("sf.db.domain");
        new EntityEnhancerASM().enhance("db.domain");
    }

    @Resource
    private DaoTemplate dt;

    @BeforeClass
    public static void before() {
        JetbrickHandler.getInstance().loadAllSQL();
    }

    @Before
    public void beforeTest(){

    }

    @Test
    public void useContext() {

    }

    @Test
    public void useContext1() {
    }

    @Test
    public void getDataSource() {
    }

    @Test
    public void closeConnection() {
    }

    @Test
    public void selectByPrimaryKeys() {
    }

    @Test
    public void selectOne() {
    }

    @Test
    public void selectList() {
    }

    @Test
    public void selectPage() {
    }

    @Test
    public void selectIterator() {
    }

    @Test
    public void selectStream() {
    }

    @Test
    public void insert() {
    }

    @Test
    public void merge() {
    }

    @Test
    public void update() {
    }

    @Test
    public void delete() {
    }

    @Test
    public void batchInsert() {
    }

    @Test
    public void batchInsertFast() {
    }

    @Test
    public void batchUpdate() {
    }

    @Test
    public void batchDelete() {
    }

    @Test
    public void createTable() {
    }

    @Test
    public void createTables() {
    }

    @Test
    public void refreshTables() {
    }

    @Test
    public void refreshTable() {
    }

    @Test
    public void fetchLinks() {
    }

    @Test
    public void fetchLinks1() {
    }

    @Test
    public void fetchLinks2() {
    }

    @Test
    public void insertLinks() {
    }

    @Test
    public void insertRelation() {
    }

    @Test
    public void updateLinks() {
    }

    @Test
    public void updateRelation() {
    }

    @Test
    public void deleteLinks() {
    }

    @Test
    public void deleteRelation() {
    }

    @Test
    public void select() {
    }

    @Test
    public void selectOne1() {
    }

    @Test
    public void selectList1() {
    }

    @Test
    public void selectArray() {
    }

    @Test
    public void selectPage1() {
    }

    @Test
    public void selectIterator1() {
    }

    @Test
    public void selectStream1() {
    }

    @Test
    public void selectResultSet() {
    }

    @Test
    public void execute() {
    }

    @Test
    public void execute1() {
    }

    @Test
    public void executeBatch() {
    }

    @Test
    public void selectTemplate() {
    }

    @Test
    public void selectOneTemplate() {
    }

    @Test
    public void selectArrayTemplate() {
    }

    @Test
    public void selectPageTemplate() {
    }

    @Test
    public void executeTemplate() {
    }

    @Test
    public void executeBatchTemplate() {
    }

    @Test
    public void selectListTemplate() {
    }

    @Test
    public void execute2() {
    }

    @Test
    public void execute3() {
    }

    @Test
    public void execute4() {
    }

    @Test
    public void fetchOneResult() {
    }

    @Test
    public void fetchOneResult1() {
    }

    @Test
    public void fetchList() {
    }

    @Test
    public void fetchList1() {
    }

    @Test
    public void fetchPage() {
    }

    @Test
    public void fetchPage1() {
    }
}