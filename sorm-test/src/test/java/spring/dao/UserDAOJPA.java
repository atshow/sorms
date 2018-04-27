package spring.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import sf.SefApplication;
import db.repo.UserRepo;

import javax.persistence.EntityManager;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"application.properties"}, classes = {
        SefApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@Rollback
public class UserDAOJPA {
    @Autowired
    private UserRepo ur;
    @Autowired
    private EntityManager em;

    @Test
    public void test() {
//        List<User> l = ur.findAll();
    }
}
