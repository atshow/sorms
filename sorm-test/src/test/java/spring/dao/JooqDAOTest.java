package spring.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.jooq.DSLContext;
import org.jooq.Insert;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.JooqRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.smallframework.spring.DaoTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import db.domain.Role;
import db.domain.User;
import sf.SefApplication;
import sf.common.wrapper.Page;
import sf.jooq.JooqTables;
import sf.jooq.tables.JooqTable;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"application.properties"}, classes = {
        SefApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@Rollback
public class JooqDAOTest {
    @Resource
    private DSLContext dsl;
    @Resource
    private DaoTemplate dt;
    private DefaultDSLContext ds;

    @Test
    public void test() {
        JooqTable<?> user = JooqTables.getTable(User.class);
        JooqTable<?> role = JooqTables.getTable(Role.class);
        ds = new DefaultDSLContext(SQLDialect.MYSQL_5_7);
        Select<?> query = DSL.select(user.fields()).from(user, role).where(user.column(User.Field.id).eq(7));

        System.out.println(query.getSQL());
        Map m=new HashMap();
        m.put("a","b");
        query = DSL.selectFrom(user).where(user.column(User.Field.id).eq(7).
                or(user.column(User.Field.activationKey).eq("123").and(user.column(User.Field.maps).eq(m)).and(user.column(User.Field.loginName).eq("784"))));

        Map mpa = query.getParams();
        List<Object> values = query.getBindValues();
        List<?> l = query.getSelect();
        Class<?> ty = query.getRecordType();
        ((SelectConditionStep) query).getQuery();
        Insert insert = ds.insertInto(user).columns(user.column(User.Field.id), user.column(User.Field.displayName)).values(1, "234234");

        Result<JooqRecord> record = (Result<JooqRecord>) dsl.selectFrom(JooqTables.getTable(User.class)).fetch();
        System.out.println(record);
    }

    @Test
    public void test2() {
        JooqTable<?> quser = JooqTables.getTable(User.class);
        JooqTable<?> qrole = JooqTables.getTable(Role.class);
        Select<?> query = DSL.select(quser.fields()).from(quser, qrole).where(quser.column(User.Field.id).eq(1));
        User u = dt.jooqSelectOne(query);
        System.out.println(query.getSQL());
        u = dt.jooqSelectOne(query);
        query = DSL.selectFrom(quser).where(quser.column(User.Field.id).eq(1));
        List<User> users = dt.jooqSelectList(query);
        query = DSL.select(quser.column(User.Field.activationKey), qrole.column(Role.Field.id)).from(quser, qrole).where(quser.column(User.Field.id).eq(1));
        Page<User> page = dt.jooqSelectPage(query, 0, 2);
        Insert<?> insert = DSL.insertInto(quser).columns(quser.column(User.Field.loginName), quser.column(User.Field.activationKey)).values("1231", "234234");
        dt.jooqInsert(insert);
        System.out.println(1);
    }
}
