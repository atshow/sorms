package querydsl;

import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLTemplates;
import db.domain.User;
import db.domain.User.Field;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import sf.SefApplication;
import sf.querydsl.JPAEntityPath;
import sf.querydsl.QueryDSL;
import sf.querydsl.SQLRelationalPath;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"application.properties"}, classes = {
        SefApplication.class}, webEnvironment = WebEnvironment.NONE)
@Transactional
@Rollback
public class QueryTest {
    @Resource
    private DataSource dataSourceMysql;

    @Test
    public void t() throws SQLException {
        JPAEntityPath<?> customer = QueryDSL.entityPathBase(User.class);
        SQLTemplates dialect = new MySQLTemplates(); // SQL-dialect
        Connection con = dataSourceMysql.getConnection();
        SQLQuery<?> query = new SQLQuery<Void>(con, dialect);
        List<User> users = ((SQLQuery)query.select(customer)/*.select(QueryDSL.column(customer, Field.nicename))*/
                .from(customer)
                .where(customer.column(Field.deleted).eq(false))).fetch();
        System.out.println(users.size());
    }

    @Test
    public void t2() throws SQLException {
        SQLRelationalPath<User> customer = QueryDSL.relationalPathBase(User.class);
        SQLTemplates dialect = new MySQLTemplates(); // SQL-dialect
        Connection con = dataSourceMysql.getConnection();
        SQLQuery<?> query = new SQLQuery<Void>(con, dialect);
        List<String> nicenames = ((SQLQuery)query.select(customer.column(Field.nicename))
                .from(customer)
                .where(customer.column( Field.deleted).eq(false))).fetch();
        System.out.println(nicenames.size());
        List<User> users = ((SQLQuery)query.select(customer)/*.select(QueryDSL.column(customer, Field.nicename))*/
                .from(customer)
                .where(customer.column(Field.deleted).eq(false))).fetch();
        System.out.println(users.size());
    }
}
