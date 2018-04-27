package spring.dao;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLQuery;
import db.domain.User;
import db.domain.User.Field;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.smallframework.spring.DaoTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import sf.SefApplication;
import sf.common.wrapper.Page;
import sf.querydsl.OrmSQLQuery;
import sf.querydsl.QueryDSL;
import sf.querydsl.SQLRelationalPath;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"application.properties"}, classes = {
        SefApplication.class}, webEnvironment = WebEnvironment.NONE)
@Transactional
@Rollback
public class QueryDSLTest {
    @Resource
    private DaoTemplate dt;

    @Test
    public void testADslSelect() {
        User user = dt.selectOne(new User());
        long start = System.currentTimeMillis();
        SQLRelationalPath<User> q = QueryDSL.relationalPathBase(User.class);
        SQLQuery<User> query = new SQLQuery<User>();
        query.select(q).from(q).where(q.string(User.Field.displayName).isNotNull())
                .orderBy(new OrderSpecifier<>(Order.ASC, q.column(User.Field.id)));
        Page<User> page = dt.sqlQueryPage(query, 2, 3);
        System.out.println((System.currentTimeMillis() - start) + "ms ->  1");
        // System.out.println(list);
        System.out.println(page);
    }

    @Test
    public void testSelect() {
        User user = dt.selectByPrimaryKeys(User.class, 1);
        long start = System.currentTimeMillis();
        SQLRelationalPath<User> quser = QueryDSL.relationalPathBase(User.class);
        System.out.println((System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();
        SQLQuery<?> query = dt.sqlQueryFactory().queryCustom(User.class);
        List<User> users = query.select(quser).from(quser).where(quser.bool(User.Field.deleted).eq(false)).fetch();
        System.out.println((System.currentTimeMillis() - start) + "ms query0");
        // QueryDSL.setReturnType(User.class, query);
        start = System.currentTimeMillis();
        users = dt.sqlQueryFactory().queryCustom(User.class).select(quser).from(quser)
                .where(quser.bool(User.Field.deleted).eq(false)).fetch();
        System.out.println((System.currentTimeMillis() - start) + "ms query1");
        start = System.currentTimeMillis();
        users = dt.sqlQueryFactory().queryCustom(User.class).select(quser).from(quser)
                .where(quser.bool(User.Field.deleted).eq(false)).fetch();
        System.out.println((System.currentTimeMillis() - start) + "ms  query2");
        start = System.currentTimeMillis();
        Long key = dt.sqlQueryFactory().insert(quser).columns(
                quser.path(Field.maps), quser.string(Field.loginName), quser.string(Field.activationKey))
                .values("{\"c\":\"d\"}", "test", "234234").executeWithKey(quser.path(Field.id));
        System.out.println("get primary key:" + key);
        System.out.println((System.currentTimeMillis() - start) + "ms  insert");
        System.out.println(query.getMetadata().getProjection().getType());
        System.out.println(users.get(0));
        dt.fetchLinks(users.get(0));
    }

    @Test
    public void testSelect3() {
        User user = dt.selectByPrimaryKeys(User.class, 1);

        SQLRelationalPath<User> quser = QueryDSL.relationalPathBase(User.class);
        SQLQuery<Object> query = new SQLQuery<Object>();
        query.select(quser).from(quser).where(quser.bool(User.Field.deleted).eq(false));
        SQLBindings sb = query.getSQL();
        List<User> users = dt.selectList(User.class, sb.getSQL(), sb.getBindings().toArray());
        System.out.println(users.get(0));
        dt.fetchLinks(users.get(0));

        testSelect4();
    }

    @Test
    public void testSelect2() {
        SQLRelationalPath<User> quser = QueryDSL.relationalPathBase(User.class);
        SQLQuery<?> query = dt.sqlQueryFactory().query();
        query.select(quser).from(quser).where(quser.bool(User.Field.deleted).eq(false));
        long start = System.currentTimeMillis();
        List<User> users = (List<User>) query.fetch();
        System.out.println((System.currentTimeMillis() - start) + "ms");
        System.out.println(users.get(0));
        dt.fetchLinks(users.get(0));
    }

    @Test
    public void testSelect4() {
        User user = dt.selectByPrimaryKeys(User.class, 1);
        long start = System.currentTimeMillis();
        SQLRelationalPath<User> quser = QueryDSL.relationalPathBase(User.class);
        System.out.println((System.currentTimeMillis() - start) + "ms");
        SQLQuery<?> query = new OrmSQLQuery();
        start = System.currentTimeMillis();
        Map maps = new HashMap();
        maps.put("a", "b");
        // query.select(QueryDSL.column(quser, User.Field.deleted),
        // QueryDSL.column(quser,
        // User.Field.id)).from(quser).where(QueryDSL.column(quser,
        // User.Field.deleted).eq(false).and(
        // QueryDSL.column(quser, User.Field.maps).eq(maps)));
        query.select(quser).from(quser)
                .where(quser.bool(User.Field.deleted).eq(false).and(quser.number(User.Field.id).eq(1)));
        System.out.println((System.currentTimeMillis() - start) + "ms");
        // QueryDSL.setReturnType(User.class, query);
        dt.sqlQueryList(query);
        start = System.currentTimeMillis();
        List<User> users = dt.sqlQueryList(query);
        System.out.println((System.currentTimeMillis() - start) + "ms execute time");
        System.out.println(users.get(0));
        dt.fetchLinks(users.get(0));
    }
}
