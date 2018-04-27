package spring.dao;

import db.domain.User;
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
public class DMLTest {
    static {
        // new EntityEnhancer().enhanceJavassist("sf.db.domain");
        new EntityEnhancerASM().enhance("db.domain");
    }

    @Resource
    private DaoTemplate dt;

    @BeforeClass
    public static void before() {
        JetbrickHandler.getInstance().loadAllSQL();
    }

    @Test
    public void createTable() {
//        dt.createTable(User.class);
//        dt.createTable(Role.class);
//        dt.createTable(Privilege.class);
        dt.createTables();
        System.out.println(1);
    }
    
    @Test
    public void testRefresh() {
        dt.refreshTable(User.class);
        System.out.println(1);
    }
}
